# AnniSim — Minecraft PvP Practice Server

**Java plugins for an event-based Minecraft practice server, with team combat, selectable kits and distinct player abilities.**

Developed and maintained independently by [Munir Karabaev](https://github.com/munirkarabaev), a BSc Computer Science student at King's College London. I also host the server and organise its community events.

## Real-world use

I have hosted **5+ events with 10–20 players per event**. Running these sessions has given me opportunities to gather player feedback, investigate bugs and improve the gameplay experience.

These participation figures are my own account of the events, not an automated analytics measurement. The server opens for events and closes between sessions, so there is no permanent public server address.

## What it does

- **Team-based rounds:** red/blue team selection, lobby preparation, countdowns, round completion and spectator handling.
- **Kit and loadout selection:** inventory menus for selecting abilities and equipment, with loadout restrictions.
- **Distinct abilities:** Dasher blink movement with target previews, Acrobat double-jump movement and cooldowns, and Succubus health-based life-drain mechanics.
- **Arena management:** select predefined maps and restore an arena from a template between rounds.
- **Objectives and scoreboards:** track diamond objectives, team elimination, round information and team wins.
- **Player interaction:** team chat, lobby protection and player mute controls.
- **Multiple server modes:** a separate RushSim plugin and HubController coordinate mode switching and shared lobby selection.

## My work

I built the custom plugins and operate the server. This combines Java development with running multiplayer sessions: implementing gameplay, observing how players use it, collecting feedback and fixing reported problems.

The repository includes development instructions acknowledging Codex assistance. Independent development describes my ownership of the custom project; Minecraft, Paper, Gradle and other dependencies are third-party software. Map authorship and redistribution permissions should be documented separately before treating the map assets as open-source content.

## Technical approach

The plugins use Paper's event listeners for interactions, combat, movement, death and disconnection. Managers separate responsibilities such as teams, kits, equipment, arenas, scoreboards and round state. Scheduled tasks support countdowns, ability cooldowns and interface updates.

HubController connects the game modes through a reflection-based bridge. It prevents mode switching while a round is active and routes shared lobby interactions to the selected plugin.

| Area | Technology |
| --- | --- |
| Language | Java 21 |
| Server API | Paper API `1.21.1-R0.1-SNAPSHOT` |
| Build | Gradle 9 wrapper, Kotlin build scripts |
| Gameplay | Paper/Bukkit events, scheduled tasks and inventory menus |
| Configuration | YAML plugin descriptors and configuration |

## Repository layout

```text
plugins/
  AnniSim/        Main practice minigame
  RushSim/        Additional game mode
  HubController/ Shared lobby and mode switching
servers/test-server/
  lobby/             Existing lobby map assets
  coastal_template/  Existing coastal arena template
```

The public working tree excludes generated builds, local server binaries, logs, operator lists and player-specific files. **Earlier Git history still contains the original server snapshot.** This cleanup does not rewrite that history or certify the old snapshot as free of personal information.

## Build from source

Install a **JDK 21**. Each plugin has its own Gradle wrapper; there is no root multi-project build.

Windows PowerShell, from the repository root:

```powershell
cd plugins/AnniSim
.\gradlew.bat clean build
cd ../RushSim
.\gradlew.bat clean build
cd ../HubController
.\gradlew.bat clean build
```

On macOS/Linux, use `sh ./gradlew clean build` in each of those directories. Outputs are written to each plugin's `build/libs/` directory. Gradle downloads build tooling and dependencies on first use.

## Run on a development server

1. Set up your own compatible Paper server using Java 21. The plugins compile against Paper 1.21.1; other server versions require separate compatibility checks.
2. Stop the server and back up any existing worlds before installing or testing plugins. Arena resets replace the active arena from its template.
3. Copy the three freshly built plugin JARs into the server's `plugins/` directory.
4. Use copies of the `lobby` and `coastal_template` assets as starting assets. The default AnniSim arena uses `coastal` / `coastal_template`.
5. Check the world names and spawn coordinates in [ArenaMap.java](plugins/AnniSim/src/main/java/me/munir/annisim/arena/ArenaMap.java) and [LobbyManager.java](plugins/AnniSim/src/main/java/me/munir/annisim/lobby/LobbyManager.java). The other map templates referenced by the code are not bundled. RushSim's default configuration also references `CleanMaps` / `CleanMaps_template`, which must be supplied separately.
6. Start the server, confirm that the plugins enabled, and use an operator account to run `/protect` before admitting players. Current protected-command mode defaults to off and is not persisted across restarts.
7. For the bundled coastal template, use `/startgame coastalv3`. The similarly named `/startgame coastal` selects a different arena in the missing `CleanMaps` world.

This is a development setup outline, not a claim that a fresh server has been verified end to end. Server software, local configuration and full map assets are not supplied as a ready-to-run installation.

### Main commands

| Command | Purpose |
| --- | --- |
| `/startgame <mapname>` | Start a round on a named map |
| `/startgame random_map` | Choose a random map; requires its template |
| `/startgame random_all` | Use randomized map/kit/loadout selection |
| `/startgame random_kit_loadout <mapname>` | Randomize selections on a named map |
| `/endgame` | End the current round |
| `/mute <player>` | Toggle player mute; operator-only |
| `/reset` | Reset the team win tracker |
| `/ban <loadout>` / `/unban <loadout>` | Restrict or restore a loadout |
| `/protect` | Toggle operator-only access to protected game commands |
| `/mode <annisim\|rushsim>` | Switch mode outside an active round; operator-only |
| `/rushreload` | Reload RushSim configuration |

Consult the descriptors in each plugin's `src/main/resources/plugin.yml` for command registration. Command permissions should be reviewed before a public event; not every command uses a dedicated permission node.

## Verification

On **3 October 2026**, all three plugins completed `clean build` successfully with Java 21. Compiler warnings remain for deprecated APIs and, in the game plugins, unchecked operations. Gradle reported `test NO-SOURCE`; the repository does not currently include automated test sources. No live multiplayer session was run during this documentation review.

## Next improvements

- Make administrative command access safe by default, persist protection settings and use explicit permission nodes.
- Add automated tests for start-command parsing, round state, cooldowns and mode switching, alongside a repeatable multiplayer test checklist.
- Make map names and spawn coordinates configurable, validate missing templates, and provide documented, licensed sample assets.
- Replace the reflection bridge's silent failures with useful diagnostic messages or a defined shared interface.
- Address compiler warnings and improve generated-looking variable names and raw collection types incrementally.
- Review old server logs/player records before deciding whether historical data needs removal from Git history.

Detailed findings: [maintenance review](docs/MAINTENANCE.md).

## Feedback

Use [GitHub issues](https://github.com/munirkarabaev/AnniSim/issues) for reproducible bugs or suggestions. Include the plugin/server versions, steps to reproduce and expected behaviour. Redact player identifiers, addresses and credentials from any logs or recordings.
