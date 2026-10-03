# Maintenance review — 3 October 2026

This review inspected the current source and built all three plugins. It did not run a live multiplayer server or exhaustively audit binary world data and historical logs.

## Changes made

- Added the project README, setup outline, verified build results and user-confirmed event participation.
- Removed 435 generated/runtime/player-specific files from Git's current index while preserving the local copies in the review checkout.
- Retained plugin source, Gradle wrappers and existing lobby/coastal-template map assets.
- Added root ignore rules to keep local runtime files and player data out of future commits.
- Removed the stale root plugin descriptor and packaged manifest from the current index; plugin descriptors remain in each module's resources.
- Updated the obsolete development milestone in the AnniSim working instructions.
- No gameplay Java files were changed.

## Priorities

1. **Administrative command defaults.** `CommandProtectionManager.protectedMode` defaults to false. Start/reset/loadout-ban commands use that toggle; `/protect` is operator-only, but enabling protection must be repeated after restart. Review all admin commands, make defaults restrictive, and add explicit permission nodes and tests.
2. **Historical player data.** The original tracked server snapshot contains logs and player-specific records. Current-tree cleanup does not erase historical copies. Review them locally without republishing identifiers. If sensitive data is confirmed, plan a coordinated history cleanup rather than assuming a normal deletion removes it.
3. **Repeatable installation.** `ArenaMap` contains hardcoded names/spawns for maps not present in this repository. RushSim's configured CleanMaps template is also absent. Provide documented sample assets with clear attribution/licensing, or move these settings into validated configuration.
4. **Test coverage.** All three builds succeeded, but all three reported `test NO-SOURCE`. Add meaningful automated logic tests and manual event checks for death/disconnect during countdown, round completion, missing maps, repeated resets, cooldown cleanup and mode switches.
5. **Build warnings and readability.** AnniSim and RushSim emit deprecated-API and unchecked-operation warnings; HubController emits deprecation warnings. Several files contain raw collections, synthetic-method comments and generated-looking identifiers. Improve these incrementally while checking gameplay remains equivalent.
6. **Bridge diagnostics.** `PluginBridge` catches reflection exceptions without reporting them, and returns false/no-op for unavailable methods. Log useful context and consider a stable interface; currently mode failures can be difficult to diagnose.
7. **Asset provenance.** Custom plugin authorship is confirmed by the maintainer. Map ownership/permissions and licences for redistributed assets have not been established in this review. Do not assign a blanket licence to the whole repository until clarified.

## Evidence

- `plugins/AnniSim`: Java 21, `clean build`, success; no test sources.
- `plugins/RushSim`: Java 21, `clean build`, success; no test sources.
- `plugins/HubController`: Java 21, `clean build`, success; no test sources.
- Events: maintainer reports 5+ events, each with 10–20 players; not independently measured analytics.
