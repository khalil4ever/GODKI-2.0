# Changelog

All notable changes to **Aegis VPN** are documented in this file.

## [2.0.0] - 2026-10-26 — Velvet Burgundy Major Release

### Added
- **Velvet Burgundy & Soft Rose Pearl Design System**:
  - Complete visual overhaul featuring Soft Rose Pearl surfaces (`#FAF5F7`), frosted white bento cards, rose-quartz status pills, and deep Velvet Burgundy gradients (`#3D081B` → `#8C1D43`).
  - Optional **Velvet Wine Dark Mode** switch in Settings.
- **3D Isometric Pedestal Shield Onboarding**:
  - Custom multi-layer Jetpack Compose `Canvas` hero illustration with floating orbital security nodes, 3 core security pillar cards, and one-tap initialization.
- **Bento Shield Hub & Quick Access Bar**:
  - Side-by-side **Active Tunnel** (deep burgundy gradient power card) and **Security Score** (`%` protection calculation) bento cards.
  - 4 **Quick Access** pills for *Relays*, *Split Apps*, *Live Ping*, and *Kill Switch*.
  - Deep burgundy quick-search bar and inline **Relay Endpoints** card with one-tap *Connect / Active / Switch* pills.
- **Security Overview & Curved Wave Header**:
  - Dedicated **Security Overview** view with a custom S-curve burgundy wave backdrop, overlapping 3D glossy shield power orb, **Overall Score %**, and 3 health metric cards (*DNS Leaks*, *Bypassed Split Apps*, *Latency ms*).
- **Floating Pill Bottom Navigation Dock**:
  - Raised circular **Deep Burgundy Center Power Orb** for instant VPN tunnel control from any tab.

## [1.0.0] - 2026-10-25 — Initial Android Release

### Added
- Real Android `VpnService` (`10.88.0.2/32`) with safe RFC 5737 subnet routing and **Sandbox Demo Tunnel** fallback.
- Real-time kernel `TrafficStats` RX/TX throughput sparkline and `ConnectivityManager` / `LinkProperties` interface inspection.
- Global Relay Nodes directory with live TCP/DNS latency probes and custom WireGuard, OpenVPN, IKEv2, and Stealth TLS endpoint profiles.
- Per-App Split Tunneling powered by Android `PackageManager` and `VpnService.Builder.addDisallowedApplication`.
- Live `InetAddress` DNS & TCP socket handshake probe tool and ephemeral zero-log session audit buffer.
