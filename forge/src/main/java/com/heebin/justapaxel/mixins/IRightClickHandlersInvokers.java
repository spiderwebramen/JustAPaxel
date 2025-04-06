package com.heebin.justapaxel.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.RightClickHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

@Mixin(RightClickHandlers.class)
public interface IRightClickHandlersInvokers {
    @Invoker("axeStripping")
    static int callAxeStripping(ServerPlayer player, InteractionHand hand, BlockPos clickPos,
            FTBUltiminePlayerData data) {
        throw new AssertionError();
    }

    @Invoker("shovelFlattening")
    static int callShovelFlattening(ServerPlayer player, InteractionHand hand, BlockPos clickPos,
            FTBUltiminePlayerData data) {
        throw new AssertionError();
    }

    @Invoker("farmlandConversion")
    static int callFarmlandConversion(ServerPlayer player, InteractionHand hand, BlockPos clickPos,
            FTBUltiminePlayerData data) {
        throw new AssertionError();
    }
}
