package de.dafuqs.fluidvoidfading.mixin.client;

import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.util.ColorU8;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.light.LightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadViewMutable;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.util.DirectionUtil;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Pseudo
@Mixin(value = DefaultFluidRenderer.class, remap = false)
public abstract class SodiumDefaultFluidRendererMixin {
    @Shadow
    @Final
    private BlockPos.MutableBlockPos scratchPos;

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
    protected abstract void writeQuad(ChunkModelBuilder builder, TranslucentGeometryCollector collector, Material material, BlockPos offset, ModelQuadView quad, ModelQuadFacing facing, boolean flip);

    @Shadow
    @Final
    private float[] brightness;

    @Shadow
    @Final
    private QuadLightData quadLightData;
    
    @Shadow @Final public static float EPSILON;

    @Shadow
    @Final
    private LightPipeline smoothLighter;

    @Shadow
    @Final
    private LightPipeline flatLighter;

    @Shadow
    @Final
    private ChunkVertexEncoder.Vertex[] vertices;

    @Inject(method = "render", at = @At("RETURN"))
    public void render(LevelSlice level, BlockState blockState, FluidState fluidState, BlockPos blockPos, BlockPos offset, TranslucentGeometryCollector collector, ChunkModelBuilder meshBuilder, Material material, ColorProvider<FluidState> colorProvider, FluidModel sprites, CallbackInfo ci) {
        if (blockPos.getY() != level.getMinY())
            return;

        boolean isWater = fluidState.is(FluidTags.WATER);
        final ModelQuadViewMutable quad = this.quad;

        LightPipeline lighter = isWater && level.useAmbientOcclusion() ? this.smoothLighter : this.flatLighter;

        quad.setFlags(0);
        for (Direction dir : DirectionUtil.HORIZONTAL_DIRECTIONS) {
            BlockState adjBlock = level.getBlockState(this.scratchPos.setWithOffset(blockPos, dir));
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
            
            TextureAtlasSprite sprite = sprites.flowingMaterial().sprite();

            float u1 = sprite.getU(0.5F);
            float u2 = sprite.getU(0.0F);
            float v1 = sprite.getV(0F);
            float v2 = sprite.getV(0.5F);

            quad.setSprite(sprite);

            setVertex(quad, 0, x2, 1F, z2, u2, v1);
            setVertex(quad, 1, x2, EPSILON, z2, u2, v2);
            setVertex(quad, 2, x1, EPSILON, z1, u1, v2);
            setVertex(quad, 3, x1, 1F, z1, u1, v1);
            float br = dir.getAxis() == Direction.Axis.Z ? 0.8F : 0.6F;

            ModelQuadFacing facing = ModelQuadFacing.fromDirection(dir);

            lighter.calculate(quad, blockPos, this.quadLightData, null, dir, false, false);
            colorProvider.getColors(level, blockPos, this.scratchPos, fluidState, quad, this.quadColors, level.hasBiomeBlend());

            int[] original = new int[]{
                    ColorARGB.toABGR(this.quadColors[0]),
                    ColorARGB.toABGR(this.quadColors[1]),
                    ColorARGB.toABGR(this.quadColors[2]),
                    ColorARGB.toABGR(this.quadColors[3])
            };

            BlockPos downPos1 = offset.below(1);
            this.fluidvoidfading$updateQuadWithAlpha(quad, facing, br, original, 1F, 0.3F);
            fluidvoidfading$writeTranslucentQuad(meshBuilder, collector, material, downPos1, quad, facing, false);
            fluidvoidfading$writeTranslucentQuad(meshBuilder, collector, material, downPos1, quad, facing.getOpposite(), true);

            BlockPos downPos2 = offset.below(2);
            this.fluidvoidfading$updateQuadWithAlpha(quad, facing, br, original, 0.3F, 0F);
            fluidvoidfading$writeTranslucentQuad(meshBuilder, collector, material, downPos2, quad, facing, false);
            fluidvoidfading$writeTranslucentQuad(meshBuilder, collector, material, downPos2, quad, facing.getOpposite(), true);
        }
    }

    @Inject(method = "isSideExposedOffset", at = @At("HEAD"), cancellable = true)
    private void fluidVoidFading$isSideExposed(BlockAndTintGetter world, BlockState ownBlockState, BlockPos originPos, Direction dir, float height, CallbackInfoReturnable<Boolean> cir) {
        if (dir == Direction.DOWN && originPos.getY() == world.getMinY()) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private void fluidvoidfading$updateQuadWithAlpha(ModelQuadViewMutable quad, ModelQuadFacing facing, float brightness, int[] originalColors, float alphaStart, float alphaEnd) {
        quad.setFaceNormal(facing.isAligned() ? facing.getPackedAlignedNormal() : quad.calculateNormal());
        {
            int original = originalColors[0];
            this.quadColors[0] = ColorABGR.withAlpha(original, alphaStart * ColorU8.byteToNormalizedFloat(ColorABGR.unpackAlpha(original)));
            this.brightness[0] = this.quadLightData.br[0] * brightness;
        }
        {
            int original = originalColors[1];
            this.quadColors[1] = ColorABGR.withAlpha(original, alphaEnd * ColorU8.byteToNormalizedFloat(ColorABGR.unpackAlpha(original)));
            this.brightness[1] = this.quadLightData.br[1] * brightness;
        }
        {
            int original = originalColors[2];
            this.quadColors[2] = ColorABGR.withAlpha(original, alphaEnd * ColorU8.byteToNormalizedFloat(ColorABGR.unpackAlpha(original)));
            this.brightness[2] = this.quadLightData.br[2] * brightness;
        }
        {
            int original = originalColors[3];
            this.quadColors[3] = ColorABGR.withAlpha(original, alphaStart * ColorU8.byteToNormalizedFloat(ColorABGR.unpackAlpha(original)));
            this.brightness[3] = this.quadLightData.br[3] * brightness;
        }
    }

    @Unique
    private void fluidvoidfading$writeTranslucentQuad(ChunkModelBuilder builder, TranslucentGeometryCollector collector, Material material, BlockPos offset, ModelQuadView quad, ModelQuadFacing facing, boolean flip) {
        ChunkVertexEncoder.Vertex[] vertices = this.vertices;

        for(int i = 0; i < 4; ++i) {
            ChunkVertexEncoder.Vertex out = vertices[flip ? 3 - i + 1 & 3 : i];
            out.x = (float)offset.getX() + quad.getX(i);
            out.y = (float)offset.getY() + quad.getY(i);
            out.z = (float)offset.getZ() + quad.getZ(i);
            out.color = this.quadColors[i];
            out.ao = this.brightness[i];
            out.u = quad.getTexU(i);
            out.v = quad.getTexV(i);
            out.light = this.quadLightData.lm[i];
        }

        TextureAtlasSprite sprite = quad.getSprite();
        if (sprite != null) {
            builder.addSprite(sprite);
        }

        if (collector != null) {
            int normal;
            if (facing.isAligned()) {
                normal = facing.getPackedAlignedNormal();
            } else {
                normal = quad.getFaceNormal();
            }

            if (flip) {
                normal = NormI8.flipPacked(normal);
            }

            if (collector.appendQuad(vertices, facing, normal)) {
                return;
            }
        }

        ChunkMeshBufferBuilder vertexBuffer = builder.getVertexBuffer(facing);
        vertexBuffer.push(vertices, material);
    }
}