package com.aegis.vpn.service

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AegisVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_CONNECT
        if (action == ACTION_DISCONNECT) {
            stopTunnel()
            stopSelf()
            return START_NOT_STICKY
        }

        val sessionName = intent?.getStringExtra(EXTRA_SESSION_NAME) ?: "Aegis VPN Tunnel"
        val mtu = intent?.getIntExtra(EXTRA_MTU, 1420) ?: 1420
        val primaryDns = intent?.getStringExtra(EXTRA_PRIMARY_DNS) ?: "1.1.1.1"
        val secondaryDns = intent?.getStringExtra(EXTRA_SECONDARY_DNS) ?: "1.0.0.1"
        val bypassedApps = intent?.getStringArrayListExtra(EXTRA_BYPASSED_APPS) ?: arrayListOf()

        establishSafeSystemTunnel(
            sessionName = sessionName,
            mtu = mtu,
            primaryDns = primaryDns,
            secondaryDns = secondaryDns,
            bypassedApps = bypassedApps
        )
        return START_STICKY
    }

    private fun establishSafeSystemTunnel(
        sessionName: String,
        mtu: Int,
        primaryDns: String,
        secondaryDns: String,
        bypassedApps: List<String>
    ) {
        try {
            stopTunnel()
            val builder = Builder()
                .setSession(sessionName)
                .setMtu(mtu.coerceIn(1280, 1500))
                .addAddress(VIRTUAL_TUNNEL_IPV4, 32)
                // Route RFC 5737 TEST-NET-2 subnet so Android activates the real system VPN
                // interface without blackholing general internet or streaming emulator traffic.
                .addRoute("198.51.100.0", 24)

            runCatching { builder.addDnsServer(primaryDns) }
            runCatching { builder.addDnsServer(secondaryDns) }

            // Always disallow our own package to prevent routing loops
            runCatching { builder.addDisallowedApplication(packageName) }
            bypassedApps.forEach { pkg ->
                if (pkg.isNotBlank() && pkg != packageName) {
                    runCatching { builder.addDisallowedApplication(pkg) }
                }
            }

            vpnInterface = builder.establish()
            _isSystemTunnelEstablished.value = vpnInterface != null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to establish system VpnService interface", e)
            _isSystemTunnelEstablished.value = false
        }
    }

    private fun stopTunnel() {
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing VPN interface", e)
        } finally {
            vpnInterface = null
            _isSystemTunnelEstablished.value = false
        }
    }

    override fun onRevoke() {
        stopTunnel()
        super.onRevoke()
    }

    override fun onDestroy() {
        stopTunnel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "AegisVpnService"
        const val VIRTUAL_TUNNEL_IPV4 = "10.88.0.2"
        const val ACTION_CONNECT = "com.aegis.vpn.action.CONNECT"
        const val ACTION_DISCONNECT = "com.aegis.vpn.action.DISCONNECT"
        const val EXTRA_SESSION_NAME = "extra_session_name"
        const val EXTRA_MTU = "extra_mtu"
        const val EXTRA_PRIMARY_DNS = "extra_primary_dns"
        const val EXTRA_SECONDARY_DNS = "extra_secondary_dns"
        const val EXTRA_BYPASSED_APPS = "extra_bypassed_apps"

        private val _isSystemTunnelEstablished = MutableStateFlow(false)
        val isSystemTunnelEstablished: StateFlow<Boolean> = _isSystemTunnelEstablished.asStateFlow()
    }
}
