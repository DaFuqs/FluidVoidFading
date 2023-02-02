package de.dafuqs.liquidvoidrenderer.mixin.client;

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
    public void render(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        if (isDirectlyAboveVoid(world, pos)) {
            renderFluidInVoid(world, pos, vertexConsumer, fluidState);
        }
    }

    private static boolean isDirectlyAboveVoid(BlockView world, BlockPos blockPos) {
        return blockPos.getY() == world.getBottomY();
    }

    @Inject(method = "isSideCovered(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;FLnet/minecraft/block/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    private static void isSideCovered(BlockView world, BlockPos pos, Direction direction, float maxDeviation, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (direction == Direction.DOWN && isDirectlyAboveVoid(world, pos)) {
            cir.setReturnValue(true);
        }
    }

    private void renderFluidInVoid(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, FluidState fluidState) {
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

            boolean bl2 = false;
            boolean bl3 = false;
            boolean sameFluidNorth = isSameFluid(fluidState, northFluidState);
            boolean sameFluidSouth = isSameFluid(fluidState, southFluidState);
            boolean sameFluidWest = isSameFluid(fluidState, westFluidState);
            boolean sameFluidEast = isSameFluid(fluidState, eastFluidState);
            if (!(!bl2 && !bl3 && !sameFluidEast && !sameFluidWest && !sameFluidNorth && !sameFluidSouth)) {
                float brightnessUp = world.getBrightness(Direction.UP, true);
                float brightnessNorth = world.getBrightness(Direction.NORTH, true);
                float brightnessWest = world.getBrightness(Direction.WEST, true);
                float n = 1.0F;
                float o = 1.0F;
                float p = 1.0F;
                float q = 1.0F;
                double d = (pos.getX() & 15);
                double e = (pos.getY() & 15);
                double r = (pos.getZ() & 15);
                float t = 0.0F;
                float ca = 0;
                float cb;
                float u1;
                float u2;

                int light = this.getLight(world, pos);

                FluidVariant fluidVariant = FluidVariant.of(fluid);
                Sprite sprites = FluidVariantRendering.getSprites(fluidVariant)[1];
                int color = FluidVariantRendering.getColor(fluidVariant, world, pos);
                int[] colors = unpackColor(color);

                float redF = colors[1] / 255F;
                float greenF = colors[2] / 255F;
                float blueF = colors[3] / 255F;

                float alpha1 = colors[0] / 255F;
                float alpha2 = 0.4F * (colors[0] / 255F);
                float alpha3 = 0.0F;

                for (int i = 0; i < 4; ++i) { // directions
                    double x1;
                    double z1;
                    double x2;
                    double z2;
                    boolean shouldRender;
                    if (i == 0) {
                        ca = n;
                        cb = q;
                        x1 = d;
                        x2 = d + 1.0D;
                        z1 = r + 0.0010000000474974513D;
                        z2 = r + 0.0010000000474974513D;
                        shouldRender = sameFluidNorth;
                    } else if (i == 1) {
                        cb = o;
                        x1 = d + 1.0D;
                        x2 = d;
                        z1 = r + 1.0D - 0.0010000000474974513D;
                        z2 = r + 1.0D - 0.0010000000474974513D;
                        shouldRender = sameFluidSouth;
                    } else if (i == 2) {
                        ca = o;
                        cb = n;
                        x1 = d + 0.0010000000474974513D;
                        x2 = d + 0.0010000000474974513D;
                        z1 = r + 1.0D;
                        z2 = r;
                        shouldRender = sameFluidWest;
                    } else {
                        ca = q;
                        cb = p;
                        x1 = d + 1.0D - 0.0010000000474974513D;
                        x2 = d + 1.0D - 0.0010000000474974513D;
                        z1 = r;
                        z2 = r + 1.0D;
                        shouldRender = sameFluidEast;
                    }

                    if (!shouldRender) {
                        u1 = sprites.getFrameU(0.0D);
                        u2 = sprites.getFrameU(8.0D);
                        float v1 = sprites.getFrameV(((1.0F - ca) * 16.0F * 0.5F));
                        float v2 = sprites.getFrameV(((1.0F - cb) * 16.0F * 0.5F));
                        float v3 = sprites.getFrameV(8.0D);
                        float sidedBrightness = i < 2 ? brightnessNorth : brightnessWest;
                        float red = brightnessUp * sidedBrightness * redF;
                        float green = brightnessUp * sidedBrightness * greenF;
                        float blue = brightnessUp * sidedBrightness * blueF;
                        vertex(vertexConsumer, x1, e + (double) ca - 1, z1, red, green, blue, u1, v1, light, alpha1);
                        vertex(vertexConsumer, x2, e + (double) cb - 1, z2, red, green, blue, u2, v2, light, alpha1);
                        vertex(vertexConsumer, x2, e + (double) t - 1, z2, red, green, blue, u2, v3, light, alpha2);
                        vertex(vertexConsumer, x1, e + (double) t - 1, z1, red, green, blue, u1, v3, light, alpha2);

                        vertex(vertexConsumer, x1, e + (double) ca - 2, z1, red, green, blue, u1, v1, light, alpha2);
                        vertex(vertexConsumer, x2, e + (double) cb - 2, z2, red, green, blue, u2, v2, light, alpha2);
                        vertex(vertexConsumer, x2, e + (double) t - 2, z2, red, green, blue, u2, v3, light, alpha3);
                        vertex(vertexConsumer, x1, e + (double) t - 2, z1, red, green, blue, u1, v3, light, alpha3);
                        if (sprites != this.waterOverlaySprite) {
                            vertex(vertexConsumer, x1, e + (double) t - 1, z1, red, green, blue, u1, v3, light, alpha2);
                            vertex(vertexConsumer, x2, e + (double) t - 1, z2, red, green, blue, u2, v3, light, alpha2);
                            vertex(vertexConsumer, x2, e + (double) cb - 1, z2, red, green, blue, u2, v2, light, alpha1);
                            vertex(vertexConsumer, x1, e + (double) ca - 1, z1, red, green, blue, u1, v1, light, alpha1);

                            vertex(vertexConsumer, x1, e + (double) t - 2, z1, red, green, blue, u1, v3, light, alpha3);
                            vertex(vertexConsumer, x2, e + (double) t - 2, z2, red, green, blue, u2, v3, light, alpha3);
                            vertex(vertexConsumer, x2, e + (double) cb - 2, z2, red, green, blue, u2, v2, light, alpha2);
                            vertex(vertexConsumer, x1, e + (double) ca - 2, z1, red, green, blue, u1, v1, light, alpha2);
                        }
                    }
                }
            }
        }
    }

    private void vertex(VertexConsumer vertexConsumer, double x, double y, double z, float red, float green, float blue, float u, float v, int light, float alpha) {
        vertexConsumer.vertex(x, y, z).color(red, green, blue, alpha).texture(u, v).light(light).normal(0.0F, 1.0F, 0.0F).next();
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
