package de.dafuqs.fluidvoidfading.mixin.client;

import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.util.ColorU8;
import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.light.LightMode;
import net.caffeinemc.mods.sodium.client.model.light.LightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.LightPipelineProvider;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadViewMutable;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFlags;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.services.PlatformBlockAccess;
import net.caffeinemc.mods.sodium.client.util.DirectionUtil;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static de.dafuqs.fluidvoidfading.FluidVoidFading.EPSILON;

@Pseudo
@Mixin(value = DefaultFluidRenderer.class, remap = false)
public abstract class SodiumFluidRendererMixin {
    @Shadow
    @Final
    private BlockPos.Mutable scratchPos;

    @Shadow
    @Final
    private ModelQuadViewMutable quad;
    @Shadow
    @Final
    private int[] quadColors;

    @Shadow
    private static void setVertex(ModelQuadViewMutable quad, int i, float x, float y, float z, float u, float v) {
    }

    @Shadow
    @Final
    private LightPipelineProvider lighters;

    @Shadow
    protected abstract void writeQuad(ChunkModelBuilder builder, TranslucentGeometryCollector collector, Material material, BlockPos offset, ModelQuadView quad, ModelQuadFacing facing, boolean flip);

    @Shadow
    @Final
    private float[] brightness;

    @Shadow
    @Final
    private QuadLightData quadLightData;

    @Inject(method = "render", at = @At("RETURN"))
    public void fluidVoidFading$render(LevelSlice level, BlockState blockState, FluidState fluidState, BlockPos blockPos, BlockPos offset, TranslucentGeometryCollector collector, ChunkModelBuilder meshBuilder, Material material, ColorProvider<FluidState> colorProvider, Sprite[] sprites, CallbackInfo ci) {
        if (blockPos.getY() != level.getBottomY())
            return;
        boolean isWater = fluidState.isIn(FluidTags.WATER);

        final ModelQuadViewMutable quad = this.quad;

        LightMode lightMode = isWater && MinecraftClient.isAmbientOcclusionEnabled() ? LightMode.SMOOTH : LightMode.FLAT;
        LightPipeline lighter = this.lighters.getLighter(lightMode);

        quad.setFlags(ModelQuadFlags.IS_PARALLEL | ModelQuadFlags.IS_ALIGNED);
        for (Direction dir : DirectionUtil.HORIZONTAL_DIRECTIONS) {
            BlockState adjBlock = level.getBlockState(this.scratchPos.set(blockPos, dir));
            if (!adjBlock.getFluidState().isEmpty())
                continue;

            float x1, x2, z1, z2;
            if (dir == Direction.NORTH) {
                x1 = 0F;
                x2 = 1F;
                z1 = EPSILON;
                z2 = z1;
            } else if (dir == Direction.SOUTH) {
                x1 = 1F;
                x2 = 0F;
                z1 = 1F - EPSILON;
                z2 = z1;
            } else if (dir == Direction.WEST) {
                x1 = EPSILON;
                x2 = x1;
                z1 = 1F;
                z2 = 0F;
            } else if (dir == Direction.EAST) {
                x1 = 1F - EPSILON;
                x2 = x1;
                z1 = 0F;
                z2 = 1F;
            } else continue;

            Sprite sprite = sprites[1];

            boolean isOverlay = false;

            if (sprites.length > 2 && sprites[2] != null &&
                PlatformBlockAccess.getInstance()
                                   .shouldShowFluidOverlay(adjBlock, level, this.scratchPos, fluidState)) {
                sprite = sprites[2];
                isOverlay = true;
            }

            float u1 = sprite.getFrameU(0F);
            float u2 = sprite.getFrameU(0.5F);
            float v1 = sprite.getFrameV(0F);
            float v2 = sprite.getFrameV(0.5F);

            quad.setSprite(sprite);

            setVertex(quad, 0, x2, 1F, z2, u2, v1);
            setVertex(quad, 1, x2, EPSILON, z2, u2, v2);
            setVertex(quad, 2, x1, EPSILON, z1, u1, v2);
            setVertex(quad, 3, x1, 1F, z1, u1, v1);
            float br = dir.getAxis() == Direction.Axis.Z ? 0.8F : 0.6F;

            ModelQuadFacing facing = ModelQuadFacing.fromDirection(dir);

            lighter.calculate(quad, blockPos, this.quadLightData, null, dir, false, false);
            colorProvider.getColors(level, blockPos, this.scratchPos, fluidState, quad, this.quadColors);

            int[] original = new int[]{ColorARGB.toABGR(this.quadColors[0]), ColorARGB.toABGR(this.quadColors[1]),
                ColorARGB.toABGR(this.quadColors[2]), ColorARGB.toABGR(this.quadColors[3])};

            BlockPos downPos1 = offset.offset(Direction.DOWN, 1);
            this.fluidvoidfading$updateQuadWithAlpha(quad, facing, br, original, 1F, 0.3F);
            this.writeQuad(meshBuilder, collector, material, downPos1, quad, facing, false);
            if (!isOverlay)
                this.writeQuad(meshBuilder, collector, material, downPos1, quad, facing.getOpposite(), true);

            BlockPos downPos2 = offset.offset(Direction.DOWN, 2);
            this.fluidvoidfading$updateQuadWithAlpha(quad, facing, br, original, 0.3F, 0F);
            this.writeQuad(meshBuilder, collector, material, downPos2, quad, facing, false);
            if (!isOverlay)
                this.writeQuad(meshBuilder, collector, material, downPos2, quad, facing.getOpposite(), true);
        }
    }

    @Inject(method = "isSideExposed", at = @At("HEAD"), cancellable = true)
    private void fluidVoidFading$isSideExposed(BlockRenderView world, int x, int y, int z, Direction dir, float height, CallbackInfoReturnable<Boolean> cir) {
        if (dir == Direction.DOWN && y == world.getBottomY())
            cir.setReturnValue(false);
    }

    @Unique
    private void fluidvoidfading$updateQuadWithAlpha(ModelQuadViewMutable quad, ModelQuadFacing facing, float brightness, int[] originalColors, float alphaStart, float alphaEnd) {
        quad.setFaceNormal(facing.isAligned() ? facing.getPackedAlignedNormal() : quad.calculateNormal());
        {
            int original = originalColors[0];
            this.quadColors[0] = ColorABGR.withAlpha(original, alphaStart * ColorU8.byteToNormalizedFloat(
                ColorABGR.unpackAlpha(original)));
            this.brightness[0] = this.quadLightData.br[0] * brightness;
        }
        {
            int original = originalColors[1];
            this.quadColors[1] = ColorABGR.withAlpha(original, alphaEnd * ColorU8.byteToNormalizedFloat(
                ColorABGR.unpackAlpha(original)));
            this.brightness[1] = this.quadLightData.br[1] * brightness;
        }
        {
            int original = originalColors[2];
            this.quadColors[2] = ColorABGR.withAlpha(original, alphaEnd * ColorU8.byteToNormalizedFloat(
                ColorABGR.unpackAlpha(original)));
            this.brightness[2] = this.quadLightData.br[2] * brightness;
        }
        {
            int original = originalColors[3];
            this.quadColors[3] = ColorABGR.withAlpha(original, alphaStart * ColorU8.byteToNormalizedFloat(
                ColorABGR.unpackAlpha(original)));
            this.brightness[3] = this.quadLightData.br[3] * brightness;
        }
    }
}
