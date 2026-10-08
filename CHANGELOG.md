# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 1.0.0

First release. Needs Minecraft 26.2, Fabric Loader, Fabric API, Java 25 and
[Create Fly](https://github.com/ZurrTum/Create-Fly) 26.2-rc-2-6.0.9-1. The six fixes were developed
inside Create: Connected (Fly Port) and are moved here unchanged in behaviour so they can be
installed on their own.

### Fixed (Create Fly bugs)

- **Closing the game always wrote a "Watchdog (Client shutdown from post-main)" crash report.**
  Flywheel's worker threads were never stopped, so the JVM could not exit. They are stopped when
  the client stops. (Create-Fly#357) Client.
- **A crafting blueprint facing south with a recipe in it crashed every player who looked at it**
  (`"quat" is null` in `BlueprintRenderer`). Client.
- **The Schematicannon consumed a whole stack of books or clipboards to print one checklist.** It
  now consumes one. Server.
- **The Schematicannon charged rich soil farmland (Farmer's Delight) as plain dirt.** It now costs
  rich soil. Server, only with Farmer's Delight.
- **A Mechanical Arm stalled on an inventory whose first item had no target** (for example a basin
  with leftover quartz ahead of finished granite). The arm now takes the item in the slot it is
  looking at. (Create-Fly#370) Server.
- **Mechanical Arms in schematics saved with the original Create lost all their targets.** Create
  writes the point mode in uppercase and Create Fly only reads lowercase. Server.

### Notes

- No blocks, items, recipes or packets are added, so the mod can be installed on only the client
  or only the server.
- If a later Create Fly changes the class or method a fix hooks into, that fix is skipped (and
  logged) instead of stopping the game.
