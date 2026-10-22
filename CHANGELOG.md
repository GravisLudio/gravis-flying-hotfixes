# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 1.1.0 (unreleased)

### Fixed (Create Fly bugs)

- **Items that hold fluid through a Fabric Transfer single-slot storage could never be filled by a
  Spout.** Create Fly works out the capacity of a slotted storage in `FluidInventoryWrapper.init()`
  with the comparison turned around (`if (max > capacity)` where the non-slotted branch of the same
  class has `max < capacity`), so every such item reported a capacity of 0 and
  `GenericItemFilling.canItemBeFilled` answered "no". Found with the paint pitchers of Steam 'n' Rails
  (the empty pitcher reported 0 instead of 324000; with this fix the unmodified Create Fly port by
  BumblePie fills it). Server and client.
- **A Spout never filled an item that sat on its depot or belt in a stack of two or more** (an empty paint
  pitcher stacks to 64; one pitcher worked, five were ignored). `GenericItemFilling.fillItem` works on a
  one-item copy, but `getRequiredAmountForItem`, which the Spout asks first, handed the whole stack to the item's
  Fabric Transfer storage; Create Fly's item context cannot split a stack (`insertOverflow` returns 0), so the answer
  was 0 and the stack passed untouched. It is now asked about one item. Glass bottles and buckets were not affected.
  Server.
- **The same inverted comparison in `InventoryWrapper.init()`** made slotted Fabric Transfer *item*
  storages report a stack limit of 0 instead of 64 (measured with a plain 9-slot container). The
  effect in play has not been observed; the value was plainly wrong. Server and client.

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
