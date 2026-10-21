package com.gravisludio.flyinghotfixes.mixin;

import com.zurrtum.create.infrastructure.transfer.FluidInventoryWrapper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Create Fly bug: the wrapper that presents a Fabric Transfer fluid storage to Create works out the capacity
 * of a <em>slotted</em> storage in {@code FluidInventoryWrapper.init()} with the comparison turned around
 * ({@code if (max > capacity)}; the branch for non-slotted storages in the same class has {@code max < capacity}),
 * so the capacity is always 0. Anything built on a single-slot storage then looks permanently full: a Spout asks
 * {@code GenericItemFilling.canItemBeFilled}, hears "no", and never starts to fill the item. Found with the paint
 * pitchers of Steam 'n' Rails (an empty pitcher reported a capacity of 0 instead of 324000).
 * <p>
 * The fix recomputes the capacity once {@code init()} has run, as the largest capacity of any view, which is what
 * the code means to do.
 */
@Mixin(value = FluidInventoryWrapper.class, remap = false)
public abstract class FluidInventoryWrapperCapacityMixin {
    @Shadow
    protected Storage<FluidVariant> storage;

    @Shadow
    protected int capacity;

    @Inject(method = "init", at = @At("TAIL"))
    private void flyinghotfixes$fixSlottedCapacity(CallbackInfo ci) {
        long max = 0;
        for (StorageView<FluidVariant> view : this.storage) {
            max = Math.max(max, view.getCapacity());
        }
        this.capacity = (int) Math.min(max, Integer.MAX_VALUE);
    }
}
