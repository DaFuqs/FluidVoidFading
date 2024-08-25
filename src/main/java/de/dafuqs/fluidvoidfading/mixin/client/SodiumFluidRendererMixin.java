package de.dafuqs.fluidvoidfading.mixin.client;
import net.caffeinemc.mods.sodium.api.util.*;
import net.caffeinemc.mods.sodium.client.util.*;
import net.caffeinemc.mods.sodium.client.world.*;
import net.caffeinemc.mods.sodium.client.services.*;
import net.caffeinemc.mods.sodium.client.model.quad.*;
import net.caffeinemc.mods.sodium.client.model.color.*;
import net.caffeinemc.mods.sodium.client.model.light.*;
import net.caffeinemc.mods.sodium.client.model.light.data.*;
import net.caffeinemc.mods.sodium.client.model.quad.properties.*;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.*;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.*;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.*;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.*;
import net.minecraft.block.*;
import net.minecraft.client.*;
import net.minecraft.client.texture.*;
import net.minecraft.fluid.*;
import net.minecraft.registry.tag.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Pseudo
@Mixin(value = DefaultFluidRenderer.class, remap = false)
public abstract class SodiumFluidRendererMixin {
    
    @Shadow @Final private BlockPos.Mutable scratchPos;
    
    @Shadow @Final private ModelQuadViewMutable quad;
    @Shadow @Final private int[] quadColors;
    
    @Shadow
    private static void setVertex(ModelQuadViewMutable quad, int i, float x, float y, float z, float u, float v) {}

    @Shadow @Final private LightPipelineProvider lighters;

    @Shadow protected abstract void writeQuad(ChunkModelBuilder builder, TranslucentGeometryCollector collector, Material material, BlockPos offset, ModelQuadView quad, ModelQuadFacing facing, boolean flip);

    @Shadow @Final public static float EPSILON;

    @Shadow @Final private float[] brightness;

    @Shadow @Final private QuadLightData quadLightData;

    @Inject(method = "render", at = @At("RETURN"))
    public void fluidVoidFading$render(LevelSlice level, BlockState blockState, FluidState fluidState, BlockPos blockPos, BlockPos offset, TranslucentGeometryCollector collector, ChunkModelBuilder meshBuilder, Material material, ColorProvider<FluidState> colorProvider, Sprite[] sprites, CallbackInfo ci) {
        if (blockPos.getY() == level.getBottomY()) {
            fluidVoidFading$renderFluidInVoid(level, fluidState, blockPos, offset, collector, meshBuilder, material, colorProvider, sprites);
        }
    }

    @Inject(method = "isSideExposed", at = @At("HEAD"), cancellable = true)
    private void fluidVoidFading$isSideExposed(BlockRenderView world, int x, int y, int z, Direction dir, float height, CallbackInfoReturnable<Boolean> cir) {
        if (dir == Direction.DOWN && y == world.getBottomY()) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private void fluidVoidFading$renderFluidInVoid(LevelSlice level, FluidState fluidState, BlockPos blockPos, BlockPos offset, TranslucentGeometryCollector collector, ChunkModelBuilder meshBuilder, Material material, ColorProvider<FluidState> colorProvider, Sprite[] sprites) {
        boolean isWater = fluidState.isIn(FluidTags.WATER);

        final ModelQuadViewMutable quad = this.quad;

        LightMode lightMode = isWater && MinecraftClient.isAmbientOcclusionEnabled() ? LightMode.SMOOTH : LightMode.FLAT;
        LightPipeline lighter = this.lighters.getLighter(lightMode);
        
        quad.setFlags(ModelQuadFlags.IS_PARALLEL | ModelQuadFlags.IS_ALIGNED);
        for (Direction dir : DirectionUtil.HORIZONTAL_DIRECTIONS) {
            BlockState adjBlock = level.getBlockState(this.scratchPos.set(blockPos, dir));
            if (!adjBlock.getFluidState().isEmpty()) continue;
            float x1;
            float z1;
            float x2;
            float z2;

            switch (dir) {
                case NORTH -> {
                    x1 = 0.0f;
                    x2 = 1.0F;
                    z1 = EPSILON;
                    z2 = z1;
                }
                case SOUTH -> {
                    x1 = 1.0F;
                    x2 = 0.0f;
                    z1 = 1.0f - EPSILON;
                    z2 = z1;
                }
                case WEST -> {
                    x1 = EPSILON;
                    x2 = x1;
                    z1 = 1.0F;
                    z2 = 0.0f;
                }
                case EAST -> {
                    x1 = 1.0f - EPSILON;
                    x2 = x1;
                    z1 = 0.0f;
                    z2 = 1.0F;
                }
                default -> {
                    continue;
                }
            }

            Sprite sprite = sprites[1];

            boolean isOverlay = false;

            if (sprites.length > 2 && sprites[2] != null && PlatformBlockAccess.getInstance().shouldShowFluidOverlay(adjBlock, level, this.scratchPos, fluidState)) {
                sprite = sprites[2];
                isOverlay = true;
            }

            float u1 = sprite.getFrameU(0.0F);
            float u2 = sprite.getFrameU(0.5F);
            float v1 = sprite.getFrameV(0.0F);
            float v2 = sprite.getFrameV(0.5F);

            quad.setSprite(sprite);

            setVertex(quad, 0, x2, 1.0F, z2, u2, v1);
            setVertex(quad, 1, x2, EPSILON, z2, u2, v2);
            setVertex(quad, 2, x1, EPSILON, z1, u1, v2);
            setVertex(quad, 3, x1, 1.0F, z1, u1, v1);
            float br = dir.getAxis() == Direction.Axis.Z ? 0.8F : 0.6F;

            ModelQuadFacing facing = ModelQuadFacing.fromDirection(dir);

            lighter.calculate(quad, blockPos, this.quadLightData, null, dir, false, false);
            colorProvider.getColors(level, blockPos, this.scratchPos, fluidState, quad, this.quadColors);

            int[] original = new int[]{ColorARGB.toABGR(this.quadColors[0]), ColorARGB.toABGR(this.quadColors[1]),
                                       ColorARGB.toABGR(this.quadColors[2]), ColorARGB.toABGR(this.quadColors[3])};

            BlockPos downPos1 = offset.offset(Direction.DOWN, 1);
            this.updateQuadWithAlpha(quad, facing, br, original, 1.0F, 0.3F);
            this.writeQuad(meshBuilder, collector, material, downPos1, quad, facing, false);
            if (!isOverlay) {
                this.writeQuad(meshBuilder, collector, material, downPos1, quad, facing.getOpposite(), true);
            }

            BlockPos downPos2 = offset.offset(Direction.DOWN, 2);
            this.updateQuadWithAlpha(quad, facing, br, original, 0.3F, 0.0F);
            this.writeQuad(meshBuilder, collector, material, downPos2, quad, facing, false);
            if (!isOverlay) {
                this.writeQuad(meshBuilder, collector, material, downPos2, quad, facing.getOpposite(), true);
            }
        }
    }

    @Unique
    private void updateQuadWithAlpha(ModelQuadViewMutable quad, ModelQuadFacing facing, float brightness, int[] original, float alphaStart, float alphaEnd) {
        int normal;
        if (facing.isAligned()) {
            normal = facing.getPackedAlignedNormal();
        } else {
            normal = quad.calculateNormal();
        }
        quad.setFaceNormal(normal);
        for (int i = 0; i < 4; ++i) {
            float alpha = i == 0 || i == 3 ? alphaStart : alphaEnd;
            alpha *= ColorU8.byteToNormalizedFloat(ColorABGR.unpackAlpha(original[i]));
            this.quadColors[i] = ColorABGR.withAlpha(original[i], alpha);
            this.brightness[i] = this.quadLightData.br[i] * brightness;
        }
    }
}
