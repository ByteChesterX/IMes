package com.p2pchat.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.p2pchat.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "p2pchat_prefs")

class LocalDataSource(private val context: Context) {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    companion object {
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val USERNAME_KEY = stringPreferencesKey("username")
        val PROFILE_IMAGE_KEY = stringPreferencesKey("profile_image")
        val BLOCKED_USERS_KEY = stringPreferencesKey("blocked_users")
        val CHAT_ROOMS_KEY = stringPreferencesKey("chat_rooms")
        val MESSAGES_KEY = stringPreferencesKey("messages")
        val CONNECTION_CODES_KEY = stringPreferencesKey("connection_codes")
    }
    
    val userIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }
    
    val usernameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USERNAME_KEY]
    }
    
    val profileImageUriFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PROFILE_IMAGE_KEY]
    }
    
    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }
    
    suspend fun saveUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
        }
    }
    
    suspend fun saveProfileImageUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri != null) {
                preferences[PROFILE_IMAGE_KEY] = uri
            } else {
                preferences.remove(PROFILE_IMAGE_KEY)
            }
        }
    }
    
    suspend fun saveUserProfile(profile: UserProfile) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = profile.userId
            preferences[USERNAME_KEY] = profile.username
            if (profile.profileImageUri != null) {
                preferences[PROFILE_IMAGE_KEY] = profile.profileImageUri
            }
        }
    }
    
    suspend fun blockUser(userId: String) {
        context.dataStore.edit { preferences ->
            val blockedUsers = getBlockedUsers().toMutableList()
            if (!blockedUsers.contains(userId)) {
                blockedUsers.add(userId)
                preferences[BLOCKED_USERS_KEY] = json.encodeToString(blockedUsers)
            }
        }
    }
    
    suspend fun unblockUser(userId: String) {
        context.dataStore.edit { preferences ->
            val blockedUsers = getBlockedUsers().toMutableList()
            blockedUsers.remove(userId)
            preferences[BLOCKED_USERS_KEY] = json.encodeToString(blockedUsers)
        }
    }
    
    suspend fun getBlockedUsers(): List<String> {
        return try {
            context.dataStore.data.map { preferences ->
                preferences[BLOCKED_USERS_KEY]?.let { json.decodeFromString<List<String>>(it) } ?: emptyList()
            }.first()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun saveChatRoom(room: ChatRoom) {
        context.dataStore.edit { preferences ->
            val rooms = getChatRooms().toMutableList()
            val existingIndex = rooms.indexOfFirst { it.id == room.id }
            
            if (existingIndex >= 0) {
                rooms[existingIndex] = room
            } else {
                rooms.add(room)
            }
            
            preferences[CHAT_ROOMS_KEY] = json.encodeToString(rooms)
        }
    }
    
    suspend fun getChatRooms(): List<ChatRoom> {
        return try {
            context.dataStore.data.map { preferences ->
                preferences[CHAT_ROOMS_KEY]?.let { json.decodeFromString<List<ChatRoom>>(it) } ?: emptyList()
            }.first()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun deleteChatRoom(roomId: String) {
        context.dataStore.edit { preferences ->
            val rooms = getChatRooms().toMutableList()
            rooms.removeAll { it.id == roomId }
            preferences[CHAT_ROOMS_KEY] = json.encodeToString(rooms)
        }
    }
    
    suspend fun saveMessage(roomId: String, message: Message) {
        context.dataStore.edit { preferences ->
            val key = stringPreferencesKey("messages_$roomId")
            val messages = getMessages(roomId).toMutableList()
            messages.add(message)
            preferences[key] = json.encodeToString(messages)
        }
    }
    
    suspend fun getMessages(roomId: String): List<Message> {
        return try {
            val key = stringPreferencesKey("messages_$roomId")
            context.dataStore.data.map { preferences ->
                preferences[key]?.let { json.decodeFromString<List<Message>>(it) } ?: emptyList()
            }.first()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun clearMessages(roomId: String) {
        context.dataStore.edit { preferences ->
            val key = stringPreferencesKey("messages_$roomId")
            preferences.remove(key)
        }
    }
    
    suspend fun saveConnectionCode(code: String, userId: String) {
        context.dataStore.edit { preferences ->
            val codes = getConnectionCodes().toMutableMap()
            codes[code] = userId
            preferences[CONNECTION_CODES_KEY] = json.encodeToString(codes)
        }
    }
    
    suspend fun getConnectionCodes(): Map<String, String> {
        return try {
            context.dataStore.data.map { preferences ->
                preferences[CONNECTION_CODES_KEY]?.let { json.decodeFromString<Map<String, String>>(it) } ?: emptyMap()
            }.first()
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    suspend fun getUserByConnectionCode(code: String): String? {
        return getConnectionCodes()[code]
    }
    
    suspend fun updateGroupMemberRole(roomId: String, userId: String, role: MemberRole) {
        val rooms = getChatRooms()
        val room = rooms.find { it.id == roomId } ?: return
        
        val updatedRoom = room.copy(
            admins = if (role == MemberRole.ADMIN || role == MemberRole.OWNER) {
                room.admins.toMutableList().apply { add(userId) }
            } else {
                room.admins.filter { it != userId }
            }
        )
        
        saveChatRoom(updatedRoom)
    }
    
    suspend fun removeGroupMember(roomId: String, userId: String) {
        val rooms = getChatRooms()
        val room = rooms.find { it.id == roomId } ?: return
        
        val updatedRoom = room.copy(
            members = room.members.filter { it != userId },
            admins = room.admins.filter { it != userId }
        )
        
        saveChatRoom(updatedRoom)
    }
}
