package com.aegis.vpn.model

enum class VpnStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

enum class VpnProtocol(
    val displayName: String,
    val cipherSuite: String,
    val defaultPort: Int,
    val badge: String
) {
    WIREGUARD("WireGuard", "ChaCha20-Poly1305 / Curve25519", 51820, "WG-UDP"),
    OPENVPN_UDP("OpenVPN UDP", "AES-256-GCM / SHA384", 1194, "OVPN-UDP"),
    IKEV2("IKEv2 / IPsec", "AES-256-GCM / MODP-2048", 500, "IKEv2"),
    STEALTH_TLS("Stealth TLS 1.3", "TLS_AES_256_GCM_SHA384 (Obfuscated)", 443, "TLS-443")
}

enum class DnsProvider(
    val displayName: String,
    val primaryDns: String,
    val secondaryDns: String,
    val description: String
) {
    CLOUDFLARE("Cloudflare 1.1.1.1", "1.1.1.1", "1.0.0.1", "Privacy-first DNS with DNSSEC"),
    QUAD9("Quad9 Threat Block", "9.9.9.9", "149.112.112.112", "Blocks known malware & phishing domains"),
    GOOGLE("Google Public DNS", "8.8.8.8", "8.8.4.4", "High-availability global anycast DNS"),
    ADGUARD("AdGuard Sinkhole DNS", "94.140.14.14", "94.140.15.15", "Filters ads, trackers & telemetry hosts")
}

enum class ServerCategory(val label: String) {
    ALL("All Nodes"),
    FAVORITES("Favorites"),
    LOW_LATENCY("Low Ping"),
    MULTI_HOP("Multi-Hop"),
    CUSTOM("Custom")
}

data class RelayServer(
    val id: String,
    val countryCode: String,
    val countryName: String,
    val city: String,
    val hostname: String,
    val ipAddress: String,
    val port: Int = 51820,
    val protocol: VpnProtocol = VpnProtocol.WIREGUARD,
    val loadPercent: Int = 28,
    val latencyMs: Int? = null,
    val isMultiHop: Boolean = false,
    val entryNodeInfo: String? = null,
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false,
    val isDemoEndpoint: Boolean = true
)

data class TrafficSample(
    val timestampMs: Long,
    val rxBytesPerSec: Long,
    val txBytesPerSec: Long
)

data class NetworkInterfaceSnapshot(
    val interfaceName: String = "eth0",
    val transportType: String = "Wi-Fi / Ethernet",
    val localIpv4: String = "Detecting…",
    val localIpv6: String = "—",
    val systemDnsServers: List<String> = emptyList(),
    val mtu: Int = 1500,
    val downstreamKbps: Int = 0,
    val upstreamKbps: Int = 0,
    val isSystemVpnTransportActive: Boolean = false,
    val isNetworkValidated: Boolean = true
)

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val isBypassed: Boolean
)

data class DnsProbeResult(
    val host: String,
    val resolvedIps: List<String>,
    val dnsLookupMs: Long,
    val tcpHandshakeMs: Long?,
    val reachable: Boolean,
    val errorMessage: String? = null,
    val timestampFormatted: String
)

data class AuditLogEntry(
    val id: String,
    val timestampFormatted: String,
    val eventType: String,
    val details: String,
    val isAlert: Boolean = false
)

data class VpnSettings(
    val protocol: VpnProtocol = VpnProtocol.WIREGUARD,
    val dnsProvider: DnsProvider = DnsProvider.CLOUDFLARE,
    val killSwitchEnabled: Boolean = true,
    val dnsLeakProtection: Boolean = true,
    val useRealSystemVpnService: Boolean = true,
    val mtu: Int = 1420,
    val darkTheme: Boolean = false,
    val onboardingCompleted: Boolean = false
)
