# SVC Intercom

**SVC Intercom** is a plugin for Simple Voice Chat that adds broadcasting functionality, meaning you can talk to everyone
inside a world in a one way system, everyone can hear you, along with people close to them.

## Features:

- **Live Broadcasts**: Stream a player's microphone to everyone in a world
- **File Playback**: Play audio files to everyone in a world  
- **Speaker System**: Create virtual speakers with positional audio and limited range
  - Audio only plays from defined speaker locations
  - Players must be near a speaker to hear the broadcast
  - Support for multiple speakers per world

## Commands:

### Broadcast Commands

`/intercom live <player> <world> <duration>` Start broadcasting `player`s microphone to everyone in `world` for `duration` seconds

`/intercom info <world>` Shows active broadcasts in a world

`/intercom file <filename> <world>` Plays `filename` for everyone in `world` for the duration of the file

`/intercom stop <world>` Stops the broadcast in `world`

### Speaker Commands

`/intercom speaker add <name> <range>` Add a speaker at your current location with the given name and range (player only)

`/intercom speaker add <name> <world> <x> <y> <z> <range>` Add a speaker at specific coordinates

`/intercom speaker remove <world> <name>` Remove a speaker by name

`/intercom speaker list [world]` List all speakers in a world (defaults to your current world)

## How It Works:

When speakers are defined in a world, broadcasts use **positional audio** - players only hear the audio if they're within range of a speaker. The audio appears to come from the speaker's location, creating a more realistic experience.

If no speakers are defined in a world, the plugin falls back to the original behavior where all players hear the broadcast equally regardless of location.