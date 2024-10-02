package com.heebin.justapaxel;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JustAPaxel implements ModInitializer {
    public static final String MODID = "justapaxel";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final TagKey<Block> MINEABLE_WITH_PAXEL = TagKey.of(RegistryKeys.BLOCK, new Identifier(MODID, "mineable/paxel"));

    @Override
    public void onInitialize() {
        ModItems.initialize();
    }

    public static class ModItems {
        public static final Item WOODEN_PAXEL = register(new PaxelItem(ToolMaterials.WOOD, 6.0f, -3.2f, new FabricItemSettings().maxDamage(177)), "wooden_paxel");
        public static final Item STONE_PAXEL = register(new PaxelItem(ToolMaterials.STONE, 7.0f, -3.2f, new FabricItemSettings().maxDamage(393)), "stone_paxel");
        public static final Item GOLDEN_PAXEL = register(new PaxelItem(ToolMaterials.GOLD, 6.0f, -3.0f, new FabricItemSettings().maxDamage(96)), "golden_paxel");
        public static final Item IRON_PAXEL = register(new PaxelItem(ToolMaterials.IRON, 6.0f, -3.1f, new FabricItemSettings().maxDamage(750)), "iron_paxel");
        public static final Item DIAMOND_PAXEL = register(new PaxelItem(ToolMaterials.DIAMOND, 5.0f, -3.0f, new FabricItemSettings().maxDamage(4683)), "diamond_paxel");
        public static final Item NETHERITE_PAXEL = register(new PaxelItem(ToolMaterials.NETHERITE, 5.0f, -3.0f, new FabricItemSettings().maxDamage(6093).fireproof()), "netherite_paxel");

        public static Item register(Item item, String id) {
            return Registry.register(Registries.ITEM, new Identifier(MODID, id), item);
        }

        public static void initialize() {
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((itemGroup) -> itemGroup.add(WOODEN_PAXEL));
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((itemGroup) -> itemGroup.add(STONE_PAXEL));
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((itemGroup) -> itemGroup.add(GOLDEN_PAXEL));
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((itemGroup) -> itemGroup.add(IRON_PAXEL));
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((itemGroup) -> itemGroup.add(DIAMOND_PAXEL));
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((itemGroup) -> itemGroup.add(NETHERITE_PAXEL));
        }
    }
}
