package com.p2pchat.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.p2pchat.domain.model.ChatRoom
import com.p2pchat.ui.theme.*

@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreateGroup: (String, List<String>) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var showMemberSelection by remember { mutableStateOf(false) }
    var selectedMembers by remember { mutableStateOf<List<String>>(emptyList()) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DiscordDarkHeader),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Create Group",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DiscordBlue,
                        unfocusedBorderColor = DiscordDarkInput,
                        focusedLabelColor = DiscordBlue,
                        unfocusedLabelColor = TextMuted
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (groupName.isNotBlank()) {
                                onCreateGroup(groupName, selectedMembers)
                                onDismiss()
                            }
                        },
                        enabled = groupName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = DiscordGreen)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionDialog(
    connectionCode: String,
    onDismiss: () -> Unit,
    onConnectWithCode: (String) -> Unit
) {
    var inputCode by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DiscordDarkHeader),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Connect to Friend",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Share this code with your friend:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Connection code display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DiscordDarkInput, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = connectionCode,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = DiscordBlue,
                        letterSpacing = androidx.compose.ui.unit.SpacedBy(8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Divider(color = DiscordDarkHover)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Or enter friend's code:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it.filter { char -> char.isDigit() }.take(6) },
                    label = { Text("Connection Code", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DiscordBlue,
                        unfocusedBorderColor = DiscordDarkInput,
                        focusedLabelColor = DiscordBlue,
                        unfocusedLabelColor = TextMuted
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = TextMuted)
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (inputCode.length == 6) {
                                onConnectWithCode(inputCode)
                            }
                        },
                        enabled = inputCode.length == 6,
                        colors = ButtonDefaults.buttonColors(containerColor = DiscordBlue)
                    ) {
                        Text("Connect")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDialog(
    username: String,
    bio: String,
    profileImageUri: String?,
    onDismiss: () -> Unit,
    onSaveProfile: (String, String, String?) -> Unit,
    blockedUsers: List<String> = emptyList(),
    onUnblockUser: (String) -> Unit = {}
) {
    var editedUsername by remember { mutableStateOf(username) }
    var editedBio by remember { mutableStateOf(bio) }
    var newProfileImageUri by remember { mutableStateOf(profileImageUri) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DiscordDarkHeader),
            shape = RoundedCornerShape(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp)
            ) {
                item {
                    Text(
                        text = "Profile Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Profile image preview
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(50))
                            .background(DiscordDarkInput),
                        contentAlignment = Alignment.Center
                    ) {
                        if (newProfileImageUri != null) {
                            AsyncImage(
                                model = newProfileImageUri,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = editedUsername,
                        onValueChange = { editedUsername = it },
                        label = { Text("Username", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DiscordBlue,
                            unfocusedBorderColor = DiscordDarkInput,
                            focusedLabelColor = DiscordBlue,
                            unfocusedLabelColor = TextMuted
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = editedBio,
                        onValueChange = { editedBio = it },
                        label = { Text("Bio", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DiscordBlue,
                            unfocusedBorderColor = DiscordDarkInput,
                            focusedLabelColor = DiscordBlue,
                            unfocusedLabelColor = TextMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 5
                    )
                    
                    // Blocked users section
                    if (blockedUsers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Blocked Users",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        blockedUsers.forEach { userId ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = userId.take(15),
                                    color = TextSecondary
                                )
                                
                                TextButton(onClick = { onUnblockUser(userId) }) {
                                    Text("Unblock", color = DiscordRed)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = TextMuted)
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                onSaveProfile(editedUsername, editedBio, newProfileImageUri)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DiscordBlue)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
