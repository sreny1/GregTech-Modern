package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.client.renderer.GTRendererProvider;
import com.gregtechceu.gtceu.common.blockentity.FOUPFunnelBlockEntity;
import com.gregtechceu.gtceu.common.blockentity.FOUPRailBlockEntity;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;

import static com.gregtechceu.gtceu.api.registry.GTRegistries.REGISTRATE;

/**
 * @author KilaBash
 * @date 2023/2/13
 * @implNote GTBlockEntities
 */
public class GTBlockEntities {
    @SuppressWarnings("unchecked")
    public static final BlockEntityEntry<CableBlockEntity> CABLE = REGISTRATE
            .blockEntity("cable", CableBlockEntity::create)
            .onRegister(CableBlockEntity::onBlockEntityRegister)
            .validBlocks(GTBlocks.CABLE_BLOCKS.values().toArray(BlockEntry[]::new))
            .register();

    @SuppressWarnings("unchecked")
    public static final BlockEntityEntry<FluidPipeBlockEntity>  FLUID_PIPE = REGISTRATE
            .blockEntity("fluid_pipe", FluidPipeBlockEntity::create)
            .onRegister(FluidPipeBlockEntity::onBlockEntityRegister)
            .validBlocks(GTBlocks.FLUID_PIPE_BLOCKS.values().toArray(BlockEntry[]::new))
            .register();

    public static final BlockEntityEntry<FOUPRailBlockEntity> FOUP_RAIL = REGISTRATE
            .blockEntity("foup_rail", FOUPRailBlockEntity::create)
            .onRegister(FOUPRailBlockEntity::onBlockEntityRegister)
            .renderer(() -> GTRendererProvider::getOrCreate)
            .validBlocks(GTBlocks.FOUP_RAIL)
            .register();

    public static final BlockEntityEntry<FOUPFunnelBlockEntity> FOUP_FUNNEL = REGISTRATE
            .blockEntity("foup_funnel", FOUPFunnelBlockEntity::create)
            .onRegister(FOUPFunnelBlockEntity::onBlockEntityRegister)
            .renderer(() -> GTRendererProvider::getOrCreate)
            .validBlocks(GTBlocks.FOUP_FUNNEL)
            .register();

    public static void init() {

    }
}
