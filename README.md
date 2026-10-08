# Create: Gravis's Flying Hotfixes

Small standalone fixes for bugs in [Create Fly](https://github.com/ZurrTum/Create-Fly) (the Fabric
port of Create) on Minecraft 26.2. No content of its own: no blocks, items, recipes or packets.

The mod's page text (for CurseForge and Modrinth) is in [docs/description.md](docs/description.md),
the six fixes and why they exist are in the Javadoc of each class, and the history is in
[CHANGELOG.md](CHANGELOG.md).

| # | Fix | Side | Code |
|---|---|---|---|
| 1 | Game exit always wrote a Watchdog crash report (Flywheel threads never stopped) | Client | `client/FlywheelShutdownFix` |
| 2 | Schematicannon ate a whole stack of books/clipboards per checklist | Server | `mixin/SchematicannonChecklistMixin` |
| 3 | Schematicannon charged Farmer's Delight rich soil farmland as dirt | Server | `fix/SchematicRequirementFixes` |
| 4 | Mechanical Arm stalled when the first item of an inventory had no target | Server | `mixin/ArmInteractionPointExtractMixin` |
| 5 | Arms in schematics from the original Create lost their targets (uppercase modes) | Server | `mixin/ArmUpstreamPointModeMixin` |
| 6 | South-facing crafting blueprint with a recipe crashed whoever looked at it | Client | `mixin/client/BlueprintRendererNullRotationMixin` |

## Design rules

- **Never be the reason the game does not start.** Both mixin configs use `defaultRequire: 0` and
  `HotfixMixinPlugin` skips a mixin whose target class no longer exists. If Create Fly changes the
  code a fix hooks into, that fix is skipped; once Create Fly fixes a bug, remove the fix here.
- **Safe next to Create: Connected (Fly Port).** Connected 1.3.3-mc26.2-4 and earlier carry the same
  fixes. Applying one twice gives the same result as once (the tests run with both loaded).
- **No registrations**, so it installs on one side only. Client-only fixes live in the client
  entrypoint and the client mixin config.

## Build and test

Needs JDK 25. `gradle.properties` sets `org.gradle.java.home` to the author's JDK path: change it to
yours (or pass `-Dorg.gradle.java.home=<path>`).

```
./gradlew build          # jar in build/libs
./gradlew runGametest    # headless server tests, ~1 min, report in build/gametest/report.xml
```

(Add `--offline` if every dependency is already in your Gradle cache.)

Optional jars for the test classpath: `-PwithFarmersDelight=<jar>` activates the rich soil test
(Farmer's Delight 26.2 asks for Minecraft `~26.2`, the dev environment is `26.2-rc-2`: use a copy
whose `fabric.mod.json` accepts it, never the original) and `-PwithConnected=<jar>` runs everything
with Connected loaded too.

`MixinAppliedTests` checks that each server fix was really woven into its target: because of
`defaultRequire: 0` a fix whose injection point stops matching would otherwise go quiet. With
Connected loaded it also checks that both mods' handlers are woven into the same classes.
`SchematicannonChecklistTests` runs the checklist print for real (5 clipboards in, 4 must be left,
also with Connected loaded). The client fixes (1 and 6) cannot be tested headless; they are tested
in the game.

## License

MIT. Create Fly is CC0; this repository contains none of its code.
