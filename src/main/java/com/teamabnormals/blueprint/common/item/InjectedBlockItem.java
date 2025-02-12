package com.teamabnormals.blueprint.common.item;

import com.google.common.collect.Maps;
import com.teamabnormals.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import java.util.Map;

/**
 * A {@link BlockItem} extension that fills itself after a defined {@link #followItem}.
 */
public class InjectedBlockItem extends BlockItem {
	private static final Map<Item, TargetedItemCategoryFiller> FILLER_MAP = Maps.newHashMap();
	private final Item followItem;

	public InjectedBlockItem(Item followItem, Block block, Properties builder) {
		super(block, builder);
		this.followItem = followItem;
		FILLER_MAP.put(followItem, new TargetedItemCategoryFiller(() -> followItem));
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		FILLER_MAP.get(this.followItem).fillItem(this.asItem(), group, items);
	}
}
