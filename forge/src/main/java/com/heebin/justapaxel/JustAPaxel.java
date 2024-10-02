package com.heebin.justapaxel;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.google.common.collect.Sets;
import com.heebin.justapaxel.PaxelItem.PaxelMode;
import com.mojang.logging.LogUtils;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(JustAPaxel.MODID)
public class JustAPaxel {
    public static final String MODID = "justapaxel";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> WOODEN_PAXEL = ITEMS.register("wooden_paxel", () -> new PaxelItem(Tiers.WOOD, 6.0f, -3.2f, new Item.Properties().durability(177)));
    public static final RegistryObject<Item> STONE_PAXEL = ITEMS.register("stone_paxel", () -> new PaxelItem(Tiers.STONE, 7.0f, -3.2f, new Item.Properties().durability(393)));
    public static final RegistryObject<Item> GOLDEN_PAXEL = ITEMS.register("golden_paxel", () -> new PaxelItem(Tiers.GOLD, 6.0f, -3.0f, new Item.Properties().durability(96)));
    public static final RegistryObject<Item> IRON_PAXEL = ITEMS.register("iron_paxel", () -> new PaxelItem(Tiers.IRON, 6.0f, -3.1f, new Item.Properties().durability(750)));
    public static final RegistryObject<Item> DIAMOND_PAXEL = ITEMS.register("diamond_paxel", () -> new PaxelItem(Tiers.DIAMOND, 5.0f, -3.0f, new Item.Properties().durability(4683)));
    public static final RegistryObject<Item> NETHERITE_PAXEL = ITEMS.register("netherite_paxel", () -> new PaxelItem(Tiers.NETHERITE, 5.0f, -3.0f, new Item.Properties().durability(6093).fireResistant()));

    public static final TagKey<Block> MINEABLE_WITH_PAXEL = BlockTags
            .create(new ResourceLocation(MODID, "mineable/paxel"));

    public static final Set<ToolAction> DEFAULT_PAXEL_ACTIONS = Stream.of(
            ToolActions.DEFAULT_AXE_ACTIONS,
            ToolActions.DEFAULT_HOE_ACTIONS,
            ToolActions.DEFAULT_SHOVEL_ACTIONS,
            ToolActions.DEFAULT_PICKAXE_ACTIONS).flatMap(Set::stream)
            .collect(Collectors.toCollection(Sets::newIdentityHashSet));

    public JustAPaxel() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(WOODEN_PAXEL);
            event.accept(STONE_PAXEL);
            event.accept(GOLDEN_PAXEL);
            event.accept(IRON_PAXEL);
            event.accept(DIAMOND_PAXEL);
            event.accept(NETHERITE_PAXEL);
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                registerPaxelProperties(WOODEN_PAXEL.get());
                registerPaxelProperties(STONE_PAXEL.get());
                registerPaxelProperties(GOLDEN_PAXEL.get());
                registerPaxelProperties(IRON_PAXEL.get());
                registerPaxelProperties(DIAMOND_PAXEL.get());
                registerPaxelProperties(NETHERITE_PAXEL.get());
                registerPaxelProperties(DIAMOND_PAXEL.get());
            });
        }

        private static void registerPaxelProperties(Item item) {
            ItemProperties.register(item, new ResourceLocation(JustAPaxel.MODID, "hoe"), (stack, level, entity, id) -> {
                return PaxelItem.getPaxelMode(stack) == PaxelMode.HOE ? 1.0f : 0.0f;
            });
        }
    }
}
