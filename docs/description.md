# Create: Gravis's Flying Hotfixes

**Small, standalone fixes for bugs in [Create Fly](https://modrinth.com/mod/create-fly), the Fabric port of Create for Minecraft 26.2.**

No blocks, no items, no recipes, no new content: install it, forget it, and the bugs below stop happening. It is not made by, or endorsed by, ZurrTum or the Create team. These are the fixes that were found while running Create Fly on a real server, written so that anyone can have them without installing anything else.

## What it fixes

| Fix | Where it matters |
|---|---|
| **The game crashes every time you close it.** Create Fly's Flywheel threads are never stopped, so Minecraft's shutdown watchdog writes a "Watchdog (Client shutdown from post-main)" crash report on every exit. ([Create-Fly#357](https://github.com/ZurrTum/Create-Fly/issues/357)) | Client |
| **A crafting blueprint that faces south crashes everyone who looks at it** (`NullPointerException: "quat" is null` in `BlueprintRenderer`) as soon as it has a recipe in it. | Client |
| **The Schematicannon eats the whole stack of books or clipboards** in its input slot to print one material checklist. It now uses one. | Server |
| **The Schematicannon charges Farmer's Delight rich soil farmland as plain dirt**, so it turned dirt into rich soil farmland for free. It now costs rich soil. Only active if Farmer's Delight is installed. | Server |
| **A Mechanical Arm stalls on an inventory whose first item has nowhere to go** (a basin with leftover quartz in front of finished granite). It now takes the item that is in the slot it is looking at. ([Create-Fly#370](https://github.com/ZurrTum/Create-Fly/issues/370)) | Server |
| **Mechanical Arms in schematics saved with the original Create lose all their targets** (Create writes the mode as `TAKE`, Create Fly only reads `take`). | Server |

## Where to install it

Fixes for the client (the first two) only need the mod on the client; fixes for the server (the other four) only need it on the server, or in a single-player world. Installing it on both is fine and is what most people should do. Players on a server that does not have it still get the two client fixes.

## Requirements

- Minecraft 26.2 and Fabric Loader
- Fabric API
- [Create Fly](https://modrinth.com/mod/create-fly) 26.2-rc-2-6.0.9-1
- Java 25

## Compatible with Create: Connected (Fly Port)

[Create: Connected (Fly Port)](https://modrinth.com/mod/create-connected-fly-port) 1.3.3-mc26.2-4 and earlier carry the same fixes inside it, so you do not need this mod if you already have that version. If you have both, nothing breaks: applying one of these fixes twice gives the same result as applying it once, and the automated tests run with both mods loaded. Future versions of Connected may stop shipping their copies.

## If Create Fly fixes these itself

The mod is built to get out of the way: if a later Create Fly changes the code a fix hooks into, that fix is skipped instead of stopping the game. Once Create Fly fixes a bug, this mod can be removed.

## Source, issues, license

Source and issue tracker: [github.com/GravisLudio/gravis-flying-hotfixes](https://github.com/GravisLudio/gravis-flying-hotfixes). MIT license. Report problems here, not to the Create or Create Fly authors.
