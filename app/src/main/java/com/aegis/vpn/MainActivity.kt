package com.aegis.vpn

import android.app.Activity
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aegis.vpn.model.VpnStatus
import com.aegis.vpn.ui.screens.OnboardingDialog
import com.aegis.vpn.ui.screens.RelayServersScreen
import com.aegis.vpn.ui.screens.SettingsScreen
import com.aegis.vpn.ui.screens.ShieldDashboardScreen
import com.aegis.vpn.ui.screens.ToolsAndSplitTunnelScreen
import com.aegis.vpn.ui.theme.AegisVpnTheme
import com.aegis.vpn.ui.theme.RoseSoftTint
import com.aegis.vpn.ui.theme.VelvetBerryBright
import com.aegis.vpn.ui.theme.VelvetBurgundyDeep
import com.aegis.vpn.ui.theme.VelvetBurgundyMid
import com.aegis.vpn.ui.theme.VelvetBurgundyPrimary
import com.aegis.vpn.viewmodel.VpnViewModel

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_SERVERS = "servers"
const val ROUTE_TOOLS = "tools"
const val ROUTE_SETTINGS = "settings"

enum class AegisDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD(ROUTE_DASHBOARD, R.string.nav_shield, Icons.Filled.Shield, Icons.Outlined.Shield),
    SERVERS(ROUTE_SERVERS, R.string.nav_relays, Icons.Filled.Public, Icons.Outlined.Public),
    TOOLS(ROUTE_TOOLS, R.string.nav_tools, Icons.Filled.Build, Icons.Outlined.Build),
    SETTINGS(ROUTE_SETTINGS, R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vpnViewModel: VpnViewModel = viewModel()
            val uiState by vpnViewModel.uiState.collectAsStateWithLifecycle()

            AegisVpnTheme(darkTheme = uiState.settings.darkTheme) {
                AegisVpnApp(vpnViewModel = vpnViewModel)
            }
        }
    }
}

@Composable
fun AegisVpnApp(vpnViewModel: VpnViewModel) {
    val uiState by vpnViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var currentRoute by rememberSaveable { mutableStateOf(ROUTE_DASHBOARD) }

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val granted = result.resultCode == Activity.RESULT_OK
        vpnViewModel.engageTunnel(systemPermissionGranted = granted)
    }

    val handleToggleVpn: () -> Unit = {
        if (uiState.vpnStatus == VpnStatus.DISCONNECTED) {
            if (uiState.settings.useRealSystemVpnService) {
                val prepareIntent = runCatching { VpnService.prepare(context) }.getOrNull()
                if (prepareIntent != null) {
                    runCatching {
                        vpnPermissionLauncher.launch(prepareIntent)
                    }.onFailure {
                        vpnViewModel.engageTunnel(systemPermissionGranted = false)
                    }
                } else {
                    vpnViewModel.engageTunnel(systemPermissionGranted = true)
                }
            } else {
                vpnViewModel.engageTunnel(systemPermissionGranted = false)
            }
        } else {
            vpnViewModel.disconnectTunnel()
        }
    }

    if (currentRoute != ROUTE_DASHBOARD) {
        BackHandler {
            currentRoute = ROUTE_DASHBOARD
        }
    }

    if (!uiState.settings.onboardingCompleted) {
        OnboardingDialog(
            onDismissAndContinue = { vpnViewModel.completeOnboarding() }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (!useNavigationRail) {
                    VelvetFloatingBottomBar(
                        currentRoute = currentRoute,
                        vpnStatus = uiState.vpnStatus,
                        onSelectRoute = { currentRoute = it },
                        onCenterPowerClick = handleToggleVpn
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        AegisDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentRoute = dest.route },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(dest.labelRes),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.testTag("nav_${dest.route}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentRoute) {
                        ROUTE_DASHBOARD -> ShieldDashboardScreen(
                            uiState = uiState,
                            onToggleVpn = handleToggleVpn,
                            onNavigateToRelays = { currentRoute = ROUTE_SERVERS },
                            onNavigateToTools = { currentRoute = ROUTE_TOOLS },
                            onSelectRelayQuick = { server -> vpnViewModel.selectServer(server) },
                            onReplayBriefing = vpnViewModel::reopenOnboarding,
                            onToggleKillSwitch = vpnViewModel::toggleKillSwitch,
                            onToggleDnsLeak = vpnViewModel::toggleDnsLeakProtection,
                            onDismissBanner = vpnViewModel::dismissBanner
                        )
                        ROUTE_SERVERS -> RelayServersScreen(
                            uiState = uiState,
                            onSearchQueryChange = vpnViewModel::updateSearchQuery,
                            onSelectCategory = vpnViewModel::selectCategory,
                            onSelectServer = { server ->
                                vpnViewModel.selectServer(server)
                                currentRoute = ROUTE_DASHBOARD
                            },
                            onToggleFavorite = vpnViewModel::toggleFavoriteServer,
                            onPingAllServers = vpnViewModel::pingAllServers,
                            onAddCustomServer = vpnViewModel::addCustomServer,
                            onDeleteCustomServer = vpnViewModel::deleteCustomServer
                        )
                        ROUTE_TOOLS -> ToolsAndSplitTunnelScreen(
                            uiState = uiState,
                            onUpdateAppSearch = vpnViewModel::updateAppSearchQuery,
                            onToggleShowSystemApps = vpnViewModel::toggleShowSystemApps,
                            onToggleAppBypass = vpnViewModel::toggleAppSplitTunnelBypass,
                            onUpdateDnsProbeInput = vpnViewModel::updateDnsProbeHostInput,
                            onRunDnsProbe = vpnViewModel::runLiveDnsProbe,
                            onClearAuditLogs = vpnViewModel::clearAuditLogs
                        )
                        ROUTE_SETTINGS -> SettingsScreen(
                            uiState = uiState,
                            onSelectProtocol = vpnViewModel::updateProtocol,
                            onSelectDnsProvider = vpnViewModel::updateDnsProvider,
                            onUpdateMtu = vpnViewModel::updateMtu,
                            onToggleUseRealVpnService = vpnViewModel::toggleUseRealSystemVpnService,
                            onToggleDarkTheme = vpnViewModel::toggleDarkTheme,
                            onReplayOnboarding = vpnViewModel::reopenOnboarding
                        )
                    }
                }
            }
        }
    }
}

/**
 * Floating Rounded Pill Bottom Navigation Dock with a Center Deep Burgundy Action Orb
 * (matching the bottom navigation bar in the reference image).
 */
@Composable
private fun VelvetFloatingBottomBar(
    currentRoute: String,
    vpnStatus: VpnStatus,
    onSelectRoute: (String) -> Unit,
    onCenterPowerClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val leftItems = listOf(AegisDestination.DASHBOARD, AegisDestination.SERVERS)
                val rightItems = listOf(AegisDestination.TOOLS, AegisDestination.SETTINGS)

                leftItems.forEach { dest ->
                    VelvetNavTabItem(
                        dest = dest,
                        selected = currentRoute == dest.route,
                        onClick = { onSelectRoute(dest.route) }
                    )
                }

                // Center Glossy Burgundy Shield Action Orb
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .minimumInteractiveComponentSize()
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (vpnStatus == VpnStatus.CONNECTED) {
                                    listOf(VelvetBerryBright, VelvetBurgundyPrimary)
                                } else {
                                    listOf(VelvetBurgundyMid, VelvetBurgundyDeep)
                                }
                            )
                        )
                        .clickable(onClick = onCenterPowerClick)
                        .testTag("bottom_bar_power_orb"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (vpnStatus == VpnStatus.CONNECTED) {
                            Icons.Default.VerifiedUser
                        } else {
                            Icons.Default.PowerSettingsNew
                        },
                        contentDescription = stringResource(R.string.cd_connect_shield),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                rightItems.forEach { dest ->
                    VelvetNavTabItem(
                        dest = dest,
                        selected = currentRoute == dest.route,
                        onClick = { onSelectRoute(dest.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VelvetNavTabItem(
    dest: AegisDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) RoseSoftTint.copy(alpha = 0.7f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("nav_${dest.route}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
            contentDescription = stringResource(dest.labelRes),
            tint = if (selected) VelvetBurgundyPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(dest.labelRes),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) VelvetBurgundyPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
