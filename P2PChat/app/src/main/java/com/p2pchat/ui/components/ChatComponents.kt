package com.p2pchat.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.halilibo.richtext.ui.material3.RichText
import com.p2pchat.domain.model.Message
import com.p2pchat.domain.model.MessageType
import com.p2pchat.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatMessageList(
    messages: List<Message>,
    currentUserId: String,
    onBlockUser: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DiscordDarkMessage)
            .padding(16.dp),
        reverseLayout = true
    ) {
        items(messages.reversed(), key = { it.id }) { message ->
            MessageItem(
                message = message,
                isCurrentUser = message.senderId == currentUserId,
                onBlockUser = onBlockUser
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MessageItem(
    message: Message,
    isCurrentUser: Boolean,
    onBlockUser: (String) -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f)
        ) {
            // Sender name and timestamp
            if (!isCurrentUser) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = message.senderId.take(12),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = DiscordBlue
                    )
                    
                    Text(
                        text = " • ${formatTimestamp(message.timestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
            
            // Message bubble
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .background(
                        color = if (isCurrentUser) DiscordBlue else DiscordDarkInput,
                        shape = RoundedCornerShape(
                            topStart = 4.dp,
                            topEnd = 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .clickable { showOptions = true }
                    .padding(12.dp)
            ) {
                when (message.messageType) {
                    MessageType.TEXT -> {
                        RichText {
                            markdown(message.content)
                        }
                    }
                    MessageType.IMAGE -> {
                        message.mediaUri?.let { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = "Image message",
                                modifier = Modifier
                                    .widthIn(max = 300.dp)
                                    .heightIn(max = 300.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                    MessageType.VIDEO -> {
                        message.mediaUri?.let { uri ->
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(DiscordDarkHover, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "Video",
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                    MessageType.AUDIO -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Audio Message",
                                color = TextPrimary
                            )
                        }
                    }
                    MessageType.FILE -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message.content,
                                color = TextPrimary
                            )
                        }
                    }
                    MessageType.SYSTEM -> {
                        Text(
                            text = message.content,
                            color = DiscordYellow,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
            
            // Timestamp for current user
            if (isCurrentUser) {
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.padding(end = 8.dp, top = 4.dp)
                )
            }
        }
    }
    
    // Options dropdown
    if (showOptions && !isCurrentUser) {
        DropdownMenu(
            expanded = showOptions,
            onDismissRequest = { showOptions = false }
        ) {
            DropdownMenuItem(
                text = { Text("Block User") },
                onClick = {
                    onBlockUser(message.senderId)
                    showOptions = false
                },
                leadingIcon = {
                    Icon(Icons.Default.Block, contentDescription = null)
                }
            )
        }
    }
}

@Composable
fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = DiscordDarkInput),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column {
            // Selected file preview
            selectedFileUri?.let { uri ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = null,
                        tint = DiscordBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "File selected",
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { selectedFileUri = null }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = TextMuted
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Attachment button
                IconButton(onClick = { 
                    filePickerLauncher.launch("*/*")
                }) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Attach",
                        tint = TextMuted,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Text input
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text("Message", color = TextMuted) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp, max = 120.dp),
                    maxLines = 5
                )
                
                // Send button
                IconButton(
                    onClick = {
                        onSendMessage()
                        selectedFileUri = null
                    },
                    enabled = value.isNotBlank() || selectedFileUri != null
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (value.isNotBlank() || selectedFileUri != null) DiscordBlue else TextMuted,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
