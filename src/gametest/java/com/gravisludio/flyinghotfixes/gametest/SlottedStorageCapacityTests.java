package com.gravisludio.flyinghotfixes.gametest;

import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.transfer.FluidInventoryWrapper;
import com.zurrtum.create.infrastructure.transfer.InventoryWrapper;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create Fly bug: the wrappers that present a Fabric Transfer storage to Create compute the capacity of a
 * <em>slotted</em> storage with the comparison turned around ({@code if (max > capacity)} where the other
 * branch of the same method has {@code max < capacity}), so the capacity always comes out as 0. Seen first with
 * the paint pitchers of Steam 'n' Rails: a Spout asks {@code canItemBeFilled}, gets "no" because the item reports
 * a capacity of 0, and never starts to fill it.
 * <p>
 * These tests wrap a plain Fabric storage that is a {@code SlottedStorage} and read what Create sees.
 * The numbers are printed with the {@code [FH-TEST]} marker.
 */
public class SlottedStorageCapacityTests {
    private static final Logger LOGGER = LoggerFactory.getLogger("flyinghotfixes gametest");

    /** A slotted fluid storage holding up to 324000 droplets must report 324000, not 0. */
    @GameTest
    public void slottedFluidStorageReportsItsCapacity(GameTestHelper helper) {
        SingleFluidStorage tank = SingleFluidStorage.withFixedCapacity(324_000L, () -> {
        });
        FluidInventory wrapped = FluidInventoryWrapper.of(tank);
        int reported = wrapped.getMaxAmountPerStack();
        LOGGER.info("[FH-TEST] slotted fluid storage: capacity 324000, Create Fly reports {}", reported);
        if (reported != 324_000)
            throw helper.assertionException(Component.literal(
                "a slotted fluid storage with capacity 324000 is reported by Create Fly as %d".formatted(reported)));
        helper.succeed();
    }

    /** A slotted item storage over a 9-slot container must report a stack limit of 64, not 0. */
    @GameTest
    public void slottedItemStorageReportsItsStackLimit(GameTestHelper helper) {
        Container wrapped = InventoryWrapper.of(ContainerStorage.of(new SimpleContainer(9), null));
        int reported = wrapped.getMaxStackSize();
        LOGGER.info("[FH-TEST] slotted item storage: slot capacity 64, Create Fly reports {}", reported);
        if (reported != 64)
            throw helper.assertionException(Component.literal(
                "a slotted item storage of 64-item slots is reported by Create Fly with a stack limit of %d".formatted(reported)));
        helper.succeed();
    }
}
