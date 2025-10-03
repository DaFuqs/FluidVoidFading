/* modified on 06/06/2025 and 9/26/2025 by AnAwesomGuy */

package de.dafuqs.fluidvoidfading.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.dafuqs.fluidvoidfading.FluidVoidFading;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.dafuqs.fluidvoidfading.FluidVoidFading.EPSILON;

@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {
    @Shadow
    private static boolean isSameFluid(FluidState firstState, FluidState secondState) {
        throw new AssertionError();
    }

    @Shadow
    protected abstract int getLight(BlockRenderView level, BlockPos pos);

    @SuppressWarnings({"InvokeAssignCanReplacedWithExpression", "LocalMayBeArgsOnly"}) // mcdev might be high
    @Inject(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/fluid/FluidState;getFluid()Lnet/minecraft/fluid/Fluid;"))
    public void render(BlockRenderView level, BlockPos pos, VertexConsumer consumer, BlockState blockState, FluidState fluidState, CallbackInfo ci,
                       @Local(ordinal = 0) int color, @Local Sprite[] sprites, @Local Fluid fluid,
                       @Local(ordinal = 3) FluidState north, @Local(ordinal = 4) FluidState south, @Local(ordinal = 5) FluidState west, @Local(ordinal = 6) FluidState east) {
        if (fluid == Fluids.EMPTY || pos.getY() != level.getBottomY())
            return;

        float xLo = pos.getX() & 0xF;
        float yLo = pos.getY() & 0xF;
        float zLo = pos.getZ() & 0xF;

        int light = getLight(level, pos);
        Sprite sprite = sprites[1]; // flowing sprite

        float u1 = sprite.getFrameU(0F);
        float u2 = sprite.getFrameU(0.5F);
        float v1 = sprite.getFrameV(0F);
        float v2 = sprite.getFrameV(0.5F);

        float brightnessUp = level.getBrightness(Direction.UP, true);
        float brightnessNorth = level.getBrightness(Direction.NORTH, true);
        float brightnessWest = level.getBrightness(Direction.WEST, true);

        color = FluidVoidFading.getColor(fluid, level, pos, color);
        float redF = ColorHelper.getRedFloat(color);
        float blueF = ColorHelper.getBlueFloat(color);
        float greenF = ColorHelper.getGreenFloat(color);
        float alpha1 = ColorHelper.getAlphaFloat(color);
        float alpha2 = 0.3F * alpha1;

        for (Direction dir : Direction.Type.HORIZONTAL) { // directions
            float x1, z1, x2, z2;
            if (dir == Direction.NORTH) {
                x1 = xLo;
                x2 = xLo + 1F;
                z1 = z2 = zLo + EPSILON;
                if (isSameFluid(fluidState, north))
                    continue;
            } else if (dir == Direction.SOUTH) {
                x1 = xLo + 1F;
                x2 = xLo;
                z1 = z2 = zLo + 1F - EPSILON;
                if (isSameFluid(fluidState, south))
                    continue;
            } else if (dir == Direction.WEST) {
                x1 = x2 = xLo + EPSILON;
                z1 = zLo + 1F;
                z2 = zLo;
                if (isSameFluid(fluidState, west))
                    continue;
            } else if (dir == Direction.EAST) {
                x1 = x2 = xLo + 1F - EPSILON;
                z1 = zLo;
                z2 = zLo + 1F;
                if (isSameFluid(fluidState, east))
                    continue;
            } else continue;

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

            // not really sure why this has to be rendered twice, but it looks better so...
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

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/FluidRenderer;shouldSkipRendering(Lnet/minecraft/util/math/Direction;FLnet/minecraft/block/BlockState;)Z", ordinal = 0))
    public boolean cullDownAtVoid(boolean original, BlockRenderView level, BlockPos pos, VertexConsumer buffer, BlockState blockState, FluidState fluidState) {
        return original || pos.getY() == level.getBottomY();
    }

    @Unique
    private void fluidvoidfading$vertex(VertexConsumer vertexConsumer, float x, float y, float z, float red, float green, float blue, float u, float v, int light, float alpha) {
        vertexConsumer.vertex(x, y, z).color(red, green, blue, alpha).texture(u, v).light(light).normal(0F, 1F, 0F);
    }
}