package com.gravisludio.flyinghotfixes.mixin;

import com.zurrtum.create.infrastructure.transfer.InventoryWrapper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Create Fly bug, the item twin of {@link FluidInventoryWrapperCapacityMixin}: {@code InventoryWrapper.init()}
 * computes the stack limit of a <em>slotted</em> Fabric Transfer item storage with {@code if (max > capacity)}
 * instead of {@code max < capacity}, so {@code getMaxStackSize()} answers 0 for every such storage. Measured with a
 * plain 9-slot container exposed through Fabric Transfer: Create Fly reports 0 where each slot holds 64.
 * <p>
 * The fix recomputes the limit once {@code init()} has run, as the largest capacity of any view.
 */
@Mixin(value = InventoryWrapper.class, remap = false)
public abstract class InventoryWrapperCapacityMixin {
    @Shadow
    protected Storage<ItemVariant> storage;

    @Shadow
    protected int capacity;

    @Inject(method = "init", at = @At("TAIL"))
    private void flyinghotfixes$fixSlottedStackLimit(CallbackInfo ci) {
        long max = 0;
        for (StorageView<ItemVariant> view : this.storage) {
            max = Math.max(max, view.getCapacity());
        }
        this.capacity = (int) Math.min(max, Integer.MAX_VALUE);
    }
}
