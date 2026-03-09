# Changes Made to SVC Intercom Plugin

## Overview
Implemented a speaker system with positional audio for broadcasts. Players now only hear broadcasts when they're near defined speaker locations, creating a more realistic experience.

---

## New Files Created

### 1. `src/main/java/eu/projnull/spelis/svci/voice/Speaker.java`
**Purpose:** Data model representing a virtual speaker in the world

**Features:**
- Stores speaker location (world, x, y, z coordinates)
- Configurable broadcast range
- Named speakers for easy management
- Distance calculation to check if players are in range
- Helper method to get Bukkit Location object

---

### 2. `src/main/java/eu/projnull/spelis/svci/voice/SpeakerManager.java`
**Purpose:** Singleton manager for speaker storage and persistence

**Features:**
- Manages all speakers across all worlds
- JSON-based persistence (saves to `plugins/SVCIntercom/speakers.json`)
- Add/remove/list speakers by world
- Find speakers within range of a location
- Thread-safe using ConcurrentHashMap
- Automatic save on modifications
- Automatic load on initialization

---

### 3. `src/main/java/eu/projnull/spelis/svci/commands/handlers/SpeakerCommand.java`
**Purpose:** Command handler for managing speakers

**Commands Implemented:**
- `/intercom speaker add <name> <range>` - Add speaker at player's current location
- `/intercom speaker add <name> <world> <x> <y> <z> <range>` - Add speaker at specific coordinates
- `/intercom speaker remove <world> <name>` - Remove a speaker
- `/intercom speaker list [world]` - List all speakers (defaults to player's world)

**Features:**
- Tab completion for world names and speaker names
- Permission checks (`svcintercom.speaker.add`, `svcintercom.speaker.remove`, `svcintercom.speaker.list`)
- Validation for duplicate names and world existence
- Range validation (1.0 to 1000.0 blocks)

---

### 4. `gradlew`
**Purpose:** Gradle wrapper script for building the project
- Standard Gradle wrapper shell script for Unix-based systems
- Made executable for building the project

---

## Modified Files

### 1. `src/main/java/eu/projnull/spelis/svci/voice/VoicePlugin.java`
**Changes Made:**
- Modified `onMicPacket()` method to use positional audio from speakers
- **Old behavior:** Sent static audio packets to all players in world (everyone hears equally)
- **New behavior:** 
  - Checks for speakers in the world
  - If speakers exist, only players near speakers hear the broadcast
  - Audio appears to come from the nearest speaker location using locational sound packets
  - Falls back to old behavior if no speakers are defined
- For each listener, finds the nearest speaker within range
- Uses `sendLocationalSoundPacketTo()` with speaker position instead of `sendStaticSoundPacketTo()`

---

### 2. `src/main/java/eu/projnull/spelis/svci/commands/handlers/FileCommand.java`
**Changes Made:**
- Modified audio playback to use speakers for positional audio
- **Old behavior:** Created static audio channels for each player (everyone hears equally)
- **New behavior:**
  - Checks for speakers in the world
  - If speakers exist, creates locational audio channels at each speaker position
  - Sets channel distance to speaker's range
  - Falls back to old behavior if no speakers are defined
  - Updated success message to show speaker count
- Uses `createLocationalAudioChannel()` at speaker positions instead of `createStaticAudioChannel()`

---

### 3. `src/main/java/eu/projnull/spelis/svci/commands/IntercomCommand.java`
**Changes Made:**
- Added `SpeakerCommand` to the list of registered handlers
- New line: `registerHandler(new SpeakerCommand());`

---

### 4. `src/main/java/eu/projnull/spelis/svci/Intercom.java`
**Changes Made:**
- Added import for `SpeakerManager`
- Modified `onEnable()` method to initialize the speaker system
- Added: `SpeakerManager.inst().initialize(this.getDataFolder());`
- This runs before everything else to load saved speakers from disk

---

### 5. `build.gradle.kts`
**Changes Made:**
- Added Gson dependency for JSON serialization
- New line in dependencies: `implementation("com.google.code.gson:gson:2.10.1")`
- Required for saving/loading speaker configuration

---

### 6. `README.md`
**Changes Made:**
- Expanded documentation with new features section
- Added speaker system explanation
- Documented all new speaker commands
- Added "How It Works" section explaining positional audio behavior
- Explained fallback to global audio when no speakers are defined

---

## Technical Details

### How Positional Audio Works

1. **Live Broadcasts:**
   - When a broadcaster speaks, the plugin intercepts their microphone packets
   - For each online player in the world, it finds the nearest speaker within range
   - If a speaker is found, audio is sent as a locational sound packet from the speaker's position
   - If no speaker is within range, that player hears nothing

2. **File Playback:**
   - Audio is decoded and prepared for playback
   - For each speaker in the world, a locational audio channel is created at the speaker's position
   - Each channel has a distance parameter set to the speaker's range
   - All speakers play the audio simultaneously

3. **Fallback Behavior:**
   - If no speakers are defined in a world, the plugin uses the original global broadcast
   - This ensures backward compatibility with existing setups

### Data Persistence

- Speakers are saved to `plugins/SVCIntercom/speakers.json`
- Format: Array of speaker objects with worldId, coordinates, range, and name
- Automatically loaded on plugin enable
- Automatically saved after any speaker modification

### Permission Nodes

New permissions added:
- `svcintercom.speaker` - Base permission for speaker commands
- `svcintercom.speaker.add` - Permission to add speakers
- `svcintercom.speaker.remove` - Permission to remove speakers
- `svcintercom.speaker.list` - Permission to list speakers

---

## Summary of Changes

**Files Created:** 4
- Speaker.java (data model)
- SpeakerManager.java (persistence & management)
- SpeakerCommand.java (command handlers)
- gradlew (build script)

**Files Modified:** 6
- VoicePlugin.java (positional audio for live broadcasts)
- FileCommand.java (positional audio for file playback)
- IntercomCommand.java (register speaker commands)
- Intercom.java (initialize speaker system)
- build.gradle.kts (add Gson dependency)
- README.md (documentation)

**Total Lines Added:** ~600+
**Key Feature:** Coordinate-based virtual speakers with positional audio and limited range
