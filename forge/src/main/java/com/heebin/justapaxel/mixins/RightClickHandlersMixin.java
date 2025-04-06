package com.heebin.justapaxel.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import com.heebin.justapaxel.PaxelItem;

import dev.ftb.mods.ftbultimine.RightClickHandlers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(RightClickHandlers.class)
public abstract class RightClickHandlersMixin {
    @Redirect(method = "axeStripping", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"), remap = false)
    private static Item redirectAxeItemAccess(ItemStack itemStack) {
        if (itemStack.getItem() instanceof PaxelItem) {
            // since axeStripping() only needs to invoke getStripped() from AxeItem
            // we can just give it any dummy AxeItem item so axeStripping() can cast it to AxeItemAccess ezpz
            return (new ItemStack(Items.DIAMOND_AXE).getItem());
        }

        return itemStack.getItem();
    }
}
