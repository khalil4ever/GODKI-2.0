# Aegis VPN

Aegis VPN is an Android VPN client, live network telemetry monitor, per-app split-tunneling manager, and DNS/latency diagnostics toolkit built with Kotlin and Jetpack Compose (Material 3).

## Key Features
- **Shield Dashboard**: Interactive Cyber Shield power orb, real Android `VpnService` (`10.88.0.2/32`) and Sandbox Demo Tunnel modes, live kernel `TrafficStats` RX/TX sparkline, and real `ConnectivityManager` / `LinkProperties` interface inspector.
- **Global Relay Nodes & Custom Profiles**: Filterable relay directory with real TCP/DNS latency sweeps and custom WireGuard / OpenVPN / IKEv2 / Stealth TLS endpoint profiles.
- **Per-App Split Tunneling & Live DNS Probe**: Inspect installed Android packages via `PackageManager` to configure per-app VPN bypass rules (`VpnService.Builder.addDisallowedApplication`), run real `InetAddress` DNS + TCP socket handshake timing probes, and inspect ephemeral zero-log session audit buffers.
- **Settings & Zero-Log Privacy**: Switch VPN protocols, encrypted DNS resolvers (Cloudflare, Quad9, Google, AdGuard), packet MTU (`1280–1500`), Kill Switch Guard, and Obsidian Dark / Daylight High-Contrast themes.
