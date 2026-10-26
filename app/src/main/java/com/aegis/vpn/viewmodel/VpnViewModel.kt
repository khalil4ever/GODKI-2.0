package com.aegis.vpn.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.vpn.data.VpnRepository
import com.aegis.vpn.model.AuditLogEntry
import com.aegis.vpn.model.DnsProbeResult
import com.aegis.vpn.model.DnsProvider
import com.aegis.vpn.model.InstalledAppInfo
import com.aegis.vpn.model.NetworkInterfaceSnapshot
import com.aegis.vpn.model.RelayServer
import com.aegis.vpn.model.ServerCategory
import com.aegis.vpn.model.TrafficSample
import com.aegis.vpn.model.VpnProtocol
import com.aegis.vpn.model.VpnSettings
import com.aegis.vpn.model.VpnStatus
import com.aegis.vpn.service.AegisVpnService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

data class VpnUiState(
    val vpnStatus: VpnStatus = VpnStatus.DISCONNECTED,
    val isSystemVpnActive: Boolean = false,
    val settings: VpnSettings = VpnSettings(),
    val servers: List<RelayServer> = emptyList(),
    val selectedServerId: String = "ch-zrh-01",
    val searchQuery: String = "",
    val selectedCategory: ServerCategory = ServerCategory.ALL,
    val isPingingServers: Boolean = false,
    val sessionUptimeSeconds: Long = 0L,
    val liveRxBytesPerSec: Long = 0L,
    val liveTxBytesPerSec: Long = 0L,
    val sessionTotalRxBytes: Long = 0L,
    val sessionTotalTxBytes: Long = 0L,
    val trafficHistory: List<TrafficSample> = emptyList(),
    val networkInterface: NetworkInterfaceSnapshot = NetworkInterfaceSnapshot(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val isLoadingApps: Boolean = false,
    val showSystemAppsInSplitTunnel: Boolean = false,
    val appSearchQuery: String = "",
    val bypassedPackages: Set<String> = emptySet(),
    val dnsProbeHostInput: String = "one.one.one.one",
    val isRunningDnsProbe: Boolean = false,
    val dnsProbeHistory: List<DnsProbeResult> = emptyList(),
    val auditLogs: List<AuditLogEntry> = emptyList(),
    val statusBannerMessage: String? = null
) {
    val selectedServer: RelayServer
        get() = servers.firstOrNull { it.id == selectedServerId }
            ?: servers.firstOrNull()
            ?: RelayServer(
                id = "ch-zrh-01",
                countryCode = "CH",
                countryName = "Switzerland",
                city = "Zurich",
                hostname = "one.one.one.one",
                ipAddress = "1.1.1.1"
            )

    val filteredServers: List<RelayServer>
        get() {
            val q = searchQuery.trim().lowercase()
            return servers.filter { server ->
                val matchesCategory = when (selectedCategory) {
                    ServerCategory.ALL -> true
                    ServerCategory.FAVORITES -> server.isFavorite
                    ServerCategory.LOW_LATENCY -> (server.latencyMs ?: 999) <= 35
                    ServerCategory.MULTI_HOP -> server.isMultiHop
                    ServerCategory.CUSTOM -> server.isCustom
                }
                val matchesSearch = q.isEmpty() ||
                    server.countryName.lowercase().contains(q) ||
                    server.city.lowercase().contains(q) ||
                    server.hostname.lowercase().contains(q) ||
                    server.ipAddress.lowercase().contains(q)
                matchesCategory && matchesSearch
            }
        }

    val filteredApps: List<InstalledAppInfo>
        get() {
            val q = appSearchQuery.trim().lowercase()
            return installedApps.filter { app ->
                val matchesSystemFilter = showSystemAppsInSplitTunnel || !app.isSystemApp
                val matchesQuery = q.isEmpty() ||
                    app.appName.lowercase().contains(q) ||
                    app.packageName.lowercase().contains(q)
                matchesSystemFilter && matchesQuery
            }
        }
}

class VpnViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VpnRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(VpnUiState())
    val uiState: StateFlow<VpnUiState> = _uiState.asStateFlow()

    private var connectionJob: Job? = null
    private var lastKernelRxBytes: Long = -1L
    private var lastKernelTxBytes: Long = -1L

    init {
        initializeState()
        startLiveTelemetryLoop()
        observeSystemVpnServiceState()
    }

    private fun initializeState() {
        val settings = repository.loadSettings()
        val favorites = repository.loadFavoriteServerIds()
        val customServers = repository.loadCustomServers()
        val builtInServers = repository.getBuiltInRelayServers(favorites)
        val allServers = (customServers.map { it.copy(isFavorite = favorites.contains(it.id)) } + builtInServers)
        val selectedId = repository.loadSelectedServerId()
            .takeIf { id -> allServers.any { it.id == id } }
            ?: allServers.first().id
        val bypassed = repository.loadBypassedPackages()
        val netSnapshot = repository.inspectActiveNetworkInterface()

        val initialSamples = List(24) { idx ->
            TrafficSample(
                timestampMs = System.currentTimeMillis() - (24 - idx) * 1000L,
                rxBytesPerSec = 0L,
                txBytesPerSec = 0L
            )
        }

        val initialLogs = listOf(
            repository.createAuditEntry(
                eventType = "KERNEL_INIT",
                details = "Aegis Telemetry Engine attached to ${netSnapshot.interfaceName} (${netSnapshot.localIpv4})"
            ),
            repository.createAuditEntry(
                eventType = "CIPHER_READY",
                details = "Configured ${settings.protocol.displayName} (${settings.protocol.cipherSuite}), DNS ${settings.dnsProvider.primaryDns}"
            )
        )

        _uiState.value = VpnUiState(
            settings = settings,
            servers = allServers,
            selectedServerId = selectedId,
            trafficHistory = initialSamples,
            networkInterface = netSnapshot,
            bypassedPackages = bypassed,
            auditLogs = initialLogs
        )

        loadInstalledAppsForSplitTunnel()
    }

    private fun observeSystemVpnServiceState() {
        viewModelScope.launch {
            AegisVpnService.isSystemTunnelEstablished.collect { established ->
                _uiState.update { it.copy(isSystemVpnActive = established) }
            }
        }
    }

    private fun startLiveTelemetryLoop() {
        viewModelScope.launch {
            val (initRx, initTx) = repository.readKernelTrafficBytes()
            lastKernelRxBytes = initRx
            lastKernelTxBytes = initTx

            var tickCount = 0
            while (isActive) {
                delay(1000L)
                tickCount++

                val (currentRx, currentTx) = repository.readKernelTrafficBytes()
                val deltaRx = if (lastKernelRxBytes >= 0 && currentRx >= lastKernelRxBytes) {
                    currentRx - lastKernelRxBytes
                } else 0L
                val deltaTx = if (lastKernelTxBytes >= 0 && currentTx >= lastKernelTxBytes) {
                    currentTx - lastKernelTxBytes
                } else 0L

                lastKernelRxBytes = currentRx
                lastKernelTxBytes = currentTx

                val netSnapshot = if (tickCount % 3 == 0) {
                    repository.inspectActiveNetworkInterface()
                } else {
                    _uiState.value.networkInterface
                }

                _uiState.update { state ->
                    val isConnected = state.vpnStatus == VpnStatus.CONNECTED
                    val newUptime = if (isConnected) state.sessionUptimeSeconds + 1 else 0L
                    val newSessionRx = if (isConnected) state.sessionTotalRxBytes + deltaRx else state.sessionTotalRxBytes
                    val newSessionTx = if (isConnected) state.sessionTotalTxBytes + deltaTx else state.sessionTotalTxBytes

                    val sample = TrafficSample(
                        timestampMs = System.currentTimeMillis(),
                        rxBytesPerSec = deltaRx,
                        txBytesPerSec = deltaTx
                    )
                    val updatedHistory = (state.trafficHistory + sample).takeLast(28)

                    state.copy(
                        liveRxBytesPerSec = deltaRx,
                        liveTxBytesPerSec = deltaTx,
                        sessionUptimeSeconds = newUptime,
                        sessionTotalRxBytes = newSessionRx,
                        sessionTotalTxBytes = newSessionTx,
                        trafficHistory = updatedHistory,
                        networkInterface = netSnapshot
                    )
                }
            }
        }
    }

    fun completeOnboarding() {
        val updated = _uiState.value.settings.copy(onboardingCompleted = true)
        repository.saveSettings(updated)
        _uiState.update { it.copy(settings = updated) }
    }

    fun reopenOnboarding() {
        val updated = _uiState.value.settings.copy(onboardingCompleted = false)
        repository.saveSettings(updated)
        _uiState.update { it.copy(settings = updated) }
    }

    /**
     * Initiates VPN handshake after VpnService permission is granted (or in Sandbox Demo Tunnel mode).
     */
    fun engageTunnel(systemPermissionGranted: Boolean) {
        connectionJob?.cancel()
        val state = _uiState.value
        val relay = state.selectedServer
        val settings = state.settings

        _uiState.update {
            it.copy(
                vpnStatus = VpnStatus.CONNECTING,
                sessionUptimeSeconds = 0L,
                sessionTotalRxBytes = 0L,
                sessionTotalTxBytes = 0L,
                statusBannerMessage = "Negotiating ${settings.protocol.displayName} handshake with ${relay.city}…"
            )
        }
        appendAuditLog(
            eventType = "HANDSHAKE_INIT",
            details = "Initiating ${settings.protocol.displayName} tunnel to ${relay.city} (${relay.ipAddress}:${relay.port})"
        )

        connectionJob = viewModelScope.launch {
            val measuredPing = repository.measureRelayLatencyMs(relay)
            delay(450L)

            val useSystemService = settings.useRealSystemVpnService && systemPermissionGranted
            if (useSystemService) {
                val appContext = getApplication<Application>().applicationContext
                val intent = Intent(appContext, AegisVpnService::class.java).apply {
                    action = AegisVpnService.ACTION_CONNECT
                    putExtra(AegisVpnService.EXTRA_SESSION_NAME, "Aegis VPN - ${relay.city}")
                    putExtra(AegisVpnService.EXTRA_MTU, settings.mtu)
                    putExtra(AegisVpnService.EXTRA_PRIMARY_DNS, settings.dnsProvider.primaryDns)
                    putExtra(AegisVpnService.EXTRA_SECONDARY_DNS, settings.dnsProvider.secondaryDns)
                    putStringArrayListExtra(
                        AegisVpnService.EXTRA_BYPASSED_APPS,
                        ArrayList(_uiState.value.bypassedPackages)
                    )
                }
                runCatching { appContext.startService(intent) }
            }

            val updatedServers = _uiState.value.servers.map {
                if (it.id == relay.id) it.copy(latencyMs = measuredPing) else it
            }

            val modeLabel = if (useSystemService) {
                "Android VpnService (10.88.0.2)"
            } else {
                "Sandbox Demo Tunnel (10.88.0.2)"
            }

            _uiState.update {
                it.copy(
                    vpnStatus = VpnStatus.CONNECTED,
                    servers = updatedServers,
                    networkInterface = repository.inspectActiveNetworkInterface(),
                    statusBannerMessage = "Shield Active • $modeLabel • ${measuredPing}ms"
                )
            }

            appendAuditLog(
                eventType = "TUNNEL_ESTABLISHED",
                details = "Connected to ${relay.city} via ${settings.protocol.displayName} ($modeLabel, MTU ${settings.mtu}, DNS ${settings.dnsProvider.primaryDns})"
            )
        }
    }

    fun disconnectTunnel() {
        connectionJob?.cancel()
        val appContext = getApplication<Application>().applicationContext
        val stopIntent = Intent(appContext, AegisVpnService::class.java).apply {
            action = AegisVpnService.ACTION_DISCONNECT
        }
        runCatching { appContext.startService(stopIntent) }

        val prevServer = _uiState.value.selectedServer
        val rxTotal = _uiState.value.sessionTotalRxBytes
        val txTotal = _uiState.value.sessionTotalTxBytes

        _uiState.update {
            it.copy(
                vpnStatus = VpnStatus.DISCONNECTED,
                isSystemVpnActive = false,
                sessionUptimeSeconds = 0L,
                networkInterface = repository.inspectActiveNetworkInterface(),
                statusBannerMessage = "Tunnel terminated. Device returned to direct routing."
            )
        }

        appendAuditLog(
            eventType = "TUNNEL_CLOSED",
            details = "Disconnected from ${prevServer.city} (Session RX: ${formatBytes(rxTotal)}, TX: ${formatBytes(txTotal)})"
        )
    }

    fun selectServer(server: RelayServer, reconnectIfActive: Boolean = true) {
        repository.saveSelectedServerId(server.id)
        val wasConnected = _uiState.value.vpnStatus == VpnStatus.CONNECTED
        _uiState.update {
            it.copy(
                selectedServerId = server.id,
                statusBannerMessage = "Selected relay: ${server.city}, ${server.countryName}"
            )
        }
        appendAuditLog(
            eventType = "RELAY_SELECTED",
            details = "Switched target relay node to ${server.city} (${server.hostname})"
        )
        if (wasConnected && reconnectIfActive) {
            engageTunnel(systemPermissionGranted = _uiState.value.isSystemVpnActive)
        }
    }

    fun toggleFavoriteServer(serverId: String) {
        val currentFavs = repository.loadFavoriteServerIds().toMutableSet()
        if (currentFavs.contains(serverId)) {
            currentFavs.remove(serverId)
        } else {
            currentFavs.add(serverId)
        }
        repository.saveFavoriteServerIds(currentFavs)
        _uiState.update { state ->
            state.copy(
                servers = state.servers.map { s ->
                    if (s.id == serverId) s.copy(isFavorite = currentFavs.contains(s.id)) else s
                }
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(category: ServerCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun pingAllServers() {
        if (_uiState.value.isPingingServers) return
        _uiState.update { it.copy(isPingingServers = true, statusBannerMessage = "Measuring live latency to all relay endpoints…") }
        viewModelScope.launch {
            val updated = _uiState.value.servers.map { server ->
                val ms = repository.measureRelayLatencyMs(server)
                server.copy(latencyMs = ms)
            }
            _uiState.update {
                it.copy(
                    servers = updated,
                    isPingingServers = false,
                    statusBannerMessage = "Live latency probe completed across ${updated.size} nodes."
                )
            }
            appendAuditLog(
                eventType = "LATENCY_SWEEP",
                details = "Completed live TCP/DNS latency sweep across ${updated.size} relay endpoints."
            )
        }
    }

    fun addCustomServer(
        cityOrLabel: String,
        countryName: String,
        hostnameOrIp: String,
        port: Int,
        protocol: VpnProtocol
    ) {
        val cleanHost = hostnameOrIp.trim()
        if (cleanHost.isEmpty()) return

        val customServer = RelayServer(
            id = "custom-${UUID.randomUUID().toString().take(8)}",
            countryCode = countryName.trim().take(2).uppercase().ifBlank { "CU" },
            countryName = countryName.trim().ifBlank { "Custom Network" },
            city = cityOrLabel.trim().ifBlank { "Custom Node" },
            hostname = cleanHost,
            ipAddress = cleanHost,
            port = port.coerceIn(1, 65535),
            protocol = protocol,
            loadPercent = 10,
            latencyMs = null,
            isCustom = true,
            isFavorite = true,
            isDemoEndpoint = false
        )

        val existingCustom = repository.loadCustomServers()
        val updatedCustom = listOf(customServer) + existingCustom
        repository.saveCustomServers(updatedCustom)

        val favs = repository.loadFavoriteServerIds() + customServer.id
        repository.saveFavoriteServerIds(favs)
        repository.saveSelectedServerId(customServer.id)

        _uiState.update { state ->
            state.copy(
                servers = listOf(customServer) + state.servers,
                selectedServerId = customServer.id,
                statusBannerMessage = "Added custom ${protocol.displayName} node: ${customServer.city}"
            )
        }
        appendAuditLog(
            eventType = "CUSTOM_NODE_ADDED",
            details = "Added user relay profile '${customServer.city}' (${cleanHost}:${customServer.port})"
        )
    }

    fun deleteCustomServer(serverId: String) {
        val updatedCustom = repository.loadCustomServers().filterNot { it.id == serverId }
        repository.saveCustomServers(updatedCustom)
        _uiState.update { state ->
            val remaining = state.servers.filterNot { it.id == serverId }
            val nextSelected = if (state.selectedServerId == serverId) {
                remaining.firstOrNull()?.id ?: "ch-zrh-01"
            } else {
                state.selectedServerId
            }
            state.copy(
                servers = remaining,
                selectedServerId = nextSelected,
                statusBannerMessage = "Removed custom server profile."
            )
        }
    }

    fun loadInstalledAppsForSplitTunnel() {
        _uiState.update { it.copy(isLoadingApps = true) }
        viewModelScope.launch {
            val bypassed = _uiState.value.bypassedPackages
            val apps = repository.queryInstalledApps(bypassed)
            _uiState.update {
                it.copy(
                    installedApps = apps,
                    isLoadingApps = false
                )
            }
        }
    }

    fun toggleAppSplitTunnelBypass(packageName: String) {
        val current = _uiState.value.bypassedPackages.toMutableSet()
        val bypassedNow = if (current.contains(packageName)) {
            current.remove(packageName)
            false
        } else {
            current.add(packageName)
            true
        }
        repository.saveBypassedPackages(current)
        _uiState.update { state ->
            state.copy(
                bypassedPackages = current,
                installedApps = state.installedApps.map { app ->
                    if (app.packageName == packageName) app.copy(isBypassed = bypassedNow) else app
                }
            )
        }
        appendAuditLog(
            eventType = "SPLIT_TUNNEL_RULE",
            details = if (bypassedNow) {
                "Excluded $packageName from VPN tunnel (Direct Bypass)"
            } else {
                "Routed $packageName inside encrypted VPN tunnel"
            }
        )
    }

    fun updateAppSearchQuery(query: String) {
        _uiState.update { it.copy(appSearchQuery = query) }
    }

    fun toggleShowSystemApps(show: Boolean) {
        _uiState.update { it.copy(showSystemAppsInSplitTunnel = show) }
    }

    fun updateDnsProbeHostInput(host: String) {
        _uiState.update { it.copy(dnsProbeHostInput = host) }
    }

    fun runLiveDnsProbe(targetHost: String = _uiState.value.dnsProbeHostInput) {
        if (_uiState.value.isRunningDnsProbe) return
        _uiState.update { it.copy(isRunningDnsProbe = true, dnsProbeHostInput = targetHost) }
        viewModelScope.launch {
            val result = repository.performLiveDnsAndLatencyProbe(targetHost)
            _uiState.update { state ->
                state.copy(
                    isRunningDnsProbe = false,
                    dnsProbeHistory = (listOf(result) + state.dnsProbeHistory).take(15)
                )
            }
            appendAuditLog(
                eventType = if (result.reachable) "DNS_PROBE_OK" else "DNS_PROBE_FAIL",
                details = if (result.reachable) {
                    "Resolved ${result.host} -> ${result.resolvedIps.firstOrNull() ?: "—"} in ${result.dnsLookupMs}ms (TCP: ${result.tcpHandshakeMs?.let { "${it}ms" } ?: "N/A"})"
                } else {
                    "Failed to resolve ${result.host}: ${result.errorMessage}"
                },
                isAlert = !result.reachable
            )
        }
    }

    fun clearAuditLogs() {
        _uiState.update {
            it.copy(
                auditLogs = listOf(
                    repository.createAuditEntry(
                        eventType = "LOGS_PURGED",
                        details = "Zero-log session buffer cleared by user."
                    )
                )
            )
        }
    }

    fun updateProtocol(protocol: VpnProtocol) {
        val updated = _uiState.value.settings.copy(protocol = protocol)
        repository.saveSettings(updated)
        _uiState.update { it.copy(settings = updated) }
        appendAuditLog(
            eventType = "PROTOCOL_CHANGE",
            details = "Switched tunnel protocol to ${protocol.displayName} (${protocol.cipherSuite})"
        )
    }

    fun updateDnsProvider(dnsProvider: DnsProvider) {
        val updated = _uiState.value.settings.copy(dnsProvider = dnsProvider)
        repository.saveSettings(updated)
        _uiState.update { it.copy(settings = updated) }
        appendAuditLog(
            eventType = "DNS_CONFIG",
            details = "Updated primary DNS resolver to ${dnsProvider.displayName} (${dnsProvider.primaryDns})"
        )
    }

    fun toggleKillSwitch(enabled: Boolean) {
        val updated = _uiState.value.settings.copy(killSwitchEnabled = enabled)
        repository.saveSettings(updated)
        _uiState.update { it.copy(settings = updated) }
        appendAuditLog(
            eventType = "KILL_SWITCH",
            details = if (enabled) "Kill Switch Guard armed." else "Kill Switch Guard disarmed.",
            isAlert = !enabled
        )
    }

    fun toggleDnsLeakProtection(enabled: Boolean) {
        val updated = _uiState.value.settings.copy(dnsLeakProtection = enabled)
        repository.saveSettings(updated)
        _uiState.update { it.copy(settings = updated) }
    }

    fun toggleUseRealSystemVpnService(enabled: Boolean) {
        val updated = _uiState.value.settings.copy(useRealSystemVpnService = enabled)
        repository.saveSettings(updated)
        _uiState.update { it.copy(settings = updated) }
    }

    fun updateMtu(mtu: Int) {
        val clamped = mtu.coerceIn(1280, 1500)
        val updated = _uiState.value.settings.copy(mtu = clamped)
        repository.saveSettings(updated)
        _uiState.update { it.copy(settings = updated) }
    }

    fun toggleDarkTheme(dark: Boolean) {
        val updated = _uiState.value.settings.copy(darkTheme = dark)
        repository.saveSettings(updated)
        _uiState.update { it.copy(settings = updated) }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    private fun appendAuditLog(eventType: String, details: String, isAlert: Boolean = false) {
        val entry = repository.createAuditEntry(eventType, details, isAlert)
        _uiState.update { state ->
            state.copy(auditLogs = (listOf(entry) + state.auditLogs).take(60))
        }
    }

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes < 1024L) return "$bytes B"
            val kb = bytes / 1024.0
            if (kb < 1024.0) return String.format(java.util.Locale.US, "%.1f KB", kb)
            val mb = kb / 1024.0
            if (mb < 1024.0) return String.format(java.util.Locale.US, "%.2f MB", mb)
            val gb = mb / 1024.0
            return String.format(java.util.Locale.US, "%.2f GB", gb)
        }

        fun formatSpeedPerSec(bytesPerSec: Long): String {
            return "${formatBytes(bytesPerSec)}/s"
        }

        fun formatDuration(seconds: Long): String {
            val hrs = seconds / 3600
            val mins = (seconds % 3600) / 60
            val secs = seconds % 60
            return String.format(java.util.Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
        }
    }
}
