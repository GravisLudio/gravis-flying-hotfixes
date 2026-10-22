package com.gravisludio.flyinghotfixes.gametest;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.fluids.spout.SpoutBlockEntity;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.depot.DepotBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The real thing a player builds: a Depot with an item on it and a Spout two blocks above, with water in the Spout.
 * The Spout's own item pipeline decides (onItemReceived / whenItemHeld), so this goes through everything the
 * capacity fix has to survive, not just the capacity number. Water is put straight into the Spout's tank, because
 * how fluid gets there (a pump and pipes; a Spout cannot be filled by hand or from a tank above it) is not what is
 * under test.
 * <p>
 * The glass bottle is the control: it must fill with or without the fix, so a failing pitcher with a passing bottle
 * means the rig is right and the pitcher is what is wrong. The pitcher test needs Steam 'n' Rails
 * ({@code -PwithSteamRails=<jar>}); without it, it passes.
 */
public class SpoutPitcherTests {
    private static final Logger LOGGER = LoggerFactory.getLogger("flyinghotfixes gametest");
    private static final BlockPos DEPOT = new BlockPos(2, 1, 2);
    private static final BlockPos SPOUT = new BlockPos(2, 3, 2);

    /** Control: a glass bottle on the depot becomes a bottle of water. */
    @GameTest(maxTicks = 400)
    public void spoutFillsAGlassBottleOnADepot(GameTestHelper helper) {
        runRig(helper, new ItemStack(Items.GLASS_BOTTLE), result -> {
            LOGGER.info("[FH-TEST] spout + glass bottle -> {}", result);
            return result.getItem() == Items.POTION;
        }, "the glass bottle did not turn into a potion (the rig itself is not filling anything)");
    }

    /** The reported bug: an empty paint pitcher on the depot must become a sandy (water) pitcher. */
    @GameTest(maxTicks = 400)
    public void spoutFillsAnEmptyPaintPitcherOnADepot(GameTestHelper helper) {
        if (!FabricLoader.getInstance().isModLoaded("railways")) {
            helper.succeed();
            return;
        }
        Item empty = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("railways", "empty_paint_pitcher"));
        Item sandy = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("railways", "sandy_paint_pitcher"));
        runRig(helper, new ItemStack(empty), result -> {
            LOGGER.info("[FH-TEST] spout + empty paint pitcher -> {}", result);
            return result.getItem() == sandy;
        }, "the empty paint pitcher was not filled by the spout (still an empty pitcher)");
    }

    /** The exact command a player pastes to give a Spout one bucket of water without building pumps and pipes. */
    static final String WATER_NBT =
        "{Tanks:[{Force:1b,Speed:0.25f,TankContent:{amount:81000,components:{\"create:fluid_max_capacity\":81000},id:\"minecraft:water\"},Target:0.0f,Value:0.0f}]}";

    /**
     * The whole player recipe in one test: place a Depot and a Spout with commands, fill the Spout with
     * {@code /data merge block}, put an empty pitcher on the depot, and see it become a sandy pitcher.
     */
    @GameTest(maxTicks = 400)
    public void spoutFedByDataMergeFillsAnEmptyPaintPitcher(GameTestHelper helper) {
        if (!FabricLoader.getInstance().isModLoaded("railways")) {
            helper.succeed();
            return;
        }
        var server = helper.getLevel().getServer();
        var source = server.createCommandSourceStack().withLevel(helper.getLevel());
        BlockPos depotAbs = helper.absolutePos(DEPOT);
        BlockPos spoutAbs = helper.absolutePos(SPOUT);
        run(server, source, "setblock %d %d %d create:depot".formatted(depotAbs.getX(), depotAbs.getY(), depotAbs.getZ()));
        run(server, source, "setblock %d %d %d create:spout".formatted(spoutAbs.getX(), spoutAbs.getY(), spoutAbs.getZ()));
        helper.runAfterDelay(5, () -> {
            run(server, source, "data merge block %d %d %d %s".formatted(spoutAbs.getX(), spoutAbs.getY(), spoutAbs.getZ(), WATER_NBT));
            SpoutBlockEntity spout = helper.getBlockEntity(SPOUT, SpoutBlockEntity.class);
            FluidStack fluid = spout.tank.getPrimaryHandler().getFluid();
            LOGGER.info("[FH-TEST] spout after /data merge: {}", fluid);
            if (fluid.getAmount() != 81_000)
                throw helper.assertionException(SPOUT, Component.literal("/data merge did not put 81000 of water in the spout: " + fluid));
            Item empty = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("railways", "empty_paint_pitcher"));
            Item sandy = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("railways", "sandy_paint_pitcher"));
            DepotBlockEntity depot = helper.getBlockEntity(DEPOT, DepotBlockEntity.class);
            depot.depotBehaviour.setHeldItem(new TransportedItemStack(new ItemStack(empty)));
            helper.runAfterDelay(150, () -> {
                ItemStack held = depot.depotBehaviour.getHeldItemStack();
                LOGGER.info("[FH-TEST] data-merge rig: depot holds {}", held);
                if (held.getItem() != sandy)
                    throw helper.assertionException(DEPOT, Component.literal("the pitcher was not filled; the depot holds " + held));
                helper.succeed();
            });
        });
    }

    /**
     * The same recipe with two commands and no {@code /data merge}: the Spout is placed with its water already in the
     * block data, so nothing depends on a second command finding the same position (relative {@code ~} coordinates
     * drift if the player moves between commands, and then {@code /data merge} says "The target block is not a block
     * entity").
     */
    @GameTest(maxTicks = 400)
    public void spoutPlacedWithItsWaterInOneCommandFillsAnEmptyPaintPitcher(GameTestHelper helper) {
        if (!FabricLoader.getInstance().isModLoaded("railways")) {
            helper.succeed();
            return;
        }
        var server = helper.getLevel().getServer();
        var source = server.createCommandSourceStack().withLevel(helper.getLevel());
        BlockPos depotAbs = helper.absolutePos(DEPOT);
        BlockPos spoutAbs = helper.absolutePos(SPOUT);
        run(server, source, "setblock %d %d %d create:depot".formatted(depotAbs.getX(), depotAbs.getY(), depotAbs.getZ()));
        run(server, source, "setblock %d %d %d create:spout%s".formatted(spoutAbs.getX(), spoutAbs.getY(), spoutAbs.getZ(), WATER_NBT));
        helper.runAfterDelay(5, () -> {
            SpoutBlockEntity spout = helper.getBlockEntity(SPOUT, SpoutBlockEntity.class);
            FluidStack fluid = spout.tank.getPrimaryHandler().getFluid();
            LOGGER.info("[FH-TEST] spout placed with nbt: {}", fluid);
            if (fluid.getAmount() != 81_000)
                throw helper.assertionException(SPOUT, Component.literal("setblock with nbt did not put 81000 of water in the spout: " + fluid));
            Item empty = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("railways", "empty_paint_pitcher"));
            Item sandy = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("railways", "sandy_paint_pitcher"));
            DepotBlockEntity depot = helper.getBlockEntity(DEPOT, DepotBlockEntity.class);
            depot.depotBehaviour.setHeldItem(new TransportedItemStack(new ItemStack(empty)));
            helper.runAfterDelay(150, () -> {
                ItemStack held = depot.depotBehaviour.getHeldItemStack();
                LOGGER.info("[FH-TEST] one-command rig: depot holds {}", held);
                if (held.getItem() != sandy)
                    throw helper.assertionException(DEPOT, Component.literal("the pitcher was not filled; the depot holds " + held));
                helper.succeed();
            });
        });
    }

    /**
     * What happens when the depot holds a STACK of 5 and the spout is never short of water (it is topped up every
     * 10 ticks, as a pump would). Diagnostic: it logs the depot content and every dropped stack and always passes;
     * the glass bottles are the control for how a Spout treats a stack in general.
     */
    @GameTest(maxTicks = 700)
    public void spoutWithAStackOfFiveGlassBottles(GameTestHelper helper) {
        stackRig(helper, "glass bottles", new ItemStack(Items.GLASS_BOTTLE, 5), Items.POTION);
    }

    @GameTest(maxTicks = 700)
    public void spoutWithAStackOfFiveEmptyPitchers(GameTestHelper helper) {
        if (!FabricLoader.getInstance().isModLoaded("railways")) {
            helper.succeed();
            return;
        }
        Item empty = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("railways", "empty_paint_pitcher"));
        LOGGER.info("[FH-TEST] empty pitcher max stack size = {}", new ItemStack(empty).getMaxStackSize());
        Item sandy = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("railways", "sandy_paint_pitcher"));
        LOGGER.info("[FH-TEST] sandy pitcher max stack size = {}", new ItemStack(sandy).getMaxStackSize());
        stackRig(helper, "empty pitchers", new ItemStack(empty, 5), sandy);
    }

    /** {@code expectedFinal}: what the depot holds at the end (the glass bottles end on a potion; the pitchers must end on a sandy pitcher). */
    private static void stackRig(GameTestHelper helper, String label, ItemStack stack, Item expectedFinal) {
        helper.setBlock(DEPOT, AllBlocks.DEPOT.defaultBlockState());
        helper.setBlock(SPOUT, AllBlocks.SPOUT.defaultBlockState());
        // Everything is scheduled from here, at tick 0: the framework's timer list breaks if dozens of timers are
        // added from inside another timer while it is being walked.
        helper.runAfterDelay(5, () -> helper.getBlockEntity(DEPOT, DepotBlockEntity.class)
            .depotBehaviour.setHeldItem(new TransportedItemStack(stack.copy())));
        for (int t = 8; t <= 600; t += 8) {
            helper.runAfterDelay(t, () -> {
                SpoutBlockEntity spout = helper.getBlockEntity(SPOUT, SpoutBlockEntity.class);
                if (spout.tank.getPrimaryHandler().getFluid().getAmount() < 81_000)
                    spout.tank.getPrimaryHandler().setFluid(new FluidStack(Fluids.WATER, 81_000));
            });
        }
        for (int t : new int[]{60, 150, 300, 600}) {
            helper.runAfterDelay(t, () -> {
                DepotBlockEntity depot = helper.getBlockEntity(DEPOT, DepotBlockEntity.class);
                var box = new net.minecraft.world.phys.AABB(helper.absolutePos(DEPOT)).inflate(5);
                var drops = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, box)
                    .stream().map(e -> e.getItem().toString()).toList();
                LOGGER.info("[FH-TEST] stack rig ({}) at tick {}: depot holds {} | dropped on the ground: {}",
                    label, t, depot.depotBehaviour.getHeldItemStack(), drops);
            });
        }
        helper.runAfterDelay(610, () -> {
            ItemStack held = helper.getBlockEntity(DEPOT, DepotBlockEntity.class).depotBehaviour.getHeldItemStack();
            if (held.getItem() != expectedFinal)
                throw helper.assertionException(DEPOT, Component.literal(
                    "a stack of 5 %s was left untouched by the spout; after 610 ticks the depot holds %s, expected a %s"
                        .formatted(label, held, BuiltInRegistries.ITEM.getKey(expectedFinal))));
        });
        helper.runAfterDelay(620, helper::succeed);
    }

    private static void run(net.minecraft.server.MinecraftServer server, net.minecraft.commands.CommandSourceStack source, String command) {
        server.getCommands().performPrefixedCommand(source, command);
    }

    private interface Check {
        boolean ok(ItemStack result);
    }

    private static void runRig(GameTestHelper helper, ItemStack input, Check check, String failure) {
        helper.setBlock(DEPOT, AllBlocks.DEPOT.defaultBlockState());
        helper.setBlock(SPOUT, AllBlocks.SPOUT.defaultBlockState());
        helper.runAfterDelay(5, () -> {
            DepotBlockEntity depot = helper.getBlockEntity(DEPOT, DepotBlockEntity.class);
            SpoutBlockEntity spout = helper.getBlockEntity(SPOUT, SpoutBlockEntity.class);
            spout.tank.getPrimaryHandler().setFluid(new FluidStack(Fluids.WATER, 81_000));
            // What /data merge would have to carry to give a Spout that water (printed so a player can paste it).
            LOGGER.info("[FH-TEST] spout nbt with 1 bucket of water: {}", spout.saveWithFullMetadata(helper.getLevel().registryAccess()));
            depot.depotBehaviour.setHeldItem(new TransportedItemStack(input.copy()));
            helper.runAfterDelay(150, () -> {
                ItemStack held = depot.depotBehaviour.getHeldItemStack();
                LOGGER.info("[FH-TEST] depot holds {} after 150 ticks (spout fluid {})", held, spout.tank.getPrimaryHandler().getFluid());
                if (!check.ok(held))
                    throw helper.assertionException(DEPOT, Component.literal(failure + "; the depot holds " + held));
                helper.succeed();
            });
        });
    }
}
