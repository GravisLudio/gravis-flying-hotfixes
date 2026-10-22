package com.gravisludio.flyinghotfixes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.content.fluids.transfer.GenericItemFilling;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Create Fly bug: a Spout never filled an item that was sitting on its depot or belt in a STACK of two or more
 * (an empty paint pitcher stacks to 64), although it fills a single one. {@code GenericItemFilling.fillItem}
 * works on a one-item copy ({@code stack.copyWithCount(1)}), but {@code getRequiredAmountForItem}, which the Spout
 * asks first, hands the whole stack to the item's Fabric Transfer storage. Create Fly's item context cannot split a
 * stack (its {@code insertOverflow} always returns 0), so the "how much fits?" question fails, the answer is 0, and the
 * Spout lets the stack pass untouched. Glass bottles and buckets are not affected: Create Fly handles them on its own.
 * <p>
 * The fix asks the question about one item, as {@code fillItem} already does.
 */
@Mixin(value = GenericItemFilling.class, remap = false)
public class GenericItemFillingStackMixin {
    @WrapOperation(
            method = "getRequiredAmountForItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/zurrtum/create/foundation/fluid/FluidHelper;getFluidInventory(Lnet/minecraft/world/item/ItemStack;)Lcom/zurrtum/create/infrastructure/fluids/FluidItemInventory;"
            )
    )
    private static FluidItemInventory flyinghotfixes$askAboutOneItem(ItemStack stack, Operation<FluidItemInventory> original) {
        return original.call(stack.getCount() > 1 ? stack.copyWithCount(1) : stack);
    }
}
