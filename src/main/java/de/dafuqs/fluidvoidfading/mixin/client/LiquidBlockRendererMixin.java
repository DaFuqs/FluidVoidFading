package de.dafuqs.fluidvoidfading.mixin.client;

import com.llamalad7.mixinextras.sugar.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.material.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(LiquidBlockRenderer.class)
public abstract class LiquidBlockRendererMixin {
    
    @Shadow
    private static boolean isNeighborSameFluid(FluidState firstState, FluidState secondState) {
        throw new AssertionError();
    }

    @Final
	@Shadow
    private TextureAtlasSprite waterOverlay;

    @Shadow
    protected abstract int getLightColor(BlockAndTintGetter level, BlockPos pos);
    
    @Unique
    private static boolean fluidVoidFading$isDirectlyAboveVoid(BlockGetter world, BlockPos blockPos) {
        return blockPos.getY() == world.getMinY();
    }
    
    @ModifyVariable(method = "tesselate", at = @At("STORE"), name = "flag2")
    private boolean injected(boolean x, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        return fluidVoidFading$isDirectlyAboveVoid(blockAndTintGetter, blockPos) ? false : x;
    }
    
    @Inject(method = "tesselate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;isNeighborStateHidingOverlay(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"))
    public void render(BlockAndTintGetter level, BlockPos pos, VertexConsumer consumer, BlockState blockState, FluidState fluidState, CallbackInfo ci,
                       @Local(ordinal = 0) int color, @Local TextureAtlasSprite[] sprites,
                       @Local(ordinal = 3) FluidState north, @Local(ordinal = 4) FluidState south, @Local(ordinal = 5) FluidState west, @Local(ordinal = 6) FluidState east) {
        if (pos.getY() != level.getMinY())
            return;

        Fluid fluid = fluidState.getType();
        if (fluid != Fluids.EMPTY) {
            float brightnessUp = level.getShade(Direction.UP, true);
            float brightnessNorth = level.getShade(Direction.NORTH, true);
            float brightnessWest = level.getShade(Direction.WEST, true);

            float xLo = (pos.getX() & 0xF);
            float yLo = (pos.getY() & 0xF);
            float zLo = (pos.getZ() & 0xF);

            int light = getLightColor(level, pos);
            TextureAtlasSprite sprite = sprites[1]; // flowing sprite

            float u1 = sprite.getU(0F);
            float u2 = sprite.getU(0.5F);
            float v1 = sprite.getV(0F);
            float v2 = sprite.getV(0.5F);
            int[] colors = unpackColor(color);
            
            float redF = colors[1] / 255F;
            float greenF = colors[2] / 255F;
            float blueF = colors[3] / 255F;
            
            float alpha1 = colors[0] / 255F;
            float alpha2 = 0.3F * alpha1;

            for (Direction dir : Direction.Plane.HORIZONTAL) { // directions
                float x1, z1, x2, z2;
                boolean shouldRender;
                if (dir == Direction.NORTH) {
                    x1 = xLo;
                    x2 = xLo + 1F;
                    z1 = zLo + 0.001F;
                    z2 = zLo + 0.001F;
                    shouldRender = isNeighborSameFluid(fluidState, north);
                } else if (dir == Direction.SOUTH) {
                    x1 = xLo + 1F;
                    x2 = xLo;
                    z1 = zLo + 0.999F;
                    z2 = zLo + 0.999F;
                    shouldRender = isNeighborSameFluid(fluidState, south);
                } else if (dir == Direction.WEST) {
                    x1 = xLo + 0.001F;
                    x2 = xLo + 0.001F;
                    z1 = zLo + 1F;
                    z2 = zLo;
                    shouldRender = isNeighborSameFluid(fluidState, west);
                } else if (dir == Direction.EAST) {
                    x1 = xLo + 0.999F;
                    x2 = xLo + 0.999F;
                    z1 = zLo;
                    z2 = zLo + 1F;
                    shouldRender = isNeighborSameFluid(fluidState, east);
                } else continue;

                if (!shouldRender) {
                    float sidedBrightness = dir.getAxis() == Direction.Axis.Z ? brightnessNorth : brightnessWest;
                    float red = brightnessUp * sidedBrightness * redF;
                    float green = brightnessUp * sidedBrightness * greenF;
                    float blue = brightnessUp * sidedBrightness * blueF;
                    fluidvoidfading$vertex(consumer, x1, yLo + 0F, z1, red, green, blue, u1, v1, light, alpha1);
                    fluidvoidfading$vertex(consumer, x2, yLo + 0F, z2, red, green, blue, u2, v1, light, alpha1);
                    fluidvoidfading$vertex(consumer, x2, yLo - 1F, z2, red, green, blue, u2, v2, light, alpha2);
                    fluidvoidfading$vertex(consumer, x1, yLo - 1F, z1, red, green, blue, u1, v2, light, alpha2);

                    fluidvoidfading$vertex(consumer, x1, yLo - 1F, z1, red, green, blue, u1, v1, light, alpha2);
                    fluidvoidfading$vertex(consumer, x2, yLo - 1F, z2, red, green, blue, u2, v1, light, alpha2);
                    fluidvoidfading$vertex(consumer, x2, yLo - 2F, z2, red, green, blue, u2, v2, light, 0F);
                    fluidvoidfading$vertex(consumer, x1, yLo - 2F, z1, red, green, blue, u1, v2, light, 0F);
                    if (sprite != waterOverlay) {
                        fluidvoidfading$vertex(consumer, x1, yLo - 1F, z1, red, green, blue, u1, v2, light, alpha2);
                        fluidvoidfading$vertex(consumer, x2, yLo - 1F, z2, red, green, blue, u2, v2, light, alpha2);
                        fluidvoidfading$vertex(consumer, x2, yLo + 0F, z2, red, green, blue, u2, v1, light, alpha1);
                        fluidvoidfading$vertex(consumer, x1, yLo + 0F, z1, red, green, blue, u1, v1, light, alpha1);

                        fluidvoidfading$vertex(consumer, x1, yLo - 2F, z1, red, green, blue, u1, v2, light, 0F);
                        fluidvoidfading$vertex(consumer, x2, yLo - 2F, z2, red, green, blue, u2, v2, light, 0F);
                        fluidvoidfading$vertex(consumer, x2, yLo - 1F, z2, red, green, blue, u2, v1, light, alpha2);
                        fluidvoidfading$vertex(consumer, x1, yLo - 1F, z1, red, green, blue, u1, v1, light, alpha2);
                    }
                }
            }
        }
    }
    
    @Unique
    private static int[] unpackColor(int color) {
        final int[] colors = new int[4];
        colors[0] = color >> 24 & 0xff; // alpha
        colors[1] = color >> 16 & 0xff; // red
        colors[2] = color >> 8 & 0xff; // green
        colors[3] = color & 0xff; // blue
        return colors;
    }

    @Unique
    private void fluidvoidfading$vertex(VertexConsumer vertexConsumer, float x, float y, float z, float red, float green, float blue, float u, float v, int light, float alpha) {
        vertexConsumer.addVertex(x, y, z).setColor(red, green, blue, alpha).setUv(u, v).setLight(light).setNormal(0F, 1F, 0F);
    }
}
