package de.dafuqs.liquidvoidrenderer.mixin.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.client.render.block.FluidRenderer.method_29708;

@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {

    @Shadow @Final private Sprite[] lavaSprites;

    @Shadow @Final private Sprite[] waterSprites;

    @Shadow
    private static boolean isSameFluid(BlockView world, BlockPos pos, Direction side, FluidState state) {
        return false;
    }

    @Shadow
    private static boolean isSideCovered(BlockView world, BlockPos pos, Direction direction, float maxDeviation) {
        return false;
    }

    @Shadow protected abstract float getNorthWestCornerFluidHeight(BlockView world, BlockPos pos, Fluid fluid);

    @Shadow protected abstract void vertex(VertexConsumer vertexConsumer, double x, double y, double z, float red, float green, float blue, float u, float v, int light);

    @Shadow protected abstract int getLight(BlockRenderView world, BlockPos pos);

    @Shadow private Sprite waterOverlaySprite;

    @Inject(method = "render", at = @At("HEAD"))
    public void render(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, FluidState state, CallbackInfoReturnable<Boolean> cir) {
        if (isDirectlyAboveVoid(world, pos)) {
            renderFluidInVoid(world, pos, vertexConsumer, state);
        }
    }

    private boolean isDirectlyAboveVoid(BlockRenderView world, BlockPos blockPos) {
        return blockPos.getY() == world.getBottomY();
    }

    private void renderFluidInVoid(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, FluidState state) {
        pos = pos.down();
        boolean isLavaFluid = state.isIn(FluidTags.LAVA);
        Sprite[] sprites = isLavaFluid ? this.lavaSprites : this.waterSprites;
        BlockState blockState = world.getBlockState(pos);
        int i = isLavaFluid ? 16777215 : BiomeColors.getWaterColor(world, pos);
        float f = (float)(i >> 16 & 255) / 255.0F;
        float g = (float)(i >> 8 & 255) / 255.0F;
        float h = (float)(i & 255) / 255.0F;
        boolean bl2 = !isSameFluid(world, pos, Direction.UP, state);
        boolean bl3 = method_29708(world, pos, state, blockState, Direction.DOWN) && !isSideCovered(world, pos, Direction.DOWN, 0.8888889F);
        boolean bl4 = method_29708(world, pos, state, blockState, Direction.NORTH);
        boolean bl5 = method_29708(world, pos, state, blockState, Direction.SOUTH);
        boolean bl6 = method_29708(world, pos, state, blockState, Direction.WEST);
        boolean bl7 = method_29708(world, pos, state, blockState, Direction.EAST);
        if (!bl2 && !bl3 && !bl7 && !bl6 && !bl4 && !bl5) {

        } else {
            boolean bl8 = false;
            float j = world.getBrightness(Direction.DOWN, true);
            float k = world.getBrightness(Direction.UP, true);
            float l = world.getBrightness(Direction.NORTH, true);
            float m = world.getBrightness(Direction.WEST, true);
            float n = this.getNorthWestCornerFluidHeight(world, pos, state.getFluid());
            float o = this.getNorthWestCornerFluidHeight(world, pos.south(), state.getFluid());
            float p = this.getNorthWestCornerFluidHeight(world, pos.east().south(), state.getFluid());
            float q = this.getNorthWestCornerFluidHeight(world, pos.east(), state.getFluid());
            double d = (pos.getX() & 15);
            double e = (pos.getY() & 15);
            double r = (pos.getZ() & 15);
            float t = bl3 ? 0.001F : 0.0F;
            float ag;
            float ai;
            float ca;
            float cb;
            float aj;
            float al;
            float an;
            float cg;
            float ch;

            if (bl3) {
                ag = sprites[0].getMinU();
                ai = sprites[0].getMaxU();
                ca = sprites[0].getMinV();
                cb = sprites[0].getMaxV();
                int bb = this.getLight(world, pos.down());
                aj = j * f;
                al = j * g;
                an = j * h;
                this.vertex(vertexConsumer, d, e + (double)t, r + 1.0D, aj, al, an, ag, cb, bb);
                this.vertex(vertexConsumer, d, e + (double)t, r, aj, al, an, ag, ca, bb);
                this.vertex(vertexConsumer, d + 1.0D, e + (double)t, r, aj, al, an, ai, ca, bb);
                this.vertex(vertexConsumer, d + 1.0D, e + (double)t, r + 1.0D, aj, al, an, ai, cb, bb);
            }

            int bf = this.getLight(world, pos);

            for(int bg = 0; bg < 4; ++bg) {
                double cc;
                double ce;
                double cd;
                double cf;
                Direction direction3;
                boolean bl12;
                if (bg == 0) {
                    ca = n;
                    cb = q;
                    cc = d;
                    cd = d + 1.0D;
                    ce = r + 0.0010000000474974513D;
                    cf = r + 0.0010000000474974513D;
                    direction3 = Direction.NORTH;
                    bl12 = bl4;
                } else if (bg == 1) {
                    ca = p;
                    cb = o;
                    cc = d + 1.0D;
                    cd = d;
                    ce = r + 1.0D - 0.0010000000474974513D;
                    cf = r + 1.0D - 0.0010000000474974513D;
                    direction3 = Direction.SOUTH;
                    bl12 = bl5;
                } else if (bg == 2) {
                    ca = o;
                    cb = n;
                    cc = d + 0.0010000000474974513D;
                    cd = d + 0.0010000000474974513D;
                    ce = r + 1.0D;
                    cf = r;
                    direction3 = Direction.WEST;
                    bl12 = bl6;
                } else {
                    ca = q;
                    cb = p;
                    cc = d + 1.0D - 0.0010000000474974513D;
                    cd = d + 1.0D - 0.0010000000474974513D;
                    ce = r;
                    cf = r + 1.0D;
                    direction3 = Direction.EAST;
                    bl12 = bl7;
                }

                if (bl12 && !isSideCovered(world, pos, direction3, Math.max(ca, cb))) {
                    bl8 = true;
                    BlockPos blockPos = pos.offset(direction3);
                    Sprite sprite3 = sprites[1];
                    if (!isLavaFluid) {
                        Block block = world.getBlockState(blockPos).getBlock();
                        if (block instanceof TransparentBlock || block instanceof LeavesBlock) {
                            sprite3 = this.waterOverlaySprite;
                        }
                    }

                    cg = sprite3.getFrameU(0.0D);
                    ch = sprite3.getFrameU(8.0D);
                    float ci = sprite3.getFrameV((double)((1.0F - ca) * 16.0F * 0.5F));
                    float cj = sprite3.getFrameV((double)((1.0F - cb) * 16.0F * 0.5F));
                    float ck = sprite3.getFrameV(8.0D);
                    float cl = bg < 2 ? l : m;
                    float cm = k * cl * f;
                    float cn = k * cl * g;
                    float co = k * cl * h;
                    this.vertex(vertexConsumer, cc, e + (double)ca, ce, cm, cn, co, cg, ci, bf);
                    this.vertex(vertexConsumer, cd, e + (double)cb, cf, cm, cn, co, ch, cj, bf);
                    this.vertex(vertexConsumer, cd, e + (double)t, cf, cm, cn, co, ch, ck, bf);
                    this.vertex(vertexConsumer, cc, e + (double)t, ce, cm, cn, co, cg, ck, bf);
                    if (sprite3 != this.waterOverlaySprite) {
                        this.vertex(vertexConsumer, cc, e + (double)t, ce, cm, cn, co, cg, ck, bf);
                        this.vertex(vertexConsumer, cd, e + (double)t, cf, cm, cn, co, ch, ck, bf);
                        this.vertex(vertexConsumer, cd, e + (double)cb, cf, cm, cn, co, ch, cj, bf);
                        this.vertex(vertexConsumer, cc, e + (double)ca, ce, cm, cn, co, cg, ci, bf);
                    }
                }
            }
        }
    }

}
