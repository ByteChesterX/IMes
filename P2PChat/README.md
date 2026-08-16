# P2P Chat - Serverless Android Messaging App

## 🚀 Features

### Core Features
- **🔒 Pure P2P Architecture**: No servers, all communication is direct between devices
- **👥 Group Chats**: Create groups with admin/member roles
- **🎨 Discord-like UI**: Beautiful dark theme sidebar interface
- **📝 Markdown Support**: Rich text formatting in messages
- **🖼️ Media Sharing**: Send images, videos, audio, and any file type
- **👤 Profile Photos**: Customizable profile pictures
- **🚫 Block Users**: Privacy controls to block unwanted contacts
- **🔗 Connection Codes**: Easy peer-to-peer connection via 6-digit codes
- **🌐 Long-range Connections**: Supports distant connections via network discovery

### Technical Features
- Jetpack Compose UI
- Material 3 Design
- Kotlin Coroutines & Flow
- DataStore for local persistence
- Network Service Discovery (NSD)
- Socket-based P2P communication

## 📁 Project Structure

```
app/src/main/java/com/p2pchat/
├── domain/model/          # Data models (User, Message, ChatRoom, etc.)
├── data/local/           # Local storage with DataStore
├── network/              # P2P network manager
├── ui/
│   ├── theme/           # Theme colors and typography
│   ├── components/      # Reusable UI components
│   └── screens/         # Main screens and dialogs
├── MainActivity.kt       # Entry point
└── P2PChatApplication.kt # Application class
```

## 🛠️ Build & Run

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 34

### Steps
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on device or emulator (API 26+)

```bash
./gradlew assembleDebug
```

## 🔐 Permissions

The app requires:
- Internet & Network access (for P2P connections)
- WiFi state permissions (for device discovery)
- Storage permissions (for media sharing)
- Camera permission (for profile photos)

## 📝 Usage

### Connecting with Friends
1. Open the app
2. Click "Connect with Friend"
3. Share your 6-digit code or enter friend's code
4. Start chatting!

### Creating Groups
1. Click the "+" button in sidebar
2. Enter group name
3. Add members
4. Group created with you as owner

### Sending Messages
- Type text with Markdown support
- Click attachment icon for files
- Supports: Images, Videos, Audio, Documents

### Managing Privacy
- Long-press on any message
- Select "Block User" to prevent future contact
- Manage blocked users in Profile settings

## 🎨 UI Components

- **Sidebar**: Discord-style server navigation
- **Channel List**: Member list with roles
- **Chat Area**: Message bubbles with media preview
- **Message Input**: Text input with file attachment
- **Dialogs**: Connection, Group Creation, Profile settings

## 🔄 GitHub Actions

Automated CI/CD pipeline:
- Build verification
- Lint checks
- Test execution
- APK artifact generation

## 📄 License

This project is open source and available for educational purposes.

## ⚠️ Notes

- P2P connections work best on same network
- For long-range, both devices need proper network configuration
- No cloud backup - all data stored locally
- Blocking is local only (no server to sync)
