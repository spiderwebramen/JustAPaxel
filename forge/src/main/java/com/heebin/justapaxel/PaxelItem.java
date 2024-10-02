package com.heebin.justapaxel;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class PaxelItem extends DiggerItem {
    private static final String tagName = "ToolMode";

    public static enum PaxelMode {
        SHOVEL,
        HOE
    }

    public PaxelItem(Tier pTier, float pAttackDamageBaseline, float pAttackSpeedModifier, Properties itemProperties) {
        super(pAttackDamageBaseline, pAttackSpeedModifier, pTier, JustAPaxel.MINEABLE_WITH_PAXEL, itemProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState blockState = level.getBlockState(blockPos);

        BlockState blockModifiedState = null;
        SoundEvent sound = null;

        // Handle shovel on campfires
        if (context.getClickedFace() != Direction.DOWN && blockState.getBlock() instanceof CampfireBlock
                && blockState.getValue(CampfireBlock.LIT)) {
            if (!level.isClientSide()) {
                level.levelEvent((Player)null, 1009, blockPos, 0); // 1009 = fire extinguish event from LevelRenderer
            }

            CampfireBlock.dowse(player, level, blockPos, blockState);
            blockModifiedState = blockState.setValue(CampfireBlock.LIT, Boolean.valueOf(false));
        }

        if (blockModifiedState == null) {
            blockModifiedState = blockState.getToolModifiedState(context, ToolActions.AXE_STRIP, false);
            sound = SoundEvents.AXE_STRIP;
        }

        if (blockModifiedState == null) {
            blockModifiedState = blockState.getToolModifiedState(context, ToolActions.AXE_SCRAPE, false);
            sound = SoundEvents.AXE_SCRAPE;
            if (blockModifiedState != null) level.levelEvent(player, 3005, blockPos, 0); // 3005 = axe scrape event
        }

        if (blockModifiedState == null) {
            blockModifiedState = blockState.getToolModifiedState(context, ToolActions.AXE_WAX_OFF, false);
            sound = SoundEvents.AXE_WAX_OFF;
            if (blockModifiedState != null) level.levelEvent(player, 3004, blockPos, 0); // 3004 = axe wax off event
        }

        if (blockModifiedState == null) {
            PaxelMode mode = getPaxelMode(player.getItemInHand(context.getHand()));
            if (mode == PaxelMode.SHOVEL) {
                blockModifiedState = blockState.getToolModifiedState(context, ToolActions.SHOVEL_FLATTEN, false);
                sound = SoundEvents.SHOVEL_FLATTEN;
            } else if (mode == PaxelMode.HOE) {
                blockModifiedState = blockState.getToolModifiedState(context, ToolActions.HOE_TILL, false);
                sound = SoundEvents.HOE_TILL;
            }
        }

        if (blockModifiedState == null) {
            return InteractionResult.PASS;
        }

        if (sound != null) {
            level.playSound(player, blockPos, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
        }

        if (!level.isClientSide()) {
            level.setBlock(blockPos, blockModifiedState, 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, blockModifiedState));

            if (player != null) {
                context.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockPos blockPos = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE).getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (player.isShiftKeyDown() && blockState.isAir()) {
            if (!level.isClientSide()) {
                toggleMode(stack, player);
            }

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.fail(stack);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState blockState) {
        // Handle cobweb like a sword
        if (blockState.is(Blocks.COBWEB)) {
            return 15.0f;
        }

        return super.getDestroySpeed(stack, blockState);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState blockState) {
        // Handle cobweb like a sword
        return blockState.is(Blocks.COBWEB) || super.isCorrectToolForDrops(stack, blockState);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return JustAPaxel.DEFAULT_PAXEL_ACTIONS.contains(toolAction);
    }

    public void toggleMode(ItemStack stack, Player player) {
        CompoundTag compoundTag = stack.getTag();
        if (compoundTag == null || !compoundTag.contains(tagName, 8)) {
            compoundTag.putString(tagName, PaxelMode.SHOVEL.name());
        }

        PaxelMode mode = PaxelMode.valueOf(compoundTag.getString(tagName));
        if (mode == PaxelMode.SHOVEL) {
            mode = PaxelMode.HOE;
        } else if (mode == PaxelMode.HOE) {
            mode = PaxelMode.SHOVEL;
        }
        compoundTag.putString(tagName, mode.name());

        player.displayClientMessage(
                Component.translatable("notification.justapaxel.switch_mode", Component.literal(mode.toString())),
                true);
    }

    public static PaxelMode getPaxelMode(ItemStack stack) {
        CompoundTag compoundTag = stack.getTag();
        if (compoundTag == null || !compoundTag.contains(tagName, 8)) { // 8 = TAG_STRING
            compoundTag.putString(tagName, PaxelMode.SHOVEL.name());
        }

        return PaxelMode.valueOf(compoundTag.getString(tagName));
    }

}
