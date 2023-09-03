package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.GTCEu;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/8/10
 * @implNote FOUPCartRenderer
 */
public class FOUPCartRenderer implements IRenderer {

    public static final FOUPCartRenderer INSTANCE = new FOUPCartRenderer();
    public static final IModelRenderer CART = new IModelRenderer(GTCEu.id("block/amhs/foup_cart"));
    public static final IModelRenderer SLING = new IModelRenderer(GTCEu.id("block/amhs/foup_sling"));
    public static final IModelRenderer CASKET = new IModelRenderer(GTCEu.id("block/amhs/foup_casket"));

    private FOUPCartRenderer() {
    }

    @Override
    @Environment(EnvType.CLIENT)
    @Nonnull
    public TextureAtlasSprite getParticleTexture() {
        return CART.getParticleTexture();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void renderItem(ItemStack stack,
                           ItemTransforms.TransformType transformType,
                           boolean leftHand, PoseStack matrixStack,
                           MultiBufferSource buffer, int combinedLight,
                           int combinedOverlay, BakedModel model) {
        CART.renderItem(stack, transformType, leftHand, matrixStack, buffer, combinedLight, combinedOverlay, model);
        SLING.renderItem(stack, transformType, leftHand, matrixStack, buffer, combinedLight, combinedOverlay, model);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean useBlockLight(ItemStack stack) {
        return CART.useBlockLight(stack);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean useAO() {
        return CART.useAO();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<BakedQuad> renderModel(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        var quads = new ArrayList<>(CART.renderModel(level, pos, state, side, rand));
        quads.addAll(SLING.renderModel(level, pos, state, side, rand));
        return quads;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean isGui3d() {
        return CART.isGui3d();
    }

}
