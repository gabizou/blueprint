package com.teamabnormals.blueprint.common.block;

import com.google.common.collect.Maps;
import com.teamabnormals.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import java.util.Map;

/**
 * A {@link Block} extension that fills its item after a defined {@link #followItem}.
 */
public class InjectedBlock extends Block {
	private static final Map<Item, TargetedItemCategoryFiller> FILLER_MAP = Maps.newHashMap();
	private final Item followItem;

	public InjectedBlock(Item followItem, Properties properties) {
		super(properties);
		this.followItem = followItem;
		FILLER_MAP.put(followItem, new TargetedItemCategoryFiller(() -> followItem));
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		FILLER_MAP.get(this.followItem).fillItem(this.asItem(), group, items);
	}
}