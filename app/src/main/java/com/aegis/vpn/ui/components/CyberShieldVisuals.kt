package com.aegis.vpn.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aegis.vpn.R
import com.aegis.vpn.model.RelayServer
import com.aegis.vpn.model.TrafficSample
import com.aegis.vpn.model.VpnStatus
import com.aegis.vpn.ui.theme.AmberAlert
import com.aegis.vpn.ui.theme.CrimsonDanger
import com.aegis.vpn.ui.theme.MintSuccess
import com.aegis.vpn.ui.theme.RoseSoftTint
import com.aegis.vpn.ui.theme.VelvetBerryBright
import com.aegis.vpn.ui.theme.VelvetBurgundyDeep
import com.aegis.vpn.ui.theme.VelvetBurgundyMid
import com.aegis.vpn.ui.theme.VelvetBurgundyPrimary
import com.aegis.vpn.ui.theme.VelvetRoseGlow
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D Isometric Pedestal & Glossy Burgundy Shield Hero Illustration (matching Left Screen of reference UI).
 */
@Composable
fun VelvetPedestalShieldHero(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pedestal_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Ambient soft rose radial glow behind pedestal
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        VelvetRoseGlow.copy(alpha = 0.28f),
                        RoseSoftTint.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = size.minDimension * 0.56f
                ),
                radius = size.minDimension * 0.56f,
                center = Offset(cx, cy)
            )

            // Tilted orbital ring
            drawOval(
                color = VelvetBerryBright.copy(alpha = 0.25f),
                topLeft = Offset(cx - 115.dp.toPx(), cy - 28.dp.toPx()),
                size = Size(230.dp.toPx(), 64.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Floating mini nodes
            drawCircle(
                color = VelvetBurgundyPrimary,
                radius = 9.dp.toPx(),
                center = Offset(cx - 88.dp.toPx(), cy - 16.dp.toPx() + floatOffset)
            )
            drawCircle(
                color = VelvetRoseGlow,
                radius = 5.dp.toPx(),
                center = Offset(cx + 86.dp.toPx(), cy + 12.dp.toPx() - floatOffset)
            )
            drawCircle(
                color = VelvetBerryBright,
                radius = 4.dp.toPx(),
                center = Offset(cx + 68.dp.toPx(), cy - 58.dp.toPx() + floatOffset)
            )

            // Bottom Isometric Pedestal Base (Lower Tier)
            val baseY = cy + 62.dp.toPx()
            val baseW = 86.dp.toPx()
            val baseH = 28.dp.toPx()
            val lowerDiamond = Path().apply {
                moveTo(cx, baseY - baseH)
                lineTo(cx + baseW, baseY)
                lineTo(cx, baseY + baseH)
                lineTo(cx - baseW, baseY)
                close()
            }
            drawPath(
                path = lowerDiamond,
                brush = Brush.linearGradient(
                    colors = listOf(VelvetBurgundyDeep, VelvetBurgundyPrimary, VelvetRoseGlow),
                    start = Offset(cx - baseW, baseY),
                    end = Offset(cx + baseW, baseY)
                )
            )

            // Upper Isometric Pedestal Platform
            val topY = cy + 48.dp.toPx()
            val topW = 72.dp.toPx()
            val topH = 23.dp.toPx()
            val upperDiamond = Path().apply {
                moveTo(cx, topY - topH)
                lineTo(cx + topW, topY)
                lineTo(cx, topY + topH)
                lineTo(cx - topW, topY)
                close()
            }
            drawPath(
                path = upperDiamond,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFDE8EF), VelvetRoseGlow, VelvetBurgundyMid),
                    start = Offset(cx - topW, topY - topH),
                    end = Offset(cx + topW, topY + topH)
                )
            )

            // 3D Glossy Shield Floating Above Pedestal
            val shieldCenterY = cy - 16.dp.toPx() + floatOffset
            val sw = 52.dp.toPx()
            val sh = 62.dp.toPx()

            val shieldPath = Path().apply {
                moveTo(cx, shieldCenterY - sh * 0.85f)
                lineTo(cx + sw, shieldCenterY - sh * 0.52f)
                lineTo(cx + sw * 0.92f, shieldCenterY + sh * 0.12f)
                quadraticTo(
                    cx + sw * 0.55f,
                    shieldCenterY + sh * 0.68f,
                    cx,
                    shieldCenterY + sh * 0.92f
                )
                quadraticTo(
                    cx - sw * 0.55f,
                    shieldCenterY + sh * 0.68f,
                    cx - sw * 0.92f,
                    shieldCenterY + sh * 0.12f
                )
                lineTo(cx - sw, shieldCenterY - sh * 0.52f)
                close()
            }

            drawPath(
                path = shieldPath,
                brush = Brush.linearGradient(
                    colors = listOf(VelvetRoseGlow, VelvetBurgundyMid, VelvetBurgundyDeep),
                    start = Offset(cx - sw, shieldCenterY - sh),
                    end = Offset(cx + sw, shieldCenterY + sh)
                )
            )

            drawPath(
                path = shieldPath,
                color = Color(0xFFFFC2D6).copy(alpha = 0.7f),
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Inner Lock Body on the 3D Shield
            val lockW = 28.dp.toPx()
            val lockH = 22.dp.toPx()
            // Shackle arc
            drawArc(
                color = Color(0xFFFFEEF4),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - 9.dp.toPx(), shieldCenterY - 20.dp.toPx()),
                size = Size(18.dp.toPx(), 18.dp.toPx()),
                style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
            )
            // Lock body rounded rect
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFF9CBD9))
                ),
                topLeft = Offset(cx - lockW / 2f, shieldCenterY - 8.dp.toPx()),
                size = Size(lockW, lockH),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            // Keyhole
            drawCircle(
                color = VelvetBurgundyDeep,
                radius = 3.dp.toPx(),
                center = Offset(cx, shieldCenterY + 1.dp.toPx())
            )
        }
    }
}

/**
 * Glossy 3D Velvet Burgundy & Rose Orb used for Security Overview & interactive VPN Connect toggle.
 */
@Composable
fun CyberShieldPowerOrb(
    vpnStatus: VpnStatus,
    onToggleConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "velvet_orb")
    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_rotation"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_pulse"
    )

    val rimColor = when (vpnStatus) {
        VpnStatus.CONNECTED -> VelvetRoseGlow
        VpnStatus.CONNECTING -> AmberAlert
        VpnStatus.DISCONNECTED -> VelvetBerryBright
    }

    Box(
        modifier = modifier.size(164.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f

            // Soft rose-burgundy outer halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        rimColor.copy(alpha = 0.36f),
                        VelvetBurgundyMid.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius * pulseScale
                ),
                radius = maxRadius,
                center = center
            )

            // Glossy 3D metallic rose ring (matching Right Screen Security Overview orb)
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        VelvetBurgundyDeep,
                        VelvetBerryBright,
                        Color(0xFFFFB8D0),
                        VelvetBurgundyPrimary,
                        VelvetBurgundyDeep
                    ),
                    center = center
                ),
                radius = maxRadius * 0.84f,
                center = center
            )

            // Rotating subtle highlight arc
            rotate(degrees = radarRotation, pivot = center) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.45f),
                    radius = maxRadius * 0.84f,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 36f), 0f)
                    )
                )
            }
        }

        val interactionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .size(112.dp)
                .minimumInteractiveComponentSize()
                .shadow(10.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            VelvetBurgundyMid,
                            VelvetBurgundyDeep
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFC4D8), VelvetBerryBright)
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, color = Color.White),
                    onClick = onToggleConnect
                )
                .testTag("vpn_connect_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = when (vpnStatus) {
                        VpnStatus.CONNECTED -> Icons.Default.VerifiedUser
                        VpnStatus.CONNECTING -> Icons.Default.Sync
                        VpnStatus.DISCONNECTED -> Icons.Default.PowerSettingsNew
                    },
                    contentDescription = stringResource(R.string.cd_connect_shield),
                    tint = Color(0xFFFFE4EE),
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (vpnStatus) {
                        VpnStatus.CONNECTED -> "PROTECTED"
                        VpnStatus.CONNECTING -> "SYNCING"
                        VpnStatus.DISCONNECTED -> "CONNECT"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun TacticalNetworkTopologyBanner(
    vpnStatus: VpnStatus,
    localIp: String,
    activeRelay: RelayServer,
    isSystemVpnActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "topology_pulse")
    val packetProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "packet_progress"
    )

    val lineColor = when (vpnStatus) {
        VpnStatus.CONNECTED -> VelvetBerryBright
        VpnStatus.CONNECTING -> AmberAlert
        VpnStatus.DISCONNECTED -> VelvetBurgundyPrimary.copy(alpha = 0.45f)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ENCRYPTED TUNNEL ROUTE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val badgeText = when {
                    vpnStatus == VpnStatus.CONNECTED && isSystemVpnActive -> "SYSTEM TUN0"
                    vpnStatus == VpnStatus.CONNECTED -> "SANDBOX DEMO TUNNEL"
                    vpnStatus == VpnStatus.CONNECTING -> "KEY EXCHANGE"
                    else -> "DIRECT ROUTE"
                }
                Surface(
                    color = RoseSoftTint,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = VelvetBurgundyPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val startPt = Offset(w * 0.12f, h * 0.70f)
                    val midPt = Offset(w * 0.50f, h * 0.22f)
                    val endPt = Offset(w * 0.88f, h * 0.70f)

                    val arcPath = Path().apply {
                        moveTo(startPt.x, startPt.y)
                        quadraticTo(midPt.x, midPt.y, endPt.x, endPt.y)
                    }

                    drawPath(
                        path = arcPath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(VelvetBurgundyPrimary, VelvetBerryBright)
                        ),
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = if (vpnStatus == VpnStatus.CONNECTED) null else PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                        )
                    )

                    if (vpnStatus != VpnStatus.DISCONNECTED) {
                        val t = packetProgress
                        val oneMinusT = 1f - t
                        val px = oneMinusT * oneMinusT * startPt.x + 2 * oneMinusT * t * midPt.x + t * t * endPt.x
                        val py = oneMinusT * oneMinusT * startPt.y + 2 * oneMinusT * t * midPt.y + t * t * endPt.y
                        drawCircle(
                            color = VelvetBerryBright,
                            radius = 5.dp.toPx(),
                            center = Offset(px, py)
                        )
                        drawCircle(
                            color = VelvetRoseGlow.copy(alpha = 0.35f),
                            radius = 10.dp.toPx(),
                            center = Offset(px, py)
                        )
                    }

                    drawCircle(color = VelvetBurgundyDeep, radius = 6.dp.toPx(), center = startPt)
                    drawCircle(color = VelvetBerryBright, radius = 6.dp.toPx(), center = endPt)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "DEVICE INTERFACE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = localIp,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${activeRelay.countryCode} • ${activeRelay.city.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = activeRelay.ipAddress,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = VelvetBurgundyPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun LiveThroughputSparkline(
    samples: List<TrafficSample>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
    ) {
        val w = size.width
        val h = size.height
        if (samples.size < 2) return@Canvas

        val maxRate = samples.maxOf { maxOf(it.rxBytesPerSec, it.txBytesPerSec) }
            .coerceAtLeast(4096L)
            .toFloat()

        val stepX = w / (samples.size - 1).coerceAtLeast(1)

        val rxPath = Path()
        val txPath = Path()

        samples.forEachIndexed { index, sample ->
            val x = index * stepX
            val rxY = h - (sample.rxBytesPerSec.toFloat() / maxRate).coerceIn(0f, 1f) * (h * 0.84f) - 4f
            val txY = h - (sample.txBytesPerSec.toFloat() / maxRate).coerceIn(0f, 1f) * (h * 0.84f) - 4f

            if (index == 0) {
                rxPath.moveTo(x, rxY)
                txPath.moveTo(x, txY)
            } else {
                rxPath.lineTo(x, rxY)
                txPath.lineTo(x, txY)
            }
        }

        drawLine(
            color = VelvetBurgundyPrimary.copy(alpha = 0.14f),
            start = Offset(0f, h - 2f),
            end = Offset(w, h - 2f),
            strokeWidth = 1.dp.toPx()
        )

        drawPath(
            path = txPath,
            color = VelvetRoseGlow,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        drawPath(
            path = rxPath,
            color = VelvetBurgundyPrimary,
            style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun LatencyBadge(latencyMs: Int?) {
    val ms = latencyMs ?: 24
    val badgeBg = when {
        ms < 50 -> RoseSoftTint
        ms < 100 -> AmberAlert.copy(alpha = 0.15f)
        else -> CrimsonDanger.copy(alpha = 0.15f)
    }
    val textColor = when {
        ms < 50 -> VelvetBurgundyPrimary
        ms < 100 -> AmberAlert
        else -> CrimsonDanger
    }
    Surface(
        color = badgeBg,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = "${ms}ms",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/**
 * Brand Hexagon-Shield Badge matching the "Proton Vault" top header icon in the reference image.
 */
@Composable
fun VelvetBrandHeaderBadge(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(VelvetBurgundyMid, VelvetBurgundyDeep)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Aegis ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "VPN",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            color = VelvetBurgundyPrimary
        )
    }
}
