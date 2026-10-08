package com.gravisludio.flyinghotfixes.gametest;

import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * Farmer's Delight's rich soil farmland must cost rich soil, not dirt, in a schematic. The fix only
 * exists when Farmer's Delight is loaded, so this needs it on the classpath
 * ({@code -PwithFarmersDelight=<jar>}, see build.gradle); without it the test has nothing to check
 * and passes.
 */
public class RichSoilFarmlandTests {
    @GameTest
    public void richSoilFarmlandCostsRichSoil(GameTestHelper helper) {
        if (!FabricLoader.getInstance().isModLoaded("farmersdelight")) {
            helper.succeed();
            return;
        }
        Block farmland = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("farmersdelight", "rich_soil_farmland"));
        Item richSoil = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("farmersdelight", "rich_soil"));
        assertCost(helper, farmland, richSoil);
        // Control: plain farmland must keep costing dirt, so the fix is not just "every farmland".
        assertCost(helper, Blocks.FARMLAND, Items.DIRT);
        helper.succeed();
    }

    private static void assertCost(GameTestHelper helper, Block block, Item expected) {
        List<ItemRequirement.StackRequirement> items = ItemRequirement.of(block.defaultBlockState(), null).getRequiredItems();
        if (items.size() != 1 || !items.getFirst().stack.is(expected)) {
            throw helper.assertionException(Component.literal("%s costs %s, expected %s".formatted(
                    BuiltInRegistries.BLOCK.getKey(block),
                    items.stream().map(r -> r.stack.toString()).toList(),
                    BuiltInRegistries.ITEM.getKey(expected))));
        }
    }
}
