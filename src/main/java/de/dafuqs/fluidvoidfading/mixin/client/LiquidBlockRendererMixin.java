package de.dafuqs.fluidvoidfading.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidRenderer.class)
public abstract class LiquidBlockRendererMixin {

    @Shadow
    protected abstract int getLightCoords(BlockAndTintGetter level, BlockPos pos);

    @Shadow
    private static boolean isNeighborSameFluid(FluidState fluidState, FluidState neighborFluidState) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @ModifyVariable(method = "tesselate", at = @At(value = "STORE", ordinal = 0), name = "renderDown")
    public boolean fluidVoidFading$modifyRenderDown(boolean renderDown, BlockAndTintGetter level, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState) {
        if(pos.getX() == level.getMinY()) {
            return false;
        }
        return renderDown;
    }

    @Inject(method = "tesselate", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/FluidRenderer;getLightCoords(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I",
            ordinal = 2, shift = At.Shift.AFTER))
    public void fluidVoidFading$render(BlockAndTintGetter level, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState, CallbackInfo ci,
        @Local(name = "x") float x, @Local(name = "y") float y, @Local(name = "z") float z,
        @Local(name = "model") FluidModel model, @Local(name = "tintColor") int tintColor, @Local(name = "cardinalLighting") CardinalLighting cardinalLighting,
        @Local(name = "fluidStateNorth") FluidState fluidStateNorth, @Local(name = "fluidStateSouth") FluidState fluidStateSouth, @Local(name = "fluidStateWest") FluidState fluidStateWest, @Local(name = "fluidStateEast") FluidState fluidStateEast,
        @Local(name = "blockStateNorth") BlockState blockStateNorth, @Local(name = "blockStateSouth") BlockState blockStateSouth,
        @Local(name = "blockStateWest") BlockState blockStateWest, @Local(name = "blockStateEast") BlockState blockStateEast) {

        if (pos.getY() == level.getMinY()) {
            boolean renderNorth = !isNeighborSameFluid(fluidState, fluidStateNorth);
            boolean renderSouth = !isNeighborSameFluid(fluidState, fluidStateSouth);
            boolean renderWest = !isNeighborSameFluid(fluidState, fluidStateWest);
            boolean renderEast = !isNeighborSameFluid(fluidState, fluidStateEast);

            VertexConsumer builder = output.getBuilder(ChunkSectionLayer.TRANSLUCENT);
            int sideLightCoords = this.getLightCoords(level, pos);
            for (Direction faceDir : Direction.Plane.HORIZONTAL) {
                float hh0;
                float hh1;
                float x0;
                float z0;
                float x1;
                float z1;
                boolean renderCondition;
                BlockState faceState;
                switch (faceDir) {
                    case NORTH:
                        hh0 = 1.0F;
                        hh1 = 1.0F;
                        x0 = x;
                        x1 = x + 1.0F;
                        z0 = z + 0.001F;
                        z1 = z + 0.001F;
                        renderCondition = renderNorth;
                        faceState = blockStateNorth;
                        break;
                    case SOUTH:
                        hh0 = 1.0F;
                        hh1 = 1.0F;
                        x0 = x + 1.0F;
                        x1 = x;
                        z0 = z + 1.0F - 0.001F;
                        z1 = z + 1.0F - 0.001F;
                        renderCondition = renderSouth;
                        faceState = blockStateSouth;
                        break;
                    case WEST:
                        hh0 = 1.0F;
                        hh1 = 1.0F;
                        x0 = x + 0.001F;
                        x1 = x + 0.001F;
                        z0 = z + 1.0F;
                        z1 = z;
                        renderCondition = renderWest;
                        faceState = blockStateWest;
                        break;
                    case EAST:
                        hh0 = 1.0F;
                        hh1 = 1.0F;
                        x0 = x + 1.0F - 0.001F;
                        x1 = x + 1.0F - 0.001F;
                        z0 = z;
                        z1 = z + 1.0F;
                        renderCondition = renderEast;
                        faceState = blockStateEast;
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }

                if (renderCondition) {
                    TextureAtlasSprite sprite = model.flowingMaterial().sprite();
                    boolean isOverlay = false;
                    if (model.overlayMaterial() != null) {
                        Block relativeBlock = faceState.getBlock();
                        if (relativeBlock instanceof HalfTransparentBlock || relativeBlock instanceof LeavesBlock) {
                            sprite = model.overlayMaterial().sprite();
                            isOverlay = true;
                        }
                    }

                    float u0 = sprite.getU(0.0F);
                    float u1 = sprite.getU(0.5F);
                    float v01 = sprite.getV((1.0F - hh0) * 0.5F);
                    float v02 = sprite.getV((1.0F - hh1) * 0.5F);
                    float v1 = sprite.getV(0.5F);
                    float shadeSide = faceDir.getAxis() == Direction.Axis.Z ? cardinalLighting.north() : cardinalLighting.west();
                    int faceColor = ARGB.scaleRGB(tintColor, cardinalLighting.up() * shadeSide);

                    this.fluidVoidFading$renderBlockWithAlpha(builder, x0, y - 1F + hh0, z0, u0, v01, x1, y - 1F + hh1, z1, u1, v02, x1, y - 1F, z1, u1, v1, x0, y - 1F, z0, u0, v1,
                            faceColor, sideLightCoords, !isOverlay, 1F, 0.3F);
                    this.fluidVoidFading$renderBlockWithAlpha(builder, x0, y - 2F + hh0, z0, u0, v01, x1, y - 2F + hh1, z1, u1, v02, x1, y - 2F, z1, u1, v1, x0, y - 2F, z0, u0, v1,
                            faceColor, sideLightCoords, !isOverlay, 0.3F, 0F);
                }
            }
        }
    }

    @Unique
    private void fluidVoidFading$renderBlockWithAlpha(
            VertexConsumer builder,
            float x0, float y0, float z0, float u0, float v0,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            int color, int lightCoords, boolean addBackFace,
            float alphaTop, float alphaBottom) {
        int colorTop = withAlpha(color, alphaTop * byteToNormalizedFloat(unpackAlpha(color)));
        int colorBottom = withAlpha(color, alphaBottom * byteToNormalizedFloat(unpackAlpha(color)));

        this.fluidVoidFading$vertex(builder, x0, y0, z0, colorTop, u0, v0, lightCoords);
        this.fluidVoidFading$vertex(builder, x1, y1, z1, colorTop, u1, v1, lightCoords);
        this.fluidVoidFading$vertex(builder, x2, y2, z2, colorBottom, u2, v2, lightCoords);
        this.fluidVoidFading$vertex(builder, x3, y3, z3, colorBottom, u3, v3, lightCoords);

        if (addBackFace) {
            this.fluidVoidFading$vertex(builder, x3, y3, z3, colorBottom, u3, v3, lightCoords);
            this.fluidVoidFading$vertex(builder, x2, y2, z2, colorBottom, u2, v2, lightCoords);
            this.fluidVoidFading$vertex(builder, x1, y1, z1, colorTop, u1, v1, lightCoords);
            this.fluidVoidFading$vertex(builder, x0, y0, z0, colorTop, u0, v0, lightCoords);
        }
    }

    private void fluidVoidFading$vertex(VertexConsumer builder, float x, float y, float z, int color, float u, float v, int lightCoords) {
        builder.addVertex(x, y, z, color, u, v, OverlayTexture.NO_OVERLAY, lightCoords, 0.0F, 1.0F, 0.0F);
    }

    private static int withAlpha(int rgb, float alpha) {
        return withAlpha(rgb, normalizedFloatToByte(alpha));
    }

    private static int normalizedFloatToByte(float value) {
        return (int)(value * 255.0F) & 255;
    }

    private static int withAlpha(int rgb, int alpha) {
        return alpha << 24 | rgb & 16777215;
    }

    private static int unpackAlpha(int color) {
        return color >> 24 & 255;
    }

    private static float byteToNormalizedFloat(int value) {
        return (float)value * 0.003921569F;
    }

}
