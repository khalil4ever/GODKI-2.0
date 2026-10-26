# Aegis VPN `v2.0.0` — Velvet Burgundy Major Release

![Version](https://img.shields.io/badge/version-2.0.0-8C1D43) ![Platform](https://img.shields.io/badge/platform-Android%20SDK%2036-3D081B) ![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-C92C61)

**Aegis VPN** is a modern Android VPN client, live network telemetry monitor, per-app split-tunneling manager, and DNS/latency diagnostics toolkit built with Kotlin and Jetpack Compose.

---

## What's New in `v2.0.0` (Major Update)
- **Velvet Burgundy & Soft Rose Pearl UI**: Redesigned across all screens with frosted white cards, deep burgundy gradients, rose-quartz pills, and an optional **Velvet Wine Dark Mode**.
- **3D Isometric Pedestal Shield Onboarding**: Custom `Canvas` 3D shield-and-lock pedestal illustration with floating orbital nodes and security pillar cards.
- **Bento Shield Dashboard**:
  - **Active Tunnel Bento Card**: Deep burgundy gradient card with live session timer and one-tap tunnel engagement.
  - **Security Score Bento Card**: Real-time protection score (`%`) with instant transition to the **Security Overview** screen.
  - **Quick Access Pills & Inline Relay Switcher**: Fast shortcuts for *Relays*, *Split Apps*, *Ping*, and *Kill Switch*.
- **Security Overview Screen**: Deep burgundy curved wave header, 3D glossy shield power orb, health metric cards (*DNS Leaks*, *Bypassed Apps*, *Latency*), and live `TrafficStats` sparkline.
- **Floating Pill Bottom Dock**: Features a raised center **Deep Burgundy Power Orb** for one-tap connect/disconnect from anywhere in the app.

---

## Core Capabilities
- **Android `VpnService` & Sandbox Demo Tunnel**: Supports both real `android.net.VpnService` (`10.88.0.2/32`) and Sandbox Demo Tunnel mode.
- **Live Kernel `TrafficStats` & `LinkProperties`**: Real-time RX/TX throughput sparkline and active network interface inspection (`localIpv4`, `MTU`, system DNS).
- **Per-App Split Tunneling**: Queries installed apps via `PackageManager` and excludes selected packages from the VPN tunnel (`addDisallowedApplication`).
- **Live DNS & TCP Handshake Probe**: Resolves hostnames via `InetAddress` and measures live TCP socket handshake latency.
- **Custom Server Profiles**: Add custom WireGuard, OpenVPN UDP, IKEv2/IPsec, or Stealth TLS 1.3 endpoints.
