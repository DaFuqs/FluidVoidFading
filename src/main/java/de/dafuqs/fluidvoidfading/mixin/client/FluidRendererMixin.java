package de.dafuqs.fluidvoidfading.mixin.client;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.*;
import net.fabricmc.fabric.api.transfer.v1.fluid.*;
import net.minecraft.block.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.*;
import net.minecraft.client.texture.*;
import net.minecraft.fluid.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {

    @Shadow
    protected abstract int getLight(BlockRenderView world, BlockPos pos);

    @Shadow
    private Sprite waterOverlaySprite;

    @Shadow
    private static boolean isSameFluid(FluidState a, FluidState b) {
        throw new AssertionError();
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void fluidVoidFading$render(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        if (fluidVoidFading$isDirectlyAboveVoid(world, pos)) {
            fluidVoidFading$renderFluidInVoid(world, pos, vertexConsumer, fluidState);
        }
    }

    @Unique
    private static boolean fluidVoidFading$isDirectlyAboveVoid(BlockView world, BlockPos blockPos) {
        return blockPos.getY() == world.getBottomY();
    }

    @Unique
    private void fluidVoidFading$renderFluidInVoid(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, FluidState fluidState) {
        Fluid fluid = fluidState.getFluid();
        if (fluid != Fluids.EMPTY) {
            BlockState northBlockState = world.getBlockState(pos.offset(Direction.NORTH));
            FluidState northFluidState = northBlockState.getFluidState();
            BlockState southBlockState = world.getBlockState(pos.offset(Direction.SOUTH));
            FluidState southFluidState = southBlockState.getFluidState();
            BlockState westBlockState = world.getBlockState(pos.offset(Direction.WEST));
            FluidState westFluidState = westBlockState.getFluidState();
            BlockState eastBlockState = world.getBlockState(pos.offset(Direction.EAST));
            FluidState eastFluidState = eastBlockState.getFluidState();

            boolean sameFluidNorth = isSameFluid(fluidState, northFluidState);
            boolean sameFluidSouth = isSameFluid(fluidState, southFluidState);
            boolean sameFluidWest = isSameFluid(fluidState, westFluidState);
            boolean sameFluidEast = isSameFluid(fluidState, eastFluidState);

            float brightnessUp = world.getBrightness(Direction.UP, true);
            float brightnessNorth = world.getBrightness(Direction.NORTH, true);
            float brightnessWest = world.getBrightness(Direction.WEST, true);
            float n = 1.0F;
            float o = 1.0F;
            float p = 1.0F;
            float q = 1.0F;
            float d = (pos.getX() & 15);
            float e = (pos.getY() & 15);
            float r = (pos.getZ() & 15);
            float t = 0.0F;
            float ca = 0;
            float cb;
            float u1;
            float u2;

            int light = this.getLight(world, pos);

            FluidVariant fluidVariant = FluidVariant.of(fluid);
            Sprite sprite = FluidVariantRendering.getSprites(fluidVariant)[1];
            int color = FluidVariantRendering.getColor(fluidVariant, world, pos);
            int[] colors = unpackColor(color);

            float redF = colors[1] / 255F;
            float greenF = colors[2] / 255F;
            float blueF = colors[3] / 255F;

            float alpha1 = colors[0] / 255F;
            float alpha2 = 0.3F * (colors[0] / 255F);
            float alpha3 = 0.0F;

            for (Direction direction : Direction.Type.HORIZONTAL) { // directions
                float x1;
                float z1;
                float x2;
                float z2;
                boolean shouldRender;
                if (direction == Direction.NORTH) {
                    ca = n;
                    cb = q;
                    x1 = d;
                    x2 = d + 1.0F;
                    z1 = r + 0.001F;
                    z2 = r + 0.001F;
                    shouldRender = sameFluidNorth;
                } else if (direction == Direction.SOUTH) {
                    cb = o;
                    x1 = d + 1.0F;
                    x2 = d;
                    z1 = r + 1.0F - 0.001F;
                    z2 = r + 1.0F - 0.001F;
                    shouldRender = sameFluidSouth;
                } else if (direction == Direction.WEST) {
                    ca = o;
                    cb = n;
                    x1 = d + 0.001F;
                    x2 = d + 0.001F;
                    z1 = r + 1.0F;
                    z2 = r;
                    shouldRender = sameFluidWest;
                } else {
                    ca = q;
                    cb = p;
                    x1 = d + 1.0F - 0.001F;
                    x2 = d + 1.0F - 0.001F;
                    z1 = r;
                    z2 = r + 1.0F;
                    shouldRender = sameFluidEast;
                }

                if (!shouldRender) {
                    u1 = sprite.getFrameU(0.F);
                    u2 = sprite.getFrameU(0.5F);
                    float v1 = sprite.getFrameV((1.0F - ca) * 0.5F);
                    float v2 = sprite.getFrameV((1.0F - cb) * 0.5F);
                    float v3 = sprite.getFrameV(0.5F);
                    
                    float sidedBrightness = direction.getAxis() == Direction.Axis.Z ? brightnessNorth : brightnessWest;
                    float red = brightnessUp * sidedBrightness * redF;
                    float green = brightnessUp * sidedBrightness * greenF;
                    float blue = brightnessUp * sidedBrightness * blueF;
                    vertex(vertexConsumer, x1, e + ca - 1, z1, red, green, blue, u1, v1, light, alpha1);
                    vertex(vertexConsumer, x2, e + cb - 1, z2, red, green, blue, u2, v2, light, alpha1);
                    vertex(vertexConsumer, x2, e + t - 1, z2, red, green, blue, u2, v3, light, alpha2);
                    vertex(vertexConsumer, x1, e + t - 1, z1, red, green, blue, u1, v3, light, alpha2);

                    vertex(vertexConsumer, x1, e + ca - 2, z1, red, green, blue, u1, v1, light, alpha2);
                    vertex(vertexConsumer, x2, e + cb - 2, z2, red, green, blue, u2, v2, light, alpha2);
                    vertex(vertexConsumer, x2, e + t - 2, z2, red, green, blue, u2, v3, light, alpha3);
                    vertex(vertexConsumer, x1, e + t - 2, z1, red, green, blue, u1, v3, light, alpha3);
                    if (sprite != this.waterOverlaySprite) {
                        vertex(vertexConsumer, x1, e + t - 1, z1, red, green, blue, u1, v3, light, alpha2);
                        vertex(vertexConsumer, x2, e + t - 1, z2, red, green, blue, u2, v3, light, alpha2);
                        vertex(vertexConsumer, x2, e + cb - 1, z2, red, green, blue, u2, v2, light, alpha1);
                        vertex(vertexConsumer, x1, e + ca - 1, z1, red, green, blue, u1, v1, light, alpha1);

                        vertex(vertexConsumer, x1, e + t - 2, z1, red, green, blue, u1, v3, light, alpha3);
                        vertex(vertexConsumer, x2, e + t - 2, z2, red, green, blue, u2, v3, light, alpha3);
                        vertex(vertexConsumer, x2, e + cb - 2, z2, red, green, blue, u2, v2, light, alpha2);
                        vertex(vertexConsumer, x1, e + ca - 2, z1, red, green, blue, u1, v1, light, alpha2);
                    }
                }
            }
        }

    }
    
    private void vertex(VertexConsumer vertexConsumer, float x, float y, float z, float red, float green, float blue, float u, float v, int light, float alpha) {
        vertexConsumer.vertex(x, y, z).color(red, green, blue, alpha).texture(u, v).light(light).normal(0.0F, 1.0F, 0.0F);
    }
    
    private static int[] unpackColor(int color) {
        final int[] colors = new int[4];
        colors[0] = color >> 24 & 0xff; // alpha
        colors[1] = color >> 16 & 0xff; // red
        colors[2] = color >> 8 & 0xff; // green
        colors[3] = color & 0xff; // blue
        return colors;
    }

}
