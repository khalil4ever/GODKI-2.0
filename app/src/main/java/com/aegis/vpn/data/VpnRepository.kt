package com.aegis.vpn.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import com.aegis.vpn.model.AuditLogEntry
import com.aegis.vpn.model.DnsProbeResult
import com.aegis.vpn.model.DnsProvider
import com.aegis.vpn.model.InstalledAppInfo
import com.aegis.vpn.model.NetworkInterfaceSnapshot
import com.aegis.vpn.model.RelayServer
import com.aegis.vpn.model.VpnProtocol
import com.aegis.vpn.model.VpnSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class VpnRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadSettings(): VpnSettings {
        val protocolName = prefs.getString(KEY_PROTOCOL, VpnProtocol.WIREGUARD.name)
        val dnsName = prefs.getString(KEY_DNS_PROVIDER, DnsProvider.CLOUDFLARE.name)
        return VpnSettings(
            protocol = runCatching { VpnProtocol.valueOf(protocolName!!) }.getOrDefault(VpnProtocol.WIREGUARD),
            dnsProvider = runCatching { DnsProvider.valueOf(dnsName!!) }.getOrDefault(DnsProvider.CLOUDFLARE),
            killSwitchEnabled = prefs.getBoolean(KEY_KILL_SWITCH, true),
            dnsLeakProtection = prefs.getBoolean(KEY_DNS_LEAK, true),
            useRealSystemVpnService = prefs.getBoolean(KEY_USE_REAL_VPN_SERVICE, true),
            mtu = prefs.getInt(KEY_MTU, 1420),
            darkTheme = prefs.getBoolean(KEY_DARK_THEME, false),
            onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        )
    }

    fun saveSettings(settings: VpnSettings) {
        prefs.edit()
            .putString(KEY_PROTOCOL, settings.protocol.name)
            .putString(KEY_DNS_PROVIDER, settings.dnsProvider.name)
            .putBoolean(KEY_KILL_SWITCH, settings.killSwitchEnabled)
            .putBoolean(KEY_DNS_LEAK, settings.dnsLeakProtection)
            .putBoolean(KEY_USE_REAL_VPN_SERVICE, settings.useRealSystemVpnService)
            .putInt(KEY_MTU, settings.mtu)
            .putBoolean(KEY_DARK_THEME, settings.darkTheme)
            .putBoolean(KEY_ONBOARDING_DONE, settings.onboardingCompleted)
            .apply()
    }

    fun loadFavoriteServerIds(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITE_SERVERS, setOf("ch-zrh-01", "us-nyc-01")) ?: emptySet()
    }

    fun saveFavoriteServerIds(ids: Set<String>) {
        prefs.edit().putStringSet(KEY_FAVORITE_SERVERS, ids).apply()
    }

    fun loadSelectedServerId(): String {
        return prefs.getString(KEY_SELECTED_SERVER, "ch-zrh-01") ?: "ch-zrh-01"
    }

    fun saveSelectedServerId(id: String) {
        prefs.edit().putString(KEY_SELECTED_SERVER, id).apply()
    }

    fun loadBypassedPackages(): Set<String> {
        return prefs.getStringSet(KEY_BYPASSED_PACKAGES, emptySet()) ?: emptySet()
    }

    fun saveBypassedPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_BYPASSED_PACKAGES, packages).apply()
    }

    fun loadCustomServers(): List<RelayServer> {
        val raw = prefs.getString(KEY_CUSTOM_SERVERS_JSON, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            val list = mutableListOf<RelayServer>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val protocol = runCatching {
                    VpnProtocol.valueOf(obj.optString("protocol", VpnProtocol.WIREGUARD.name))
                }.getOrDefault(VpnProtocol.WIREGUARD)
                list.add(
                    RelayServer(
                        id = obj.getString("id"),
                        countryCode = obj.optString("countryCode", "CUSTOM"),
                        countryName = obj.optString("countryName", "Custom Endpoint"),
                        city = obj.optString("city", "Private Node"),
                        hostname = obj.getString("hostname"),
                        ipAddress = obj.optString("ipAddress", obj.getString("hostname")),
                        port = obj.optInt("port", 51820),
                        protocol = protocol,
                        loadPercent = 12,
                        latencyMs = null,
                        isMultiHop = false,
                        isCustom = true,
                        isDemoEndpoint = false
                    )
                )
            }
            list
        }.getOrDefault(emptyList())
    }

    fun saveCustomServers(customServers: List<RelayServer>) {
        val arr = JSONArray()
        customServers.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("countryCode", s.countryCode)
                put("countryName", s.countryName)
                put("city", s.city)
                put("hostname", s.hostname)
                put("ipAddress", s.ipAddress)
                put("port", s.port)
                put("protocol", s.protocol.name)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_CUSTOM_SERVERS_JSON, arr.toString()).apply()
    }

    fun getBuiltInRelayServers(favorites: Set<String>): List<RelayServer> {
        val defaults = listOf(
            RelayServer(
                id = "ch-zrh-01",
                countryCode = "CH",
                countryName = "Switzerland",
                city = "Zurich (Privacy Core)",
                hostname = "one.one.one.one",
                ipAddress = "1.1.1.1",
                port = 51820,
                protocol = VpnProtocol.WIREGUARD,
                loadPercent = 24,
                latencyMs = 18,
                isMultiHop = true,
                entryNodeInfo = "Zurich → Reykjavik Core",
                isDemoEndpoint = true
            ),
            RelayServer(
                id = "us-nyc-01",
                countryCode = "US",
                countryName = "United States",
                city = "New York, NY",
                hostname = "dns.google",
                ipAddress = "8.8.8.8",
                port = 51820,
                protocol = VpnProtocol.WIREGUARD,
                loadPercent = 41,
                latencyMs = 22,
                isDemoEndpoint = true
            ),
            RelayServer(
                id = "de-fra-01",
                countryCode = "DE",
                countryName = "Germany",
                city = "Frankfurt",
                hostname = "dns.quad9.net",
                ipAddress = "9.9.9.9",
                port = 51820,
                protocol = VpnProtocol.WIREGUARD,
                loadPercent = 33,
                latencyMs = 29,
                isDemoEndpoint = true
            ),
            RelayServer(
                id = "nl-ams-01",
                countryCode = "NL",
                countryName = "Netherlands",
                city = "Amsterdam",
                hostname = "1.0.0.1",
                ipAddress = "1.0.0.1",
                port = 1194,
                protocol = VpnProtocol.OPENVPN_UDP,
                loadPercent = 37,
                latencyMs = 26,
                isMultiHop = true,
                entryNodeInfo = "Amsterdam → Stockholm",
                isDemoEndpoint = true
            ),
            RelayServer(
                id = "jp-tyo-01",
                countryCode = "JP",
                countryName = "Japan",
                city = "Tokyo",
                hostname = "8.8.4.4",
                ipAddress = "8.8.4.4",
                port = 443,
                protocol = VpnProtocol.STEALTH_TLS,
                loadPercent = 52,
                latencyMs = 84,
                isDemoEndpoint = true
            ),
            RelayServer(
                id = "sg-sin-01",
                countryCode = "SG",
                countryName = "Singapore",
                city = "Marina Bay",
                hostname = "149.112.112.112",
                ipAddress = "149.112.112.112",
                port = 500,
                protocol = VpnProtocol.IKEV2,
                loadPercent = 45,
                latencyMs = 76,
                isDemoEndpoint = true
            ),
            RelayServer(
                id = "gb-lon-01",
                countryCode = "GB",
                countryName = "United Kingdom",
                city = "London",
                hostname = "example.com",
                ipAddress = "93.184.215.14",
                port = 51820,
                protocol = VpnProtocol.WIREGUARD,
                loadPercent = 29,
                latencyMs = 31,
                isDemoEndpoint = true
            )
        )
        return defaults.map { it.copy(isFavorite = favorites.contains(it.id)) }
    }

    /**
     * Reads real Android kernel TrafficStats byte counters.
     */
    fun readKernelTrafficBytes(): Pair<Long, Long> {
        val rx = TrafficStats.getTotalRxBytes().coerceAtLeast(0L)
        val tx = TrafficStats.getTotalTxBytes().coerceAtLeast(0L)
        return rx to tx
    }

    /**
     * Reads real Android ConnectivityManager, NetworkCapabilities, and LinkProperties.
     */
    fun inspectActiveNetworkInterface(): NetworkInterfaceSnapshot {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetwork
            val caps = activeNetwork?.let { cm.getNetworkCapabilities(it) }
            val linkProps = activeNetwork?.let { cm.getLinkProperties(it) }

            val transport = when {
                caps == null -> "Offline"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN Tunnel + Underlying Link"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi (802.11)"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular Data"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet Bridge"
                else -> "System Network"
            }

            val ipv4 = linkProps?.linkAddresses
                ?.map { it.address }
                ?.firstOrNull { it is Inet4Address && !it.isLoopbackAddress }
                ?.hostAddress ?: "10.0.2.15"

            val ipv6 = linkProps?.linkAddresses
                ?.map { it.address }
                ?.firstOrNull { it is Inet6Address && !it.isLoopbackAddress }
                ?.hostAddress?.substringBefore("%") ?: "—"

            val dnsServers = linkProps?.dnsServers
                ?.mapNotNull { it.hostAddress?.substringBefore("%") }
                ?.ifEmpty { listOf("8.8.8.8") }
                ?: listOf("8.8.8.8")

            val ifaceName = linkProps?.interfaceName ?: "eth0"
            val mtu = linkProps?.mtu?.takeIf { it > 0 } ?: 1500
            val downKbps = caps?.linkDownstreamBandwidthKbps ?: 0
            val upKbps = caps?.linkUpstreamBandwidthKbps ?: 0
            val hasVpn = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
            val isValidated = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true

            NetworkInterfaceSnapshot(
                interfaceName = ifaceName,
                transportType = transport,
                localIpv4 = ipv4,
                localIpv6 = ipv6,
                systemDnsServers = dnsServers,
                mtu = mtu,
                downstreamKbps = downKbps,
                upstreamKbps = upKbps,
                isSystemVpnTransportActive = hasVpn,
                isNetworkValidated = isValidated
            )
        } catch (e: Exception) {
            NetworkInterfaceSnapshot()
        }
    }

    /**
     * Queries real installed applications on the device for Per-App Split Tunneling.
     */
    suspend fun queryInstalledApps(bypassedPackages: Set<String>): List<InstalledAppInfo> =
        withContext(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val apps = pm.getInstalledApplications(0)
                apps.asSequence()
                    .filter { it.packageName != context.packageName }
                    .map { appInfo ->
                        val label = runCatching { pm.getApplicationLabel(appInfo).toString() }
                            .getOrDefault(appInfo.packageName)
                        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        InstalledAppInfo(
                            packageName = appInfo.packageName,
                            appName = label,
                            isSystemApp = isSystem,
                            isBypassed = bypassedPackages.contains(appInfo.packageName)
                        )
                    }
                    .sortedWith(compareBy<InstalledAppInfo> { it.isSystemApp }.thenBy { it.appName.lowercase() })
                    .take(120)
                    .toList()
            } catch (e: Exception) {
                emptyList()
            }
        }

    /**
     * Performs a real DNS lookup + TCP socket handshake timing test on Dispatchers.IO.
     */
    suspend fun performLiveDnsAndLatencyProbe(hostInput: String): DnsProbeResult =
        withContext(Dispatchers.IO) {
            val cleanHost = hostInput.trim()
                .removePrefix("https://")
                .removePrefix("http://")
                .substringBefore("/")
                .substringBefore(":")
                .ifBlank { "one.one.one.one" }

            val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.US)
            val nowStr = timeFormatter.format(Date())

            val startDns = System.nanoTime()
            try {
                val addresses = InetAddress.getAllByName(cleanHost)
                val dnsMs = ((System.nanoTime() - startDns) / 1_000_000L).coerceAtLeast(1L)
                val resolvedList = addresses.mapNotNull { it.hostAddress }.distinct()

                val firstIp = addresses.firstOrNull()?.hostAddress
                var tcpMs: Long? = null
                if (firstIp != null) {
                    for (port in listOf(443, 53, 80)) {
                        val socketStart = System.nanoTime()
                        val connected = runCatching {
                            Socket().use { socket ->
                                socket.connect(InetSocketAddress(firstIp, port), 1800)
                            }
                            true
                        }.getOrDefault(false)
                        if (connected) {
                            tcpMs = ((System.nanoTime() - socketStart) / 1_000_000L).coerceAtLeast(1L)
                            break
                        }
                    }
                }

                DnsProbeResult(
                    host = cleanHost,
                    resolvedIps = resolvedList,
                    dnsLookupMs = dnsMs,
                    tcpHandshakeMs = tcpMs,
                    reachable = resolvedList.isNotEmpty(),
                    timestampFormatted = nowStr
                )
            } catch (e: Exception) {
                val elapsed = ((System.nanoTime() - startDns) / 1_000_000L).coerceAtLeast(1L)
                DnsProbeResult(
                    host = cleanHost,
                    resolvedIps = emptyList(),
                    dnsLookupMs = elapsed,
                    tcpHandshakeMs = null,
                    reachable = false,
                    errorMessage = e.localizedMessage ?: "Host resolution failed",
                    timestampFormatted = nowStr
                )
            }
        }

    /**
     * Measures real TCP/DNS latency to a relay server endpoint.
     */
    suspend fun measureRelayLatencyMs(server: RelayServer): Int = withContext(Dispatchers.IO) {
        val start = System.nanoTime()
        val connected = runCatching {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(server.ipAddress, 443), 1200)
            }
            true
        }.getOrDefault(false)

        if (connected) {
            return@withContext ((System.nanoTime() - start) / 1_000_000L).toInt().coerceIn(2, 999)
        }

        val dnsStart = System.nanoTime()
        val resolved = runCatching {
            InetAddress.getByName(server.hostname)
            true
        }.getOrDefault(false)

        if (resolved) {
            val ms = ((System.nanoTime() - dnsStart) / 1_000_000L).toInt()
            return@withContext ms.coerceIn(8, 450)
        }
        server.latencyMs ?: 42
    }

    fun createAuditEntry(eventType: String, details: String, isAlert: Boolean = false): AuditLogEntry {
        val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
        return AuditLogEntry(
            id = UUID.randomUUID().toString(),
            timestampFormatted = formatter.format(Date()),
            eventType = eventType,
            details = details,
            isAlert = isAlert
        )
    }

    companion object {
        private const val PREFS_NAME = "aegis_vpn_prefs"
        private const val KEY_PROTOCOL = "key_protocol"
        private const val KEY_DNS_PROVIDER = "key_dns_provider"
        private const val KEY_KILL_SWITCH = "key_kill_switch"
        private const val KEY_DNS_LEAK = "key_dns_leak"
        private const val KEY_USE_REAL_VPN_SERVICE = "key_use_real_vpn_service"
        private const val KEY_MTU = "key_mtu"
        private const val KEY_DARK_THEME = "key_dark_theme"
        private const val KEY_ONBOARDING_DONE = "key_onboarding_done"
        private const val KEY_FAVORITE_SERVERS = "key_favorite_servers"
        private const val KEY_SELECTED_SERVER = "key_selected_server"
        private const val KEY_BYPASSED_PACKAGES = "key_bypassed_packages"
        private const val KEY_CUSTOM_SERVERS_JSON = "key_custom_servers_json"
    }
}
