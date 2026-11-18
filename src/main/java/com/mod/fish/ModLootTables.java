package com.mod.fish;

import java.util.List;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;

import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;

/**
 * 把 9 条新鱼注入原版钓鱼战利品表，让它们能被钓到。
 *
 * 26.1 里钓鱼战利品表拆成了子表，鱼在 minecraft:gameplay/fishing/fish。
 * 用 Fabric API 的 LootTableEvents.MODIFY（v3）注入，不用 Mixin。
 *
 * 注意：这里用 modifyPools 往「原版已有的池子」里加条目，而不是新开一个 pool。
 * 若新开 pool，原版池子仍会照常结算，一次抛竿就会同时掉出两条鱼。
 */
public final class ModLootTables {

	// 钓鱼「鱼」子表的 key（原版 cod/salmon 就在这张表里）
	private static final ResourceKey<LootTable> FISHING_FISH = ResourceKey.create(
		Registries.LOOT_TABLE,
		Identifier.fromNamespaceAndPath("minecraft", "gameplay/fishing/fish")
	);

	/** 一天 24000 tick：0 = 日出，13000 = 入夜。 */
	private static final long DAY_LENGTH = 24000L;
	private static final int DAY_START = 0;
	private static final int DAY_END = 13000;
	private static final int NIGHT_START = 13000;
	private static final int NIGHT_END = 23999;

	/**
	 * 雪地生物群系。
	 * 26.1 没有 is_snowy / is_cold 这类群系标签，只能按群系 key 逐个列。
	 */
	private static final List<ResourceKey<Biome>> SNOWY_BIOMES = List.of(
		Biomes.SNOWY_PLAINS,
		Biomes.ICE_SPIKES,
		Biomes.SNOWY_TAIGA,
		Biomes.SNOWY_BEACH,
		Biomes.FROZEN_RIVER,
		Biomes.FROZEN_OCEAN,
		Biomes.DEEP_FROZEN_OCEAN,
		Biomes.GROVE,
		Biomes.SNOWY_SLOPES,
		Biomes.FROZEN_PEAKS,
		Biomes.JAGGED_PEAKS
	);

	private ModLootTables() {
	}

	/** 在主类 onInitialize 里调用，注册战利品表修改监听。 */
	public static void initialize() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			// 只改原版自带的钓鱼表；如果玩家用数据包覆盖了它，就别插手
			if (!FISHING_FISH.equals(key) || !source.isBuiltin()) {
				return;
			}

			// 白天 / 夜晚
			LootItemCondition.Builder day = timeOfDay(registries, DAY_START, DAY_END);
			LootItemCondition.Builder night = timeOfDay(registries, NIGHT_START, NIGHT_END);

			// 霜米诺：在雪地群系，或者正在下雨（冷群系下雨＝下雪）
			LootItemCondition.Builder snowy = snowBiomeOrSnowing(registries);

			// 雷雨
			LootItemCondition.Builder thunder = WeatherCheck.weather()
				.setRaining(true)
				.setThundering(true);

			// 原版这池子权重合计 100（鳕鱼 60 / 鲑鱼 25 / 河豚 13 / 热带鱼 2），rolls=1。
			// 新鱼权重合计 102，基本与原版平分秋色。
			tableBuilder.modifyPools(pool -> pool
				.add(LootItem.lootTableItem(ModItems.SILVER_CARP).setWeight(30))
				.add(LootItem.lootTableItem(ModItems.RIVER_TROUT).setWeight(25))
				.add(LootItem.lootTableItem(ModItems.MOON_JELLY).setWeight(20).when(night))
				.add(LootItem.lootTableItem(ModItems.SUNFISH).setWeight(10).when(day))
				.add(LootItem.lootTableItem(ModItems.FROST_MINNOW).setWeight(8).when(snowy))
				.add(LootItem.lootTableItem(ModItems.CRYSTAL_EEL).setWeight(5).when(thunder))
				.add(LootItem.lootTableItem(ModItems.ABYSSAL_WHALE).setWeight(2))
				.add(LootItem.lootTableItem(ModItems.GOLDEN_DRAGONFISH).setWeight(1).when(thunder))
				.add(LootItem.lootTableItem(ModItems.STARFISH).setWeight(1).when(night))
			);
		});
	}

	/** 按世界时钟取某一段 tick 区间（以一天 24000 tick 循环）。 */
	private static LootItemCondition.Builder timeOfDay(HolderLookup.Provider registries, int from, int to) {
		Holder<WorldClock> clock = registries
			.lookupOrThrow(Registries.WORLD_CLOCK)
			.getOrThrow(WorldClocks.OVERWORLD);

		return TimeCheck.time(clock, IntRange.range(from, to)).setPeriod(DAY_LENGTH);
	}

	/** 雪地群系 或 正在下雨，二者满足其一。 */
	private static LootItemCondition.Builder snowBiomeOrSnowing(HolderLookup.Provider registries) {
		HolderGetter<Biome> biomeGetter = registries.lookupOrThrow(Registries.BIOME);
		List<Holder<Biome>> snowy = SNOWY_BIOMES.stream()
			.map(biomeKey -> (Holder<Biome>) biomeGetter.getOrThrow(biomeKey))
			.toList();

		LootItemCondition.Builder inSnowyBiome = LocationCheck.checkLocation(
			LocationPredicate.Builder.location().setBiomes(HolderSet.direct(snowy))
		);

		return inSnowyBiome.or(WeatherCheck.weather().setRaining(true));
	}
}
