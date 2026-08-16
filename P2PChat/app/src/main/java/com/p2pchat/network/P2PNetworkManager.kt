package com.p2pchat.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.*
import java.net.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class P2PNetworkManager(private val context: Context) {
    
    companion object {
        private const val TAG = "P2PNetworkManager"
        const val SERVICE_TYPE = "_p2pchat._tcp."
        const val SERVER_PORT = 8888
        const val CONNECTION_CODE_LENGTH = 6
    }
    
    private var serverSocket: ServerSocket? = null
    private var isServerRunning = false
    private val clients = ConcurrentHashMap<String, Socket>()
    private val messageListeners = mutableListOf<(ByteArray) -> Unit>()
    
    val connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val discoveredPeers = MutableStateFlow<List<PeerInfo>>(emptyList())
    
    private var nsdManager: NsdManager? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    
    enum class ConnectionStatus {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        SEARCHING
    }
    
    data class PeerInfo(
        val host: String,
        val port: Int,
        val name: String
    )
    
    fun generateConnectionCode(): String {
        return (100000..999999).random().toString()
    }
    
    fun startServer(onMessageReceived: (ByteArray) -> Unit) {
        if (isServerRunning) return
        
        Thread {
            try {
                serverSocket = ServerSocket(SERVER_PORT)
                isServerRunning = true
                connectionStatus.value = ConnectionStatus.CONNECTED
                
                messageListeners.add(onMessageReceived)
                
                Log.d(TAG, "Server started on port $SERVER_PORT")
                
                while (isServerRunning) {
                    try {
                        val clientSocket = serverSocket?.accept()
                        clientSocket?.let { socket ->
                            val clientId = UUID.randomUUID().toString()
                            clients[clientId] = socket
                            Log.d(TAG, "Client connected: $clientId")
                            
                            Thread {
                                handleIncomingMessages(socket, onMessageReceived)
                            }.start()
                        }
                    } catch (e: Exception) {
                        if (isServerRunning) {
                            Log.e(TAG, "Error accepting client", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Server error", e)
                connectionStatus.value = ConnectionStatus.DISCONNECTED
            }
        }.start()
        
        registerService()
    }
    
  private fun handleIncomingMessages(socket: Socket, onMessageReceived: (ByteArray) -> Unit) {
    try {
        val inputStream = socket.getInputStream()
        val buffer = ByteArray(4096)
        var bytesRead: Int // 1. Değişkeni burada var ile tanımlıyoruz

        // 2. Döngüden önceki hatalı "if (bytesRead > 0)" satırını SİLİNİZ.

        while (socket.isConnected && (inputStream.read(buffer).also { bytesRead = it }) != -1) {
            if (bytesRead > 0) {
                val data = buffer.copyOf(bytesRead)
                onMessageReceived(data)
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error reading from socket", e)
    }
}
    
    fun connectToPeer(host: String, port: Int = SERVER_PORT, onMessageReceived: (ByteArray) -> Unit): Boolean {
        return try {
            connectionStatus.value = ConnectionStatus.CONNECTING
            
            val socket = Socket(host, port)
            val clientId = UUID.randomUUID().toString()
            clients[clientId] = socket
            
            messageListeners.add(onMessageReceived)
            
            Thread {
                handleIncomingMessages(socket, onMessageReceived)
            }.start()
            
            connectionStatus.value = ConnectionStatus.CONNECTED
            Log.d(TAG, "Connected to peer: $host:$port")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed", e)
            connectionStatus.value = ConnectionStatus.DISCONNECTED
            false
        }
    }
    
    fun sendMessage(data: ByteArray): Boolean {
        return try {
            clients.values.forEach { socket ->
                try {
                    val outputStream = socket.getOutputStream()
                    outputStream.write(data)
                    outputStream.flush()
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending message", e)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Send failed", e)
            false
        }
    }
    
    fun sendFile(fileUri: String, recipientHost: String): Boolean {
        return try {
            val file = File(fileUri)
            if (!file.exists()) return false
            
            val socket = Socket(recipientHost, SERVER_PORT)
            val outputStream = DataOutputStream(socket.getOutputStream())
            
            // Send file header
            outputStream.writeUTF("FILE_TRANSFER")
            outputStream.writeUTF(file.name)
            outputStream.writeLong(file.length())
            
            // Send file content
            val inputStream = FileInputStream(file)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            
            inputStream.close()
            outputStream.close()
            socket.close()
            
            Log.d(TAG, "File sent successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "File transfer failed", e)
            false
        }
    }
    
    private fun registerService() {
        nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "P2PChat_${generateConnectionCode()}"
            serviceType = SERVICE_TYPE
            port = SERVER_PORT
        }
        
        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) {
                Log.d(TAG, "Service registered: ${info.serviceName}")
            }
            
            override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Registration failed: $errorCode")
            }
            
            override fun onServiceUnregistered(info: NsdServiceInfo) {
                Log.d(TAG, "Service unregistered")
            }
            
            override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Unregistration failed: $errorCode")
            }
        }
        
        try {
            nsdManager?.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            Log.e(TAG, "NSD registration error", e)
        }
    }
    
    fun startPeerDiscovery() {
        connectionStatus.value = ConnectionStatus.SEARCHING
        
        nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) {
                Log.d(TAG, "Discovery started")
            }
            
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service found: ${serviceInfo.serviceName}")
                
                if (serviceInfo.serviceType == SERVICE_TYPE) {
                    nsdManager?.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) {
                            Log.e(TAG, "Resolve failed: $errorCode")
                        }
                        
                        override fun onServiceResolved(info: NsdServiceInfo) {
                            val peer = PeerInfo(
                                host = info.host?.hostAddress ?: "",
                                port = info.port,
                                name = info.serviceName
                            )
                            val currentList = discoveredPeers.value.toMutableList()
                            if (!currentList.any { it.host == peer.host }) {
                                currentList.add(peer)
                                discoveredPeers.value = currentList
                            }
                        }
                    })
                }
            }
            
            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service lost")
                val currentList = discoveredPeers.value.toMutableList()
                currentList.removeAll { it.name == serviceInfo.serviceName }
                discoveredPeers.value = currentList
            }
            
            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "Discovery stopped")
            }
            
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery start failed: $errorCode")
                connectionStatus.value = ConnectionStatus.DISCONNECTED
            }
            
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery stop failed: $errorCode")
            }
        }
        
        try {
            nsdManager?.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "NSD discovery error", e)
        }
    }
    
    fun stopPeerDiscovery() {
        try {
            nsdManager?.stopServiceDiscovery(discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping discovery", e)
        }
    }
    
    fun stopServer() {
        isServerRunning = false
        try {
            serverSocket?.close()
            clients.values.forEach { it.close() }
            clients.clear()
            nsdManager?.unregisterService(registrationListener)
            connectionStatus.value = ConnectionStatus.DISCONNECTED
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server", e)
        }
    }
    
    fun disconnect() {
        stopServer()
        stopPeerDiscovery()
    }
}
