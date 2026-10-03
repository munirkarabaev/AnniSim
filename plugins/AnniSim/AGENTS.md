# AnniSim coding instructions



This repository contains a Minecraft Paper plugin.

Development workflow:
- Code is written in IntelliJ.
- Codex assists with implementation.
- Plugins are built with Gradle.
- The test server is located at:

../../servers/test-server/

After building, the plugin jar must be copied to the server plugins folder.


This is a Minecraft Paper plugin project written in Java using Gradle.

## Rules
- Do not rename the plugin.
- Do not change the package name from `me.munir.annisim`.
- Do not add unnecessary frameworks or libraries.
- Keep the code simple and beginner-friendly.
- Do not implement extra features that were not requested.
- Only make changes needed for the current task.
- Prefer clear class names and small classes.
- Keep comments minimal and useful.
- Preserve existing working code unless the task requires changing it.

## Current game concept
A team-based Minecraft minigame with:
- a lobby
- red and blue team selection
- kit selection
- admin start command
- match on a preset map
- spectator mode on death
- diamond objective and elimination win condition later

## Current baseline

The plugin now includes lobby menus, teams, kits and abilities, round commands,
scoreboards and diamond objectives. The earlier lobby-only milestone is obsolete.
Read the root README and docs/MAINTENANCE.md before planning further changes.
Keep local server installations, generated builds, logs and player records out of Git.
Existing lobby/coastal map assets remain in the repository; server binaries and
other map templates must be provided separately for a fresh test installation.

## Developer Commands

### test
When the user types `test`, run the full local plugin testing pipeline.

Steps:

1. Build the plugin if needed
./gradlew build
Skip this step if the plugin was already built after the latest code changes.
2. Copy the newest plugin jar into the local Paper test server plugins folder
cp build/libs/*.jar ../../servers/test-server/plugins/

3. The server will be restarted manually by the user after deployment.

Purpose:
This command is used frequently during development to quickly compile and deploy the plugin to the test server.
