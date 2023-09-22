package com.gregtechceu.gtceu.integration.kjs.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.IndicatorType;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.lowdragmc.lowdraglib.Platform;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;

@SuppressWarnings("unused")
@Accessors(chain = true, fluent = true)
public class OreVeinBuilderJS {
    private final ResourceLocation id;
    @Setter
    public transient int clusterSize, weight;
    @Setter
    public transient float density, discardChanceOnAirExposure;
    @Setter
    public transient IWorldGenLayer layer;
    @Setter
    public transient IndicatorType indicatorType = IndicatorType.SURFACE;
    @Setter
    public transient int indicatorCount = 4;
    @Setter
    public transient HeightRangePlacement heightRange;
    @Setter
    public transient BiomeWeightModifier biomeWeightModifier;
    @Setter
    public VeinGenerator generator;

    private final transient JsonArray dimensionFilter = new JsonArray();
    private final transient JsonArray biomeFilter = new JsonArray();
    @Getter
    private boolean isBuilt = false;

    public OreVeinBuilderJS(ResourceLocation id) {
        this.id = id;
    }

    public OreVeinBuilderJS addSpawnDimension(String dimension) {
        dimensionFilter.add(dimension);
        return this;
    }

    public OreVeinBuilderJS addSpawnBiome(String biome) {
        biomeFilter.add(biome);
        return this;
    }

    public VeinGenerator generatorBuilder(ResourceLocation id) {
        return build().generator(id);
    }

    @HideFromJS
    public GTOreDefinition build() {
        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        HolderSet<DimensionType> dimensions = RegistryCodecs.homogeneousList(Registry.DIMENSION_TYPE_REGISTRY)
            .decode(registryOps, dimensionFilter.size() == 1 ? dimensionFilter.get(0) : dimensionFilter).map(Pair::getFirst).getOrThrow(false, GTCEu.LOGGER::error);
        HolderSet<Biome> biomes = RegistryCodecs.homogeneousList(Registry.BIOME_REGISTRY)
                .decode(registryOps, biomeFilter.size() == 1 ? biomeFilter.get(0) : biomeFilter).map(Pair::getFirst).getOrThrow(false, GTCEu.LOGGER::error);
        isBuilt = true;
        return new GTOreDefinition(id, clusterSize, density, weight, layer, indicatorType, indicatorCount, dimensions, heightRange, discardChanceOnAirExposure, biomes, biomeWeightModifier, generator);
    }

}
