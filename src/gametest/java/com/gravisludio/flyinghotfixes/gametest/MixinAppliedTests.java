package com.gravisludio.flyinghotfixes.gametest;

import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.zurrtum.create.content.schematics.cannon.SchematicannonBlockEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.gametest.framework.GameTestHelper;

import java.lang.reflect.Method;

/**
 * The mixin configs use {@code defaultRequire: 0} so a Create Fly update can never stop the game
 * from starting. The price is that a fix whose injection point stops matching goes quiet, so this
 * checks that each server-side fix really was woven into its target: Mixin merges a handler into
 * the target class under a name that still contains the handler's own name.
 */
public class MixinAppliedTests {
    @GameTest
    public void schematicannonChecklistFixIsApplied(GameTestHelper helper) {
        assertWoven(helper, SchematicannonBlockEntity.class, "flyinghotfixes$consumeOnePaper");
        helper.succeed();
    }

    @GameTest
    public void armExtractFixIsApplied(GameTestHelper helper) {
        assertWoven(helper, ArmInteractionPoint.class, "flyinghotfixes$extractFromSlot");
        helper.succeed();
    }

    @GameTest
    public void armUppercaseModeFixIsApplied(GameTestHelper helper) {
        assertWoven(helper, ArmBlockEntity.class, "flyinghotfixes$lowercaseUpstreamModes");
        helper.succeed();
    }

    /**
     * With Create: Connected (Fly Port) 1.3.3-mc26.2-4 or earlier loaded, which carries the same
     * fixes, both mods' handlers must be woven into the same classes. Needs
     * {@code -PwithConnected=<jar>}; without Connected there is nothing to check and it passes.
     */
    @GameTest
    public void fixesCoexistWithConnected(GameTestHelper helper) {
        if (!FabricLoader.getInstance().isModLoaded("create_connected")) {
            helper.succeed();
            return;
        }
        assertWoven(helper, SchematicannonBlockEntity.class, "create_connected$consumeOnePaper");
        assertWoven(helper, SchematicannonBlockEntity.class, "flyinghotfixes$consumeOnePaper");
        assertWoven(helper, ArmInteractionPoint.class, "create_connected$extractFromSlot");
        assertWoven(helper, ArmInteractionPoint.class, "flyinghotfixes$extractFromSlot");
        assertWoven(helper, ArmBlockEntity.class, "create_connected$lowercaseUpstreamModes");
        assertWoven(helper, ArmBlockEntity.class, "flyinghotfixes$lowercaseUpstreamModes");
        helper.succeed();
    }

    private static void assertWoven(GameTestHelper helper, Class<?> target, String handler) {
        for (Method method : target.getDeclaredMethods()) {
            if (method.getName().contains(handler))
                return;
        }
        throw helper.assertionException(net.minecraft.network.chat.Component.literal(
                "%s is not in %s: the fix was not applied".formatted(handler, target.getName())));
    }
}
