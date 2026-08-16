package com.p2pchat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    val profileImageUri: String? = null,
    val isBlocked: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
)

@Serializable
data class Message(
    val id: String,
    val senderId: String,
    val content: String,
    val messageType: MessageType = MessageType.TEXT,
    val timestamp: Long = System.currentTimeMillis(),
    val mediaUri: String? = null,
    val replyToId: String? = null
)

@Serializable
enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    FILE,
    SYSTEM
}

@Serializable
data class ChatRoom(
    val id: String,
    val name: String,
    val isGroup: Boolean,
    val members: List<String>,
    val admins: List<String> = emptyList(),
    val creatorId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastMessageId: String? = null,
    val profileImageUri: String? = null
)

@Serializable
data class GroupMember(
    val userId: String,
    val role: MemberRole,
    val joinedAt: Long = System.currentTimeMillis()
)

@Serializable
enum class MemberRole {
    OWNER,
    ADMIN,
    MEMBER,
    MUTED
}

@Serializable
data class ConnectionRequest(
    val fromUserId: String,
    val toUserId: String,
    val code: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class UserProfile(
    val userId: String,
    val username: String,
    val bio: String = "",
    val profileImageUri: String? = null,
    val status: String = "Available"
)
