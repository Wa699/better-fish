package com.mod.fish;

import java.util.function.Function;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;

/**
 * 注册本 Mod 的所有物品：9 条生鱼 + 9 条熟鱼。
 * 采用 Fabric 官方 wiki 的泛型注册写法：先造 ResourceKey，setId 后再注册。
 *
 * 食物效果在 26.1 里不在 FoodProperties 里，而是挂在 Consumable 组件上
 * （原版河豚就是 properties.food(Foods.PUFFERFISH, Consumables.PUFFERFISH) 这种写法）。
 *
 * 设计原则：越稀有营养越高、效果越强；生吃普遍弱且有副作用，烤熟后负面消失、正面加强。
 */
public final class ModItems {

	// ===================== 常见鱼 =====================

	/** 银鲫：最普通的淡水鱼。生吃只是充饥，烤熟才会回血。 */
	public static final Item SILVER_CARP = register("silver_carp", p -> foodItem(p, 2, 0.1F, consume()));
	public static final Item COOKED_SILVER_CARP = register("cooked_silver_carp",
		p -> foodItem(p, 5, 0.6F, consume(buff(MobEffects.REGENERATION, 5, 0))));

	/** 溪鳟：溪流里练出来的敏捷。 */
	public static final Item RIVER_TROUT = register("river_trout",
		p -> foodItem(p, 2, 0.15F, consume(buff(MobEffects.SPEED, 8, 0))));
	public static final Item COOKED_RIVER_TROUT = register("cooked_river_trout",
		p -> foodItem(p, 5, 0.6F, consume(buff(MobEffects.SPEED, 30, 0))));

	/** 月水母：会发光，但生吃不干净。 */
	public static final Item MOON_JELLY = register("moon_jelly",
		p -> foodItem(p, 1, 0.1F, consume(
			buff(MobEffects.NIGHT_VISION, 20, 0),
			chance(MobEffects.POISON, 4, 0, 0.15F))));
	public static final Item COOKED_MOON_JELLY = register("cooked_moon_jelly",
		p -> foodItem(p, 4, 0.5F, consume(
			buff(MobEffects.NIGHT_VISION, 60, 0),
			buff(MobEffects.GLOWING, 30, 0))));

	// ===================== 稀有鱼 =====================

	/** 太阳鱼：阳光与再生。 */
	public static final Item SUNFISH = register("sunfish",
		p -> foodItem(p, 3, 0.3F, consume(buff(MobEffects.REGENERATION, 4, 0))));
	public static final Item COOKED_SUNFISH = register("cooked_sunfish",
		p -> foodItem(p, 6, 0.7F, consume(
			buff(MobEffects.REGENERATION, 8, 1),
			buff(MobEffects.FIRE_RESISTANCE, 20, 0))));

	/** 霜米诺：冰寒之身。生吃会冻得发慢，熟吃变成轻盈（缓降 + 水下呼吸）。 */
	public static final Item FROST_MINNOW = register("frost_minnow",
		p -> foodItem(p, 2, 0.25F, consume(
			buff(MobEffects.SLOW_FALLING, 30, 0),
			chance(MobEffects.SLOWNESS, 5, 0, 0.3F))));
	public static final Item COOKED_FROST_MINNOW = register("cooked_frost_minnow",
		p -> foodItem(p, 5, 0.6F, consume(
			buff(MobEffects.SLOW_FALLING, 90, 0),
			buff(MobEffects.WATER_BREATHING, 30, 0))));

	/** 水晶鳗：结晶外壳，硬得像护甲。 */
	public static final Item CRYSTAL_EEL = register("crystal_eel",
		p -> foodItem(p, 4, 0.4F, consume(
			buff(MobEffects.RESISTANCE, 10, 0),
			buff(MobEffects.GLOWING, 10, 0))));
	public static final Item COOKED_CRYSTAL_EEL = register("cooked_crystal_eel",
		p -> foodItem(p, 7, 0.7F, consume(
			buff(MobEffects.RESISTANCE, 20, 1),
			buff(MobEffects.ABSORPTION, 30, 1))));

	// ===================== 传说鱼 =====================

	/** 深渊鲸：深海之王。生吃就有长时水下呼吸，但深海压力会让你变慢。 */
	public static final Item ABYSSAL_WHALE = register("abyssal_whale",
		p -> foodItem(p, 6, 0.7F, consume(
			buff(MobEffects.WATER_BREATHING, 60, 0),
			buff(MobEffects.DOLPHINS_GRACE, 30, 0),
			buff(MobEffects.NIGHT_VISION, 30, 0),
			buff(MobEffects.SLOWNESS, 10, 0))));
	public static final Item COOKED_ABYSSAL_WHALE = register("cooked_abyssal_whale",
		p -> foodItem(p, 9, 0.9F, consume(
			buff(MobEffects.WATER_BREATHING, 180, 0),
			buff(MobEffects.DOLPHINS_GRACE, 90, 0),
			buff(MobEffects.NIGHT_VISION, 90, 0),
			buff(MobEffects.RESISTANCE, 30, 0))));

	/** 金龙鱼：龙血。生吃力量强劲，但极耗体力（饥饿）。 */
	public static final Item GOLDEN_DRAGONFISH = register("golden_dragonfish",
		p -> foodItem(p, 6, 0.8F, consume(
			buff(MobEffects.STRENGTH, 15, 0),
			buff(MobEffects.HEALTH_BOOST, 60, 0),
			buff(MobEffects.HUNGER, 10, 0))));
	public static final Item COOKED_GOLDEN_DRAGONFISH = register("cooked_golden_dragonfish",
		p -> foodItem(p, 10, 1.0F, consume(
			buff(MobEffects.STRENGTH, 30, 1),
			buff(MobEffects.HEALTH_BOOST, 120, 1),
			buff(MobEffects.RESISTANCE, 30, 0),
			buff(MobEffects.FIRE_RESISTANCE, 60, 0))));

	/** 星辉鱼：星光加身，跳得高、落得轻。 */
	public static final Item STARFISH = register("starfish",
		p -> foodItem(p, 5, 0.8F, consume(
			buff(MobEffects.JUMP_BOOST, 30, 1),
			buff(MobEffects.SLOW_FALLING, 60, 0),
			buff(MobEffects.GLOWING, 30, 0))));
	public static final Item COOKED_STARFISH = register("cooked_starfish",
		p -> foodItem(p, 9, 1.0F, consume(
			buff(MobEffects.JUMP_BOOST, 60, 1),
			buff(MobEffects.SLOW_FALLING, 120, 0),
			buff(MobEffects.REGENERATION, 5, 1),
			buff(MobEffects.ABSORPTION, 60, 1))));

	private ModItems() {
	}

	// ===================== 工具方法 =====================

	/** 造一个带食物属性 + 食用效果的普通 Item。 */
	private static Item foodItem(Item.Properties properties, int nutrition, float saturationModifier, Consumable consumable) {
		FoodProperties food = new FoodProperties.Builder()
			.nutrition(nutrition)
			.saturationModifier(saturationModifier)
			.build();

		return new Item(properties.food(food, consumable));
	}

	/** 100% 生效的状态效果。seconds = 秒，amplifier = 等级-1（0 即 I 级）。 */
	private static ConsumeEffect buff(Holder<MobEffect> effect, float seconds, int amplifier) {
		return new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(effect, Math.round(seconds * 20.0F), amplifier));
	}

	/** 概率生效的状态效果（用于「有几率中毒/被冻住」这类设定）。 */
	private static ConsumeEffect chance(Holder<MobEffect> effect, float seconds, int amplifier, float probability) {
		return new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(effect, Math.round(seconds * 20.0F), amplifier), probability);
	}

	/** 把若干条食用效果打包成一个 Consumable。 */
	private static Consumable consume(ConsumeEffect... effects) {
		Consumable.Builder builder = Consumable.builder();
		for (ConsumeEffect effect : effects) {
			builder.onConsume(effect);
		}
		return builder.build();
	}

	/**
	 * 通用注册方法（Fabric 官方写法）。
	 * 1. 用 modid + 名字造一个 ResourceKey
	 * 2. 把 id set 进 Properties（新版必须有，否则启动时报 "Item id not set"）
	 * 3. 注册进 BuiltInRegistries.ITEM
	 */
	public static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ABetterFishingExperience.MOD_ID, name));
		T item = itemFactory.apply(settings.setId(itemKey));
		return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
	}

	/** 简化版：默认给一份空的 Properties。 */
	private static Item register(String name, Function<Item.Properties, Item> itemFactory) {
		return register(name, itemFactory, new Item.Properties());
	}

	/** 在主类的 onInitialize 里调用，触发上面所有 static 字段的注册。 */
	public static void initialize() {
		// 空方法：调用它时 JVM 会加载本类，从而执行所有 static final 字段的初始化。
	}
}
