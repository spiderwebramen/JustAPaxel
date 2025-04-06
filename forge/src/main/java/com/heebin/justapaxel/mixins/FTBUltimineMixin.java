package com.heebin.justapaxel.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.heebin.justapaxel.PaxelItem;
import com.heebin.justapaxel.PaxelItem.PaxelMode;

import dev.architectury.event.EventResult;
import dev.ftb.mods.ftbultimine.CooldownTracker;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.RightClickHandlers;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import dev.ftb.mods.ftbultimine.shape.ShapeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ToolActions;

@Mixin(FTBUltimine.class)
public class FTBUltimineMixin {

    @Inject(method = "blockRightClick", at = @At(value = "INVOKE_ASSIGN", target = "Ldev/ftb/mods/ftbultimine/FTBUltiminePlayerData;updateBlocks(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;ZI)Ldev/ftb/mods/ftbultimine/shape/ShapeContext;"), locals = LocalCapture.CAPTURE_FAILHARD, remap = false, cancellable = true)
    public void injectBlockRightClick(Player player, InteractionHand hand, BlockPos clickPos, Direction face,
            CallbackInfoReturnable<EventResult> cir, ServerPlayer serverPlayer, FTBUltiminePlayerData data,
            HitResult hitResult, BlockHitResult blockHitResult, ShapeContext shapeContext) {

        // TODO: config paxel ultimine enable on ma mod owo
        // TODO: paxels right clicks currently dont respect ultimine configs
        // BUG: ultimine hoe till on rooted dirt gives double hanging roots
        // BUG: ultimine shovel flatten with shovel mode on rooted dirt gives single hanging root instead of none
        
        ItemStack itemStack = serverPlayer.getItemInHand(hand);
        if (itemStack.getItem() instanceof PaxelItem) {
            if (shapeContext == null || !data.isPressed() || !data.hasCachedPositions()) {
                cir.setReturnValue(EventResult.pass());
                cir.cancel();
            }

            int didWork = 0;
            BlockState blockState = serverPlayer.level().getBlockState(clickPos);
            UseOnContext context = new UseOnContext(player, hand, blockHitResult);
            if (blockState.getToolModifiedState(context, ToolActions.HOE_TILL, false) != null && PaxelItem.getPaxelMode(itemStack) == PaxelMode.HOE) {
                didWork = IRightClickHandlersInvokers.callFarmlandConversion(serverPlayer, hand, clickPos, data);
            } else if (
                (blockState.getToolModifiedState(context, ToolActions.AXE_STRIP, false) != null) ||
                (blockState.getToolModifiedState(context, ToolActions.AXE_SCRAPE, false) != null) ||
                (blockState.getToolModifiedState(context, ToolActions.AXE_WAX_OFF, false) != null)
            ) {
                didWork = IRightClickHandlersInvokers.callAxeStripping(serverPlayer, hand, clickPos, data);
            } 
            else if (blockState.getToolModifiedState(context, ToolActions.SHOVEL_FLATTEN, false) != null && PaxelItem.getPaxelMode(itemStack) == PaxelMode.SHOVEL) {
                didWork = IRightClickHandlersInvokers.callShovelFlattening(serverPlayer, hand, clickPos, data);
            }

            if (didWork > 0) {
                player.swing(hand);
                if (!player.isCreative()) {
                    CooldownTracker.setLastUltimineTime(player, System.currentTimeMillis());
                    data.addPendingXPCost(Math.max(0, didWork - 1));
                }
                cir.setReturnValue(EventResult.interruptFalse());
                cir.cancel();
            } else {
                cir.setReturnValue(EventResult.pass());
                cir.cancel();
            }
        }
    }
}




