package com.teamabnormals.blueprint.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

/**
 * A class containing template {@link Block.Properties} and {@link Item.Properties}
 *
 * @author bageldotjpg
 */
public final class PropertyUtil {
	public static final Item.Properties TOOL = new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_TOOLS);
	public static final Item.Properties MISC_TOOL = new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC);

	public static Item.Properties food(FoodProperties food) {
		return new Item.Properties().food(food).tab(CreativeModeTab.TAB_FOOD);
	}

	public static final Block.Properties FLOWER = Block.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS);
	public static final Block.Properties SAPLING = Block.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS);
	public static final Block.Properties LADDER = Block.Properties.of(Material.DECORATION).strength(0.4F).sound(SoundType.LADDER).noOcclusion();
	public static final Block.Properties FLOWER_POT = Block.Properties.of(Material.DECORATION).instabreak().noOcclusion();

	public static Block.Properties thatch(MaterialColor color, SoundType soundType) {
		return Block.Properties.of(Material.GRASS, color).strength(0.5F).sound(soundType).noOcclusion();
	}

	public static boolean never(BlockState state, BlockGetter getter, BlockPos pos) {
		return false;
	}

	public static boolean never(BlockState state, BlockGetter getter, BlockPos pos, EntityType<?> entity) {
		return false;
	}

	public static boolean always(BlockState state, BlockGetter getter, BlockPos pos) {
		return true;
	}

	public static boolean always(BlockState state, BlockGetter getter, BlockPos pos, EntityType<?> entity) {
		return true;
	}

	public static boolean ocelotOrParrot(BlockState state, BlockGetter reader, BlockPos pos, EntityType<?> entity) {
		return entity == EntityType.OCELOT || entity == EntityType.PARROT;
	}

	public static class WoodSetProperties {
		private final MaterialColor woodColor;
		private final SoundType logSound;

		public WoodSetProperties(MaterialColor woodColor) {
			this.woodColor = woodColor;
			this.logSound = SoundType.WOOD;
		}

		public WoodSetProperties(MaterialColor woodColor, SoundType logSound) {
			this.woodColor = woodColor;
			this.logSound = logSound;
		}

		public Block.Properties planks() {
			return Block.Properties.of(Material.WOOD, this.woodColor).strength(2.0F, 3.0F).sound(SoundType.WOOD);
		}

		public Block.Properties log() {
			return Block.Properties.of(Material.WOOD, this.woodColor).strength(2.0F).sound(logSound);
		}

		public Block.Properties leaves() {
			return Block.Properties.of(Material.LEAVES).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion().isValidSpawn(PropertyUtil::ocelotOrParrot).isSuffocating(PropertyUtil::never).isViewBlocking(PropertyUtil::never);
		}

		public Block.Properties pressurePlate() {
			return Block.Properties.of(Material.WOOD, this.woodColor).noCollission().strength(0.5F).sound(SoundType.WOOD);
		}

		public Block.Properties trapdoor() {
			return Block.Properties.of(Material.WOOD, this.woodColor).strength(3.0F).sound(SoundType.WOOD).noOcclusion().isValidSpawn(PropertyUtil::never);
		}

		public Block.Properties button() {
			return Block.Properties.of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD);
		}

		public Block.Properties door() {
			return Block.Properties.of(Material.WOOD, this.woodColor).strength(3.0F).sound(SoundType.WOOD).noOcclusion();
		}

		public Block.Properties beehive() {
			return Block.Properties.of(Material.WOOD, this.woodColor).strength(0.6F).sound(SoundType.WOOD);
		}

		public Block.Properties bookshelf() {
			return Block.Properties.of(Material.WOOD, this.woodColor).strength(1.5F).sound(SoundType.WOOD);
		}

		public Block.Properties chest() {
			return Block.Properties.of(Material.WOOD, this.woodColor).strength(2.5F).sound(SoundType.WOOD);
		}

		public Block.Properties leafCarpet() {
			return Block.Properties.of(Material.CLOTH_DECORATION).strength(0.0F).sound(SoundType.GRASS).noOcclusion();
		}

		public Block.Properties post() {
			return Block.Properties.of(Material.WOOD, this.woodColor).strength(2.0F, 3.0F).sound(this.logSound);
		}
	}

}
