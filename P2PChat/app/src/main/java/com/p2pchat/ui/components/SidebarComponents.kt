package com.p2pchat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.p2pchat.domain.model.ChatRoom
import com.p2pchat.domain.model.MemberRole
import com.p2pchat.ui.theme.*

@Composable
fun Sidebar(
    chatRooms: List<ChatRoom>,
    selectedRoomId: String?,
    onRoomSelected: (ChatRoom) -> Unit,
    onCreateGroup: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(72.dp)
            .background(DiscordDarkSidebar)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile button at top
        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(DiscordDarkHover)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = TextPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Divider(color = DiscordDarkHover, thickness = 1.dp)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Home button
        IconButton(
            onClick = { },
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (selectedRoomId == null) DiscordDarkActive else Color.Transparent)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = if (selectedRoomId == null) DiscordGreen else TextMuted
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Chat rooms list
        chatRooms.forEach { room ->
            IconButton(
                onClick = { onRoomSelected(room) },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selectedRoomId == room.id) DiscordDarkActive else Color.Transparent)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (room.profileImageUri != null) {
                        AsyncImage(
                            model = room.profileImageUri,
                            contentDescription = room.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                        )
                    } else {
                        Text(
                            text = room.name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedRoomId == room.id) TextPrimary else TextMuted
                        )
                    }
                    
                    // Online indicator for groups
                    if (room.isGroup) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(DiscordGreen)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Add server button
        Spacer(modifier = Modifier.weight(1f))
        
        IconButton(
            onClick = onCreateGroup,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Transparent)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Server",
                tint = DiscordGreen
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ChannelList(
    chatRoom: ChatRoom?,
    currentUserIsAdmin: Boolean,
    onMemberClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(240.dp)
            .background(DiscordDarkChannelList)
            .padding(16.dp)
    ) {
        if (chatRoom != null) {
            // Room header
            Text(
                text = if (chatRoom.isGroup) "# ${chatRoom.name}" else chatRoom.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Divider(color = DiscordDarkHover)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Members section
            Text(
                text = "MEMBERS — ${chatRoom.members.size}",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Admins first
            chatRoom.admins.forEach { adminId ->
                MemberItem(
                    userId = adminId,
                    role = MemberRole.ADMIN,
                    onClick = onMemberClick
                )
            }
            
            // Then regular members
            chatRoom.members.filter { it !in chatRoom.admins }.forEach { memberId ->
                MemberItem(
                    userId = memberId,
                    role = MemberRole.MEMBER,
                    onClick = onMemberClick
                )
            }
        }
    }
}

@Composable
private fun MemberItem(
    userId: String,
    role: MemberRole,
    onClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(userId) }
            .padding(vertical = 6.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(DiscordDarkHover),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(10.dp))
        
        Column {
            Text(
                text = userId.take(12),
                style = MaterialTheme.typography.bodyMedium,
                color = when (role) {
                    MemberRole.OWNER -> DiscordRed
                    MemberRole.ADMIN -> DiscordOrange
                    MemberRole.MUTED -> TextMuted
                    else -> TextPrimary
                },
                fontWeight = if (role == MemberRole.ADMIN || role == MemberRole.OWNER) FontWeight.Bold else FontWeight.Normal
            )
            
            Text(
                text = role.name.lowercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}
