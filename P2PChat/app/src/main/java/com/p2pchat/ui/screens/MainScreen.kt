package com.p2pchat.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.p2pchat.data.local.LocalDataSource
import com.p2pchat.domain.model.*
import com.p2pchat.network.P2PNetworkManager
import com.p2pchat.ui.components.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    networkManager: P2PNetworkManager,
    localDataSource: LocalDataSource,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var currentUserId by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<String?>(null) }
    var chatRooms by remember { mutableStateOf<List<ChatRoom>>(emptyList()) }
    var selectedRoom by remember { mutableStateOf<ChatRoom?>(null) }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var messageInput by remember { mutableStateOf("") }
    var blockedUsers by remember { mutableStateOf<List<String>>(emptyList()) }
    
    var showConnectionDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    
    val connectionCode by remember { mutableStateOf(networkManager.generateConnectionCode()) }
    
    // Load user data
    LaunchedEffect(Unit) {
        val userId = UUID.randomUUID().toString()
        localDataSource.saveUserId(userId)
        currentUserId = userId
        
        chatRooms = localDataSource.getChatRooms()
        blockedUsers = localDataSource.getBlockedUsers()
        
        // Start P2P server
        networkManager.startServer { data ->
            // Handle incoming message
            val receivedMessage = String(data)
            // Parse and save message
        }
        
        // Start peer discovery
        networkManager.startPeerDiscovery()
    }
    
    // Request permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission result
    }
    
    LaunchedEffect(Unit) {
        ActivityCompat.requestPermissions(
            context as android.app.Activity,
            arrayOf(
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            100
        )
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DiscordDarkBg)
        ) {
            // Left Sidebar (Discord-style)
            Sidebar(
                chatRooms = chatRooms,
                selectedRoomId = selectedRoom?.id,
                onRoomSelected = { room ->
                    selectedRoom = room
                    scope.launch {
                        messages = localDataSource.getMessages(room.id)
                    }
                },
                onCreateGroup = { showCreateGroupDialog = true },
                onProfileClick = { showProfileDialog = true }
            )
            
            // Channel List / Members Panel
            if (selectedRoom != null && selectedRoom!!.isGroup) {
                ChannelList(
                    chatRoom = selectedRoom,
                    currentUserIsAdmin = currentUserId in selectedRoom!!.admins,
                    onMemberClick = { memberId ->
                        // Show member options
                    }
                )
            }
            
            // Main Chat Area
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (selectedRoom != null) {
                    // Chat header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DiscordDarkHeader)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (selectedRoom!!.isGroup) "# ${selectedRoom!!.name}" else selectedRoom!!.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    
                    // Messages list
                    ChatMessageList(
                        messages = messages,
                        currentUserId = currentUserId,
                        onBlockUser = { userId ->
                            scope.launch {
                                localDataSource.blockUser(userId)
                                blockedUsers = localDataSource.getBlockedUsers()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Message input
                    MessageInput(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        onSendMessage = {
                            if (messageInput.isNotBlank() && selectedRoom != null) {
                                val message = Message(
                                    id = UUID.randomUUID().toString(),
                                    senderId = currentUserId,
                                    content = messageInput,
                                    messageType = MessageType.TEXT,
                                    timestamp = System.currentTimeMillis()
                                )
                                
                                scope.launch {
                                    localDataSource.saveMessage(selectedRoom!!.id, message)
                                    messages = localDataSource.getMessages(selectedRoom!!.id)
                                    
                                    // Send via P2P
                                    networkManager.sendMessage(messageInput.toByteArray())
                                }
                                
                                messageInput = ""
                            }
                        },
                        onAttachFile = {
                            // File attachment handled in MessageInput component
                        }
                    )
                } else {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DiscordDarkMessage),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Welcome to P2P Chat!",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Select a chat or connect with friends",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextMuted
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = { showConnectionDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DiscordBlue)
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connect with Friend")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showConnectionDialog) {
        ConnectionDialog(
            connectionCode = connectionCode,
            onDismiss = { showConnectionDialog = false },
            onConnectWithCode = { code ->
                scope.launch {
                    val targetUserId = localDataSource.getUserByConnectionCode(code)
                    if (targetUserId != null) {
                        // Connect to peer
                        // Implementation depends on network discovery
                    }
                }
                showConnectionDialog = false
            }
        )
    }
    
    if (showCreateGroupDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateGroupDialog = false },
            onCreateGroup = { groupName, members ->
                scope.launch {
                    val group = ChatRoom(
                        id = UUID.randomUUID().toString(),
                        name = groupName,
                        isGroup = true,
                        members = listOf(currentUserId) + members,
                        admins = listOf(currentUserId),
                        creatorId = currentUserId
                    )
                    localDataSource.saveChatRoom(group)
                    chatRooms = localDataSource.getChatRooms()
                }
                showCreateGroupDialog = false
            }
        )
    }
    
    if (showProfileDialog) {
        ProfileDialog(
            username = username,
            bio = "",
            profileImageUri = profileImageUri,
            onDismiss = { showProfileDialog = false },
            onSaveProfile = { newUsername, newBio, newImageUri ->
                scope.launch {
                    localDataSource.saveUsername(newUsername)
                    localDataSource.saveProfileImageUri(newImageUri)
                    username = newUsername
                    profileImageUri = newImageUri
                }
                showProfileDialog = false
            },
            blockedUsers = blockedUsers,
            onUnblockUser = { userId ->
                scope.launch {
                    localDataSource.unblockUser(userId)
                    blockedUsers = localDataSource.getBlockedUsers()
                }
            }
        )
    }
}
