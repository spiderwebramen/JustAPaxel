package com.heebin.justapaxel;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JustAPaxelClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(JustAPaxel.MODID);

    @Override
    public void onInitializeClient() {
        registerPaxelProperties(JustAPaxel.ModItems.WOODEN_PAXEL);
        registerPaxelProperties(JustAPaxel.ModItems.STONE_PAXEL);
        registerPaxelProperties(JustAPaxel.ModItems.GOLDEN_PAXEL);
        registerPaxelProperties(JustAPaxel.ModItems.IRON_PAXEL);
        registerPaxelProperties(JustAPaxel.ModItems.DIAMOND_PAXEL);
        registerPaxelProperties(JustAPaxel.ModItems.NETHERITE_PAXEL);
	}

    private static void registerPaxelProperties(Item item) {
        ModelPredicateProviderRegistry.register(item, new Identifier(JustAPaxel.MODID, "hoe"), (stack, level, entity, id) -> {
            return PaxelItem.getPaxelMode(stack) == PaxelItem.PaxelMode.HOE ? 1.0f : 0.0f;
        });
    }
}
