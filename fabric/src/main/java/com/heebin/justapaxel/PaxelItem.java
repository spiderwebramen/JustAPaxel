package com.heebin.justapaxel;

import net.minecraft.item.AxeItem;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Optional;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.PillarBlock;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import com.google.common.collect.BiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;

public class PaxelItem extends MiningToolItem {
    private static final String tagName = "ToolMode";

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Map<Block, BlockState> TILLING_STATES = Maps.newHashMap(
        new Builder()
            .put(Blocks.GRASS_BLOCK, Blocks.FARMLAND.getDefaultState())
            .put(Blocks.DIRT_PATH, Blocks.FARMLAND.getDefaultState())
            .put(Blocks.DIRT, Blocks.FARMLAND.getDefaultState())
            .put(Blocks.COARSE_DIRT, Blocks.DIRT.getDefaultState())
            .put(Blocks.ROOTED_DIRT, Blocks.DIRT.getDefaultState())
            .build()
    );

    public static enum PaxelMode {
        SHOVEL,
        HOE
    }

    public PaxelItem(ToolMaterial pTier, float pAttackDamageBaseline, float pAttackSpeedModifier, Item.Settings itemProperties) {
        super(pAttackDamageBaseline, pAttackSpeedModifier, pTier, JustAPaxel.MINEABLE_WITH_PAXEL, itemProperties);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World level = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        BlockState blockState = level.getBlockState(blockPos);

        BlockState blockModifiedState = null;
        SoundEvent sound = null;

        // Shovel on campfires
        if (context.getSide() != Direction.DOWN && blockState.getBlock() instanceof CampfireBlock
                && blockState.get(CampfireBlock.LIT)) {
            if (!level.isClient()) {
                level.syncWorldEvent((PlayerEntity)null, 1009, blockPos, 0); // 1009 = fire extinguish event from LevelRenderer
            }

            CampfireBlock.extinguish(player, level, blockPos, blockState);
            blockModifiedState = blockState.with(CampfireBlock.LIT, Boolean.valueOf(false));
        }

        // Axe log strip
        if (blockModifiedState == null) {
            blockModifiedState = Optional.ofNullable(TempAxeItem.STRIPPED_BLOCKS.get(blockState.getBlock()))
                .map(block -> block.getDefaultState().with(PillarBlock.AXIS, blockState.get(PillarBlock.AXIS))).orElse(null);
            sound = SoundEvents.ITEM_AXE_STRIP;
        }

        // Axe copper scrape
        if (blockModifiedState == null) {
            blockModifiedState = Oxidizable.getDecreasedOxidationState(blockState).orElse(null);
            sound = SoundEvents.ITEM_AXE_SCRAPE;
            if (blockModifiedState != null) level.syncWorldEvent(player, 3005, blockPos, 0); // 3005 = axe scrape event
        }

        // Axe copper remove wax
        if (blockModifiedState == null) {
            blockModifiedState = Optional.ofNullable((Block)((BiMap)HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get()).get(blockState.getBlock()))
                .map(block -> ((Block)block).getStateWithProperties(blockState)).orElse(null);
            sound = SoundEvents.ITEM_AXE_WAX_OFF;
            if (blockModifiedState != null) level.syncWorldEvent(player, 3004, blockPos, 0); // 3004 = axe wax off event
        }

        if (blockModifiedState == null) {
            PaxelMode mode = getPaxelMode(player.getStackInHand(context.getHand()));
            // Shovel flatten
            if (mode == PaxelMode.SHOVEL) {
                blockModifiedState = TempShovelItem.PATH_STATES.get(blockState.getBlock());
                sound = SoundEvents.ITEM_SHOVEL_FLATTEN;
            // Hoe till
            } else if (mode == PaxelMode.HOE) {
                if(blockState.isOf(Blocks.ROOTED_DIRT)) {
                    Block.dropStack(level, blockPos, context.getSide(), new ItemStack(Items.HANGING_ROOTS));
                    blockModifiedState = TILLING_STATES.get(blockState.getBlock());
                }

                if(HoeItem.canTillFarmland(context)) { // can only till dirt -> farmland when the block above is air.
                    blockModifiedState = TILLING_STATES.get(blockState.getBlock());
                }
                sound = SoundEvents.ITEM_HOE_TILL;
            }
        }

        if (blockModifiedState == null) {
            return ActionResult.PASS;
        }

        if (sound != null) {
            level.playSound(player, blockPos, sound, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }

        if (!level.isClient()) {
            level.setBlockState(blockPos, blockModifiedState, 11);
            level.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(player, blockModifiedState));

            if (player != null) {
                context.getStack().damage(1, player, p -> p.sendToolBreakStatus(context.getHand()));
            }
        }

        return ActionResult.success(level.isClient);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        BlockPos blockPos = raycast(level, player, RaycastContext.FluidHandling.NONE).getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (player.isSneaking() && blockState.isAir()) {
            if (!level.isClient()) {
                toggleMode(stack, player);
            }

            return TypedActionResult.success(stack);
        }

        return TypedActionResult.fail(stack);
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState blockState) {
        // Handle cobweb like a sword
        if (blockState.isOf(Blocks.COBWEB)) {
            return 15.0f;
        }

        return super.getMiningSpeedMultiplier(stack, blockState);
    }

    @Override
    public boolean isSuitableFor(ItemStack stack, BlockState blockState) {
        // Handle cobweb like a sword
        return blockState.isOf(Blocks.COBWEB) || super.isSuitableFor(stack, blockState);
    }

    public void toggleMode(ItemStack stack, PlayerEntity player) {
        NbtCompound compoundTag = stack.getNbt();
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

        player.sendMessage(Text.translatable("notification.justapaxel.switch_mode", Text.literal(mode.toString())), true);
    }

    public static PaxelMode getPaxelMode(ItemStack stack) {
        NbtCompound compoundTag = stack.getNbt();
        if (compoundTag == null || !compoundTag.contains(tagName, 8)) { // 8 = TAG_STRING
            compoundTag.putString(tagName, PaxelMode.SHOVEL.name());
        }

        return PaxelMode.valueOf(compoundTag.getString(tagName));
    }

    // WHY AXEITEM.STRIPPED_BLOCKS IS FUCKING PROTECTED WTF? FUCK OOP FUCK ENCAPSULATION BULLSHIT JUST LET ME FUCKING ACCESS IT FUCKING GODDAMN FUCKING IT 
    public class TempAxeItem extends AxeItem {
        public static Map<Block, Block> STRIPPED_BLOCKS = AxeItem.STRIPPED_BLOCKS;
        public TempAxeItem(ToolMaterial material, float attackDamage, float attackSpeed, Item.Settings settings) {
            super(material, attackDamage, attackSpeed, settings);
        }
    } 

    // THIS SHIT TOO
    public class TempShovelItem extends ShovelItem {
        public static Map<Block, BlockState> PATH_STATES = ShovelItem.PATH_STATES;
        public TempShovelItem(ToolMaterial material, float attackDamage, float attackSpeed, Item.Settings settings) {
            super(material, attackDamage, attackSpeed, settings);
        }
    }
}
