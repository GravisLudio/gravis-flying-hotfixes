package com.gravisludio.flyinghotfixes.gametest;

import com.zurrtum.create.content.fluids.transfer.GenericItemFilling;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * End to end for the bug these fixes were found with: in Steam 'n' Rails (a Create Fly addon) an empty paint
 * pitcher exposes its fluid through a single-slot Fabric Transfer storage, Create Fly reported its capacity as 0,
 * and a Spout never filled it, so no coloured pitcher could be obtained. With this mod loaded the empty pitcher must
 * be fillable. It needs Steam 'n' Rails on the classpath ({@code -PwithSteamRails=<jar>}, see build.gradle);
 * without it there is nothing to check and the test passes. The jar must be one whose pitcher storage is still the
 * single-slot one (the Create Fly ports by cat4blep or BumblePie as of 2026-10); a build that already works around it
 * proves nothing here.
 */
public class SteamRailsPitcherTests {
    private static final Logger LOGGER = LoggerFactory.getLogger("flyinghotfixes gametest");

    @GameTest
    public void emptyPitcherCanBeFilledBySpout(GameTestHelper helper) {
        if (!FabricLoader.getInstance().isModLoaded("railways")) {
            helper.succeed();
            return;
        }
        Item empty = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("railways", "empty_paint_pitcher"));
        Item black = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("railways", "black_paint_pitcher"));
        Fluid paint = BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("railways", "paint"));
        CompoundTag color = new CompoundTag();
        color.putString("Color", "black");
        FluidStack stack = new FluidStack(paint, 81_000,
            DataComponentPatch.builder().set(DataComponents.CUSTOM_DATA, CustomData.of(color)).build());

        ItemStack pitcher = new ItemStack(empty);
        boolean fillable = GenericItemFilling.canItemBeFilled(helper.getLevel(), pitcher);
        int required = GenericItemFilling.getRequiredAmountForItem(helper.getLevel(), pitcher, stack);
        ItemStack filled = required > 0
            ? GenericItemFilling.fillItem(helper.getLevel(), required, pitcher.copy(), stack.copy())
            : ItemStack.EMPTY;
        LOGGER.info("[FH-TEST] Steam 'n' Rails empty pitcher: canItemBeFilled={} requiredPaint={} filled={}", fillable, required, filled);

        if (!fillable)
            throw helper.assertionException(new BlockPos(0, 1, 0), Component.literal(
                "an empty paint pitcher cannot be filled by a Spout (canItemBeFilled=false)"));
        if (filled.isEmpty() || filled.getItem() != black)
            throw helper.assertionException(new BlockPos(0, 1, 0), Component.literal(
                "filling an empty pitcher with black paint gave %s instead of railways:black_paint_pitcher".formatted(filled)));
        helper.succeed();
    }
}
