package com.aegis.vpn.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aegis.vpn.model.RelayServer
import com.aegis.vpn.model.VpnStatus
import com.aegis.vpn.service.AegisVpnService
import com.aegis.vpn.ui.components.CyberShieldPowerOrb
import com.aegis.vpn.ui.components.LiveThroughputSparkline
import com.aegis.vpn.ui.components.TacticalNetworkTopologyBanner
import com.aegis.vpn.ui.components.VelvetBrandHeaderBadge
import com.aegis.vpn.ui.theme.AmberAlert
import com.aegis.vpn.ui.theme.RoseSoftTint
import com.aegis.vpn.ui.theme.VelvetBerryBright
import com.aegis.vpn.ui.theme.VelvetBurgundyDeep
import com.aegis.vpn.ui.theme.VelvetBurgundyMid
import com.aegis.vpn.ui.theme.VelvetBurgundyPrimary
import com.aegis.vpn.ui.theme.VelvetRoseGlow
import com.aegis.vpn.viewmodel.VpnUiState
import com.aegis.vpn.viewmodel.VpnViewModel

@Composable
fun ShieldDashboardScreen(
    uiState: VpnUiState,
    onToggleVpn: () -> Unit,
    onNavigateToRelays: () -> Unit,
    onNavigateToTools: () -> Unit = {},
    onSelectRelayQuick: (RelayServer) -> Unit = {},
    onReplayBriefing: () -> Unit = {},
    onToggleKillSwitch: (Boolean) -> Unit,
    onToggleDnsLeak: (Boolean) -> Unit,
    onDismissBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSecurityOverviewSubscreen by rememberSaveable { mutableStateOf(false) }

    val securityScore = calculateSecurityScore(uiState)
    val scoreRating = when {
        securityScore >= 90 -> "Very Strong"
        securityScore >= 75 -> "Strong"
        else -> "Needs Shield"
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 680.dp
        val scrollState = rememberScrollState()

        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    VelvetHomeVaultColumn(
                        uiState = uiState,
                        securityScore = securityScore,
                        scoreRating = scoreRating,
                        onToggleVpn = onToggleVpn,
                        onNavigateToRelays = onNavigateToRelays,
                        onNavigateToTools = onNavigateToTools,
                        onOpenSecurityOverview = { showSecurityOverviewSubscreen = true },
                        onSelectRelayQuick = onSelectRelayQuick,
                        onReplayBriefing = onReplayBriefing,
                        onToggleKillSwitch = onToggleKillSwitch,
                        onDismissBanner = onDismissBanner
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    VelvetSecurityOverviewColumn(
                        uiState = uiState,
                        securityScore = securityScore,
                        scoreRating = scoreRating,
                        showBackButton = false,
                        onBack = {},
                        onToggleVpn = onToggleVpn,
                        onToggleKillSwitch = onToggleKillSwitch,
                        onToggleDnsLeak = onToggleDnsLeak,
                        onNavigateToTools = onNavigateToTools
                    )
                }
            }
        } else {
            if (showSecurityOverviewSubscreen) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    VelvetSecurityOverviewColumn(
                        uiState = uiState,
                        securityScore = securityScore,
                        scoreRating = scoreRating,
                        showBackButton = true,
                        onBack = { showSecurityOverviewSubscreen = false },
                        onToggleVpn = onToggleVpn,
                        onToggleKillSwitch = onToggleKillSwitch,
                        onToggleDnsLeak = onToggleDnsLeak,
                        onNavigateToTools = onNavigateToTools
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    VelvetHomeVaultColumn(
                        uiState = uiState,
                        securityScore = securityScore,
                        scoreRating = scoreRating,
                        onToggleVpn = onToggleVpn,
                        onNavigateToRelays = onNavigateToRelays,
                        onNavigateToTools = onNavigateToTools,
                        onOpenSecurityOverview = { showSecurityOverviewSubscreen = true },
                        onSelectRelayQuick = onSelectRelayQuick,
                        onReplayBriefing = onReplayBriefing,
                        onToggleKillSwitch = onToggleKillSwitch,
                        onDismissBanner = onDismissBanner
                    )
                }
            }
        }
    }
}

private fun calculateSecurityScore(uiState: VpnUiState): Int {
    var score = 52
    if (uiState.vpnStatus == VpnStatus.CONNECTED) score += 25
    if (uiState.settings.killSwitchEnabled) score += 10
    if (uiState.settings.dnsLeakProtection) score += 10
    return score.coerceAtMost(99)
}

/**
 * Center Screen from Reference UI: Greeting, Burgundy Search Pill, Two Bento Cards,
 * Quick Access Chips, Recent Relay Nodes List, and Live Telemetry.
 */
@Composable
private fun VelvetHomeVaultColumn(
    uiState: VpnUiState,
    securityScore: Int,
    scoreRating: String,
    onToggleVpn: () -> Unit,
    onNavigateToRelays: () -> Unit,
    onNavigateToTools: () -> Unit,
    onOpenSecurityOverview: () -> Unit,
    onSelectRelayQuick: (RelayServer) -> Unit,
    onReplayBriefing: () -> Unit,
    onToggleKillSwitch: (Boolean) -> Unit,
    onDismissBanner: () -> Unit
) {
    // Top Header Bar (Menu circle left, Aegis VPN badge center, Notification Bell with red badge right)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(42.dp)
                .clickable(onClick = onReplayBriefing),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 3.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open security briefing",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        VelvetBrandHeaderBadge()

        Box {
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clickable(onClick = onOpenSecurityOverview)
                    .testTag("open_security_overview_button"),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 3.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "Security overview",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(VelvetBerryBright),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${uiState.auditLogs.size.coerceAtMost(9)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Greeting & Status Subtitle (matching "Good morning, Brett / Your vault is secure")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = "Network Shield,",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = uiState.selectedServer.city,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = when (uiState.vpnStatus) {
                    VpnStatus.CONNECTED -> "Your encrypted tunnel is secure (${AegisVpnService.VIRTUAL_TUNNEL_IPV4})"
                    VpnStatus.CONNECTING -> "Handshaking ${uiState.settings.protocol.displayName}…"
                    VpnStatus.DISCONNECTED -> "Tap the Burgundy Tunnel card to protect traffic"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            color = if (uiState.vpnStatus == VpnStatus.CONNECTED) RoseSoftTint else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(50),
            shadowElevation = 2.dp,
            modifier = Modifier.testTag("connection_status_pill")
        ) {
            Text(
                text = when (uiState.vpnStatus) {
                    VpnStatus.CONNECTED -> "PROTECTED"
                    VpnStatus.CONNECTING -> "HANDSHAKING"
                    VpnStatus.DISCONNECTED -> "UNPROTECTED"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = VelvetBurgundyPrimary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }

    AnimatedVisibility(visible = uiState.statusBannerMessage != null) {
        uiState.statusBannerMessage?.let { msg ->
            Surface(
                color = RoseSoftTint,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clickable { onDismissBanner() }
            ) {
                Text(
                    text = msg,
                    style = MaterialTheme.typography.labelMedium,
                    color = VelvetBurgundyDeep,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Deep Burgundy Search / Relay Filter Pill Bar (matching Center Screen search bar!)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .shadow(6.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(VelvetBurgundyDeep, VelvetBurgundyPrimary, VelvetBurgundyMid)
                )
            )
            .clickable { onNavigateToRelays() }
            .padding(horizontal = 16.dp)
            .testTag("active_relay_card"),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFFF7C6D7),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Search ${uiState.servers.size} relays (${uiState.selectedServer.hostname})…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFF9DAE5)
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Filter relays",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Two Side-by-Side Hero Bento Cards (Left: Deep Burgundy Tunnel Power Card; Right: Frosted White Security Score Card)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Bento Card: Deep Burgundy Gradient (Tapping toggles VPN connection!)
        Box(
            modifier = Modifier
                .weight(1.05f)
                .height(138.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            VelvetBurgundyMid,
                            VelvetBurgundyPrimary,
                            VelvetBurgundyDeep
                        )
                    )
                )
                .clickable(onClick = onToggleVpn)
                .padding(16.dp)
                .testTag("vpn_connect_button")
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (uiState.vpnStatus == VpnStatus.CONNECTED) "Active Tunnel" else "Tap to Connect",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFADCE6)
                )
                Text(
                    text = if (uiState.vpnStatus == VpnStatus.CONNECTED) {
                        VpnViewModel.formatDuration(uiState.sessionUptimeSeconds)
                    } else {
                        "${uiState.servers.size} Nodes"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.testTag("session_timer_text")
                )
                Text(
                    text = if (uiState.vpnStatus == VpnStatus.CONNECTED) {
                        "Tap to Disconnect"
                    } else {
                        "${uiState.settings.protocol.displayName} Ready"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFF6C2D4)
                )
            }

            // 3D Glossy Cube/Lock Emblem on Right of Burgundy Bento Card
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(VelvetRoseGlow, VelvetBerryBright)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Engage VPN",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Right Bento Card: Frosted White Security Score Card (Tapping opens Security Overview!)
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(138.dp)
                .clickable(onClick = onOpenSecurityOverview)
                .testTag("security_score_bento_card"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 5.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Security Score",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open Security Overview",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "$securityScore%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = VelvetBurgundyPrimary
                )

                Column {
                    Text(
                        text = scoreRating,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { securityScore / 100f },
                        color = VelvetBurgundyPrimary,
                        trackColor = RoseSoftTint,
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Quick Access Section Header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Quick Access",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "View All",
            style = MaterialTheme.typography.labelMedium,
            color = VelvetBurgundyMid,
            modifier = Modifier.clickable(onClick = onOpenSecurityOverview)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 4 Quick Access Pills Row (matching Logins 98, Cards 16, Notes 12, IDs 6 in reference image!)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickAccessPill(
            icon = Icons.Default.Public,
            title = "Relays",
            countText = "${uiState.servers.size}",
            onClick = onNavigateToRelays
        )
        QuickAccessPill(
            icon = Icons.Default.Apps,
            title = "Split Apps",
            countText = "${uiState.bypassedPackages.size}",
            onClick = onNavigateToTools
        )
        QuickAccessPill(
            icon = Icons.Default.NetworkPing,
            title = "Ping",
            countText = "${uiState.selectedServer.latencyMs ?: 18}ms",
            onClick = onNavigateToTools
        )
        QuickAccessPill(
            icon = Icons.Default.GppGood,
            title = "Kill Switch",
            countText = if (uiState.settings.killSwitchEnabled) "ON" else "OFF",
            onClick = { onToggleKillSwitch(!uiState.settings.killSwitchEnabled) }
        )
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Recent / Top Relay Nodes Section (matching "Recent Items" list in Center Screen!)
    Text(
        text = "Relay Endpoints",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(modifier = Modifier.height(10.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            val topRelays = uiState.servers.take(4)
            topRelays.forEachIndexed { index, server ->
                val isCurrent = server.id == uiState.selectedServerId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectRelayQuick(server) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isCurrent) {
                                        Brush.linearGradient(
                                            colors = listOf(VelvetBurgundyMid, VelvetBurgundyDeep)
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(RoseSoftTint, RoseSoftTint)
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = server.countryCode,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color.White else VelvetBurgundyPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "${server.city}, ${server.countryName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${server.hostname} • ${server.protocol.badge} • ${server.latencyMs ?: 22}ms",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Soft Rose Pill ("Connect" / "Active", matching "Login" pill in screenshot!)
                        Surface(
                            color = if (isCurrent && uiState.vpnStatus == VpnStatus.CONNECTED) {
                                VelvetBurgundyPrimary
                            } else {
                                RoseSoftTint
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable {
                                onSelectRelayQuick(server)
                                if (uiState.vpnStatus != VpnStatus.CONNECTED) {
                                    onToggleVpn()
                                }
                            }
                        ) {
                            Text(
                                text = when {
                                    isCurrent && uiState.vpnStatus == VpnStatus.CONNECTED -> "Active"
                                    isCurrent -> "Connect"
                                    else -> "Switch"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent && uiState.vpnStatus == VpnStatus.CONNECTED) {
                                    Color.White
                                } else {
                                    VelvetBurgundyPrimary
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Relay options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onNavigateToRelays() }
                        )
                    }
                }

                if (index < topRelays.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Encrypted Tunnel Route Topology Card
    TacticalNetworkTopologyBanner(
        vpnStatus = uiState.vpnStatus,
        localIp = uiState.networkInterface.localIpv4,
        activeRelay = uiState.selectedServer,
        isSystemVpnActive = uiState.isSystemVpnActive
    )

    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
private fun QuickAccessPill(
    icon: ImageVector,
    title: String,
    countText: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(RoseSoftTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VelvetBurgundyPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = countText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Right Screen from Reference UI: Deep Burgundy Curved Wave Header, Overlapping 3D Glossy Shield Orb,
 * Overall Score %, 3 Metric Cards (DNS Leaks, Split Apps, Live Ping), and Stacked Security Recommendations!
 */
@Composable
private fun VelvetSecurityOverviewColumn(
    uiState: VpnUiState,
    securityScore: Int,
    scoreRating: String,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onToggleVpn: () -> Unit,
    onToggleKillSwitch: (Boolean) -> Unit,
    onToggleDnsLeak: (Boolean) -> Unit,
    onNavigateToTools: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Curved Burgundy Wave Header + Overlapping 3D Orb & Overall Score
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
        ) {
            // Deep Burgundy Wave Canvas Background
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(215.dp)
            ) {
                val w = size.width
                val h = size.height

                val wavePath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h * 0.66f)
                    cubicTo(
                        w * 0.72f, h * 0.56f,
                        w * 0.35f, h * 1.05f,
                        0f, h * 0.78f
                    )
                    close()
                }

                drawPath(
                    path = wavePath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            VelvetBurgundyDeep,
                            VelvetBurgundyPrimary,
                            VelvetBerryBright
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )

                // Subtle translucent highlight wave
                val highlightPath = Path().apply {
                    moveTo(w * 0.35f, 0f)
                    quadraticTo(w * 0.65f, h * 0.52f, w, h * 0.42f)
                    lineTo(w, 0f)
                    close()
                }
                drawPath(
                    path = highlightPath,
                    color = Color.White.copy(alpha = 0.08f)
                )
            }

            // Header Top Controls & Title inside Burgundy Wave
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showBackButton) {
                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .clickable(onClick = onBack),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.92f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to vault",
                                    tint = VelvetBurgundyDeep,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.size(38.dp))
                    }

                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clickable(onClick = onNavigateToTools),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.92f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "More tools",
                                tint = VelvetBurgundyDeep,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Security Overview",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Monitor your tunnel & network health",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFADCE6)
                )
            }

            // Overlapping Glossy 3D Velvet Orb (Left) + Overall Score 96% (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CyberShieldPowerOrb(
                    vpnStatus = uiState.vpnStatus,
                    onToggleConnect = onToggleVpn
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(y = 14.dp)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = "Overall Score",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$securityScore%",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = VelvetBurgundyDeep
                    )
                    Surface(
                        color = RoseSoftTint,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = scoreRating,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VelvetBurgundyPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3 Metric Cards Row (matching "Breaches 0", "Weak 2", "2FA Enabled 15" in Right Screen!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OverviewStatCard(
                label = "DNS Leaks",
                value = if (uiState.settings.dnsLeakProtection) "0" else "1",
                subtitle = if (uiState.settings.dnsLeakProtection) "No leaks found" else "Enable shield",
                badgeText = "✓",
                modifier = Modifier.weight(1f)
            )
            OverviewStatCard(
                label = "Bypassed",
                value = "${uiState.bypassedPackages.size}",
                subtitle = "Split apps",
                badgeText = "!",
                modifier = Modifier.weight(1f)
            )
            OverviewStatCard(
                label = "Latency",
                value = "${uiState.selectedServer.latencyMs ?: 18}",
                subtitle = "ms (${uiState.selectedServer.countryCode})",
                badgeText = "✓",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Security Recommendations & Live Telemetry Section
        Column(modifier = Modifier.padding(horizontal = 18.dp)) {
            Text(
                text = "Security Controls & Live Telemetry",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stacked Recommendation / Control Rows (matching the 4 stacked rows in Right Screen!)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    // Row 1: Kill Switch Guard Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            VelvetHexIconBadge(icon = Icons.Default.GppGood)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Kill Switch Guard",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Block unencrypted packets if tunnel drops",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = uiState.settings.killSwitchEnabled,
                            onCheckedChange = onToggleKillSwitch,
                            modifier = Modifier.testTag("kill_switch_toggle")
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    )

                    // Row 2: DNS Leak & Tracker Protection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            VelvetHexIconBadge(icon = Icons.Default.Dns)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "DNS Leak & Tracker Shield",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Routing queries via ${uiState.settings.dnsProvider.displayName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = uiState.settings.dnsLeakProtection,
                            onCheckedChange = onToggleDnsLeak,
                            modifier = Modifier.testTag("dns_leak_toggle")
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    )

                    // Row 3: Live Kernel TrafficStats Throughput
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                VelvetHexIconBadge(icon = Icons.Default.Speed)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Live Kernel TrafficStats",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "RX: ${VpnViewModel.formatSpeedPerSec(uiState.liveRxBytesPerSec)} • TX: ${VpnViewModel.formatSpeedPerSec(uiState.liveTxBytesPerSec)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = VelvetBurgundyPrimary
                                    )
                                }
                            }
                            Text(
                                text = VpnViewModel.formatBytes(uiState.sessionTotalRxBytes + uiState.sessionTotalTxBytes),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = VelvetBerryBright
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LiveThroughputSparkline(samples = uiState.trafficHistory)
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    )

                    // Row 4: Device LinkProperties & DNS Probe Quick Action
                    val net = uiState.networkInterface
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToTools)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            VelvetHexIconBadge(icon = Icons.Default.VerifiedUser)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Interface ${net.interfaceName.uppercase()} (${net.localIpv4})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${net.transportType} • System DNS ${net.systemDnsServers.firstOrNull() ?: "8.8.8.8"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OverviewStatCard(
    label: String,
    value: String,
    subtitle: String,
    badgeText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(RoseSoftTint),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = VelvetBurgundyPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VelvetHexIconBadge(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(VelvetBurgundyMid, VelvetBurgundyDeep)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}
