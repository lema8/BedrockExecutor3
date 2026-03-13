# Bedrock Executor

A script executor / behavior pack injector for Minecraft Bedrock Edition on Android.

## How It Works

1. Scripts are written in JavaScript using the **Bedrock ScriptAPI** (`@minecraft/server`)
2. The app packages enabled scripts into a valid behavior pack
3. The behavior pack is injected into MCBE's data folder (`/sdcard/Android/data/com.mojang.minecraftpe/files/games/com.mojang/behavior_packs/`)
4. You activate it in Minecraft → Settings → Global Resources

**No root required** for local worlds. The MCBE data folder is accessible on most Android devices.

## Project Structure

```
BedrockExecutor/
├── app/src/main/java/com/bedrockexecutor/
│   ├── MainActivity.kt                    # Entry point, handles permissions
│   ├── data/
│   │   ├── model/Script.kt               # Data models
│   │   ├── BuiltInScripts.kt             # Pre-made scripts (fly, speed, etc.)
│   │   └── repository/ScriptRepository.kt # Script storage (SharedPreferences)
│   ├── injection/
│   │   └── BehaviorPackBuilder.kt        # Builds + injects behavior packs
│   └── ui/
│       ├── MainViewModel.kt              # App state + injection logic
│       ├── theme/Theme.kt                # Dark executor UI theme
│       └── screens/HomeScreen.kt         # All UI screens (Scripts/Console/Editor)
```

## Built-In Scripts

| Script | Category | Description |
|--------|----------|-------------|
| Fly Mode | Movement | Survival fly for all players |
| Speed Boost | Movement | Speed effect level 3 |
| Kill Aura | Combat | Damages nearby entities |
| X-Ray Vision | Visual | Shows nearby ores in action bar |
| God Mode | Combat | Infinite health + resistance |
| Infinite Items | Utility | Refills held item stack |
| Chat Prefix | Utility | [OP] tag on all messages |
| Anti Knockback | Combat | Negates knockback |
| Server Info | Server | Broadcasts player count |

## Build Instructions

### Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Steps
1. Open the project in Android Studio
2. Let Gradle sync (it downloads dependencies automatically)
3. Connect your Android device or use an emulator
4. Click Run → Run 'app'
5. On first launch, grant "All Files Access" permission

### Build APK
```
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

## Usage

1. Launch the app
2. Grant All Files Access permission when prompted
3. Go to **Scripts** tab — toggle scripts ON/OFF
4. Tap the **INJECT** button
5. Open Minecraft → Settings → Global Resources → Activate "BedrockExecutor"
6. Load a world — scripts run immediately

## Adding Custom Scripts

1. Go to the **Editor** tab
2. Write your JavaScript using `@minecraft/server` API
3. Save with a name
4. Toggle it on in the Scripts tab
5. Re-inject

## Notes

- Scripts use Minecraft's official ScriptAPI — works on local worlds out of the box
- For servers: the server must have the behavior pack active too (self-hosted servers only)
- Re-inject after making changes — Minecraft only loads packs on world start
- Tested on Minecraft Bedrock 1.20+
