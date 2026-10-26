package com.aegis.vpn.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aegis.vpn.model.AuditLogEntry
import com.aegis.vpn.model.DnsProbeResult
import com.aegis.vpn.model.InstalledAppInfo
import com.aegis.vpn.ui.theme.AmberAlert
import com.aegis.vpn.ui.theme.CrimsonDanger
import com.aegis.vpn.ui.theme.CyberEmerald
import com.aegis.vpn.ui.theme.ElectricCyan
import com.aegis.vpn.viewmodel.VpnUiState

@Composable
fun ToolsAndSplitTunnelScreen(
    uiState: VpnUiState,
    onUpdateAppSearch: (String) -> Unit,
    onToggleShowSystemApps: (Boolean) -> Unit,
    onToggleAppBypass: (String) -> Unit,
    onUpdateDnsProbeInput: (String) -> Unit,
    onRunDnsProbe: (String) -> Unit,
    onClearAuditLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Split Tunnel", "DNS & Ping Probe", "Audit Logs")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "SECURITY & NETWORK TOOLS",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Per-App Routing, Live DNS/TCP Diagnostics & Session Buffer",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tools_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTabIndex) {
            0 -> SplitTunnelingSection(
                uiState = uiState,
                onUpdateAppSearch = onUpdateAppSearch,
                onToggleShowSystemApps = onToggleShowSystemApps,
                onToggleAppBypass = onToggleAppBypass
            )
            1 -> LiveDnsProbeSection(
                uiState = uiState,
                onUpdateDnsProbeInput = onUpdateDnsProbeInput,
                onRunDnsProbe = onRunDnsProbe
            )
            2 -> AuditLogSection(
                logs = uiState.auditLogs,
                onClearLogs = onClearAuditLogs
            )
        }
    }
}

@Composable
private fun SplitTunnelingSection(
    uiState: VpnUiState,
    onUpdateAppSearch: (String) -> Unit,
    onToggleShowSystemApps: (Boolean) -> Unit,
    onToggleAppBypass: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${uiState.bypassedPackages.size} Apps Bypassing VPN Tunnel",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Apps toggled ON use direct connection (VpnService.Builder.addDisallowedApplication)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilterChip(
                selected = uiState.showSystemAppsInSplitTunnel,
                onClick = { onToggleShowSystemApps(!uiState.showSystemAppsInSplitTunnel) },
                label = { Text("System Apps", style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.testTag("toggle_system_apps_chip")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = uiState.appSearchQuery,
            onValueChange = onUpdateAppSearch,
            placeholder = { Text("Filter installed Android packages…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("split_tunnel_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.isLoadingApps) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Inspecting installed packages via PackageManager…",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val apps = uiState.filteredApps
            if (apps.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Matching Apps Found",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Enable 'System Apps' chip above to view pre-installed system packages.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(apps, key = { it.packageName }) { app ->
                        InstalledAppRow(
                            app = app,
                            onToggleBypass = { onToggleAppBypass(app.packageName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstalledAppRow(
    app: InstalledAppInfo,
    onToggleBypass: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = app.packageName + if (app.isSystemApp) " • System" else " • User App",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = app.isBypassed,
                    onCheckedChange = { onToggleBypass() },
                    modifier = Modifier.testTag("bypass_switch_${app.packageName}")
                )
                Text(
                    text = if (app.isBypassed) "BYPASS VPN" else "IN TUNNEL",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (app.isBypassed) AmberAlert else CyberEmerald
                )
            }
        }
    }
}

@Composable
private fun LiveDnsProbeSection(
    uiState: VpnUiState,
    onUpdateDnsProbeInput: (String) -> Unit,
    onRunDnsProbe: (String) -> Unit
) {
    val presetHosts = listOf("one.one.one.one", "dns.google", "dns.quad9.net", "example.com")

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "LIVE DNS & TCP SOCKET HANDSHAKE PROBE",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.dnsProbeHostInput,
                        onValueChange = onUpdateDnsProbeInput,
                        label = { Text("Target Hostname or IP") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dns_probe_host_input")
                    )
                    Button(
                        onClick = { onRunDnsProbe(uiState.dnsProbeHostInput) },
                        enabled = !uiState.isRunningDnsProbe,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("run_dns_probe_button")
                    ) {
                        if (uiState.isRunningDnsProbe) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Run DNS probe")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Probe")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetHosts.forEach { preset ->
                        FilterChip(
                            selected = uiState.dnsProbeHostInput == preset,
                            onClick = {
                                onUpdateDnsProbeInput(preset)
                                onRunDnsProbe(preset)
                            },
                            label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.dnsProbeHistory.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ready for Live Network Probe",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Tap 'Probe' or select a preset host above to measure real Android InetAddress DNS resolution and TCP socket handshake latency.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(uiState.dnsProbeHistory) { result ->
                    DnsProbeResultCard(result = result)
                }
            }
        }
    }
}

@Composable
private fun DnsProbeResultCard(result: DnsProbeResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (result.reachable) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = if (result.reachable) CyberEmerald else CrimsonDanger,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = result.host,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = result.timestampFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (result.reachable) {
                Text(
                    text = "Resolved Records: ${result.resolvedIps.joinToString(", ")}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "DNS Lookup: ${result.dnsLookupMs}ms • TCP Handshake: ${result.tcpHandshakeMs?.let { "${it}ms" } ?: "Filtered/UDP"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = result.errorMessage ?: "Unreachable",
                    style = MaterialTheme.typography.labelMedium,
                    color = CrimsonDanger
                )
            }
        }
    }
}

@Composable
private fun AuditLogSection(
    logs: List<AuditLogEntry>,
    onClearLogs: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "EPHEMERAL SESSION AUDIT LOGS (${logs.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = onClearLogs,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("clear_logs_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear audit logs",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Purge RAM", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(logs, key = { it.id }) { entry ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = entry.eventType,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (entry.isAlert) AmberAlert else CyberEmerald,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = entry.timestampFormatted,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = entry.details,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
