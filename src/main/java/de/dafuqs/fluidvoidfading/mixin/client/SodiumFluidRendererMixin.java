package de.dafuqs.fluidvoidfading.mixin.client;

import me.jellysquid.mods.sodium.client.model.light.*;
import me.jellysquid.mods.sodium.client.model.light.data.*;
import me.jellysquid.mods.sodium.client.model.quad.*;
import me.jellysquid.mods.sodium.client.model.quad.blender.*;
import me.jellysquid.mods.sodium.client.model.quad.properties.*;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.*;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.*;
import me.jellysquid.mods.sodium.client.render.vertex.type.*;
import me.jellysquid.mods.sodium.client.util.color.*;
import me.jellysquid.mods.sodium.common.util.*;
import net.fabricmc.fabric.api.client.render.fluid.v1.*;
import net.minecraft.block.*;
import net.minecraft.client.*;
import net.minecraft.client.texture.*;
import net.minecraft.fluid.*;
import net.minecraft.registry.tag.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Pseudo
@Mixin(FluidRenderer.class)
public abstract class SodiumFluidRendererMixin {
    
    @Shadow @Final private BlockPos.Mutable scratchPos;
    
    @Shadow protected abstract ColorSampler<FluidState> createColorProviderAdapter(FluidRenderHandler handler);
    
    @Shadow @Final private ModelQuadViewMutable quad;
    
    @Shadow @Final private LightPipelineProvider lighters;
    
    @Shadow @Final private QuadLightData quadLightData;
    
    @Shadow @Final private int[] quadColors;
    
    @Shadow @Final private ColorBlender colorBlender;
    
    @Shadow
    private static void setVertex(ModelQuadViewMutable quad, int i, float x, float y, float z, float u, float v) {
    }
    
    @Shadow protected abstract void updateQuad(ModelQuadView quad, BlockRenderView world, BlockPos pos, LightPipeline lighter, Direction dir, float brightness, ColorSampler<FluidState> colorSampler, FluidState fluidState);
    
    @Shadow protected abstract void writeQuad(ChunkModelBuilder builder, BlockPos offset, ModelQuadView quad, ModelQuadFacing facing, ModelQuadWinding winding);
    
    @Shadow @Final private ChunkVertexEncoder.Vertex[] vertices;
    
    @Inject(method = "render", at = @At("HEAD"))
    public void render(BlockRenderView world, FluidState fluidState, BlockPos pos, BlockPos offset, ChunkModelBuilder buffers, CallbackInfoReturnable<Boolean> cir) {
        if (pos.getY() == world.getBottomY()) {
            renderFluidInVoid(world, fluidState, pos, offset, buffers);
        }
    }

    @Inject(method = "isSideExposed(Lnet/minecraft/world/BlockRenderView;IIILnet/minecraft/util/math/Direction;F)Z", at = @At("HEAD"), cancellable = true)
    private void isSideExposed(BlockRenderView world, int x, int y, int z, Direction dir, float height, CallbackInfoReturnable<Boolean> cir) {
        if (dir == Direction.DOWN && y == world.getBottomY()) {
            cir.setReturnValue(false);
        }
    }
    
    private void renderFluidInVoid(BlockRenderView world, @NotNull FluidState fluidState, BlockPos pos, BlockPos offset, ChunkModelBuilder buffers) {
        Fluid fluid = fluidState.getFluid();
        if (fluid != Fluids.EMPTY) {
            int posX = pos.getX();
            int posY = pos.getY();
            int posZ = pos.getZ();
            
            BlockState northBlockState = world.getBlockState(pos.offset(Direction.NORTH));
            FluidState northFluidState = northBlockState.getFluidState();
            BlockState southBlockState = world.getBlockState(pos.offset(Direction.SOUTH));
            FluidState southFluidState = southBlockState.getFluidState();
            BlockState westBlockState = world.getBlockState(pos.offset(Direction.WEST));
            FluidState westFluidState = westBlockState.getFluidState();
            BlockState eastBlockState = world.getBlockState(pos.offset(Direction.EAST));
            FluidState eastFluidState = eastBlockState.getFluidState();
            
            boolean sfNorth = northFluidState.getFluid().matchesType(fluidState.getFluid());
            boolean sfSouth = southFluidState.getFluid().matchesType(fluidState.getFluid());
            boolean sfWest = westFluidState.getFluid().matchesType(fluidState.getFluid());
            boolean sfEast = eastFluidState.getFluid().matchesType(fluidState.getFluid());
    
            boolean isWater = fluidState.isIn(FluidTags.WATER);
            FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluidState.getFluid());
            if (handler == null) {
                boolean isLava = fluidState.isIn(FluidTags.LAVA);
                handler = FluidRenderHandlerRegistry.INSTANCE.get(isLava ? Fluids.LAVA : Fluids.WATER);
            }
            
            ColorSampler<FluidState> colorizer = this.createColorProviderAdapter(handler);
            Sprite[] sprites = handler.getFluidSprites(world, pos, fluidState);
            float h1;
            float h2;
            float h3;
            float h4;
            float yOffset = 0.0F;
            h1 = 1.0F;
            h2 = 1.0F;
            h3 = 1.0F;
            h4 = 1.0F;
    
            ModelQuadViewMutable quad = this.quad;
            LightMode lightMode = isWater && MinecraftClient.isAmbientOcclusionEnabled() ? LightMode.SMOOTH : LightMode.FLAT;
            LightPipeline lighter = this.lighters.getLighter(lightMode);
            quad.setFlags(0);
            float c1;
            float c2;
            float x1;
            float z1;
            float x2;
            float z2;
            float u1;
            for (Direction dir : DirectionUtil.HORIZONTAL_DIRECTIONS) {
                switch (dir) {
                    case NORTH:
                        if (sfNorth) {
                            continue;
                        }
                
                        c1 = h1;
                        c2 = h4;
                        x1 = 0.0F;
                        x2 = 1.0F;
                        z1 = 0.001F;
                        z2 = z1;
                        break;
                    case SOUTH:
                        if (sfSouth) {
                            continue;
                        }
                
                        c1 = h3;
                        c2 = h2;
                        x1 = 1.0F;
                        x2 = 0.0F;
                        z1 = 0.999F;
                        z2 = z1;
                        break;
                    case WEST:
                        if (sfWest) {
                            continue;
                        }
                
                        c1 = h2;
                        c2 = h1;
                        x1 = 0.001F;
                        x2 = x1;
                        z1 = 1.0F;
                        z2 = 0.0F;
                        break;
                    case EAST:
                        if (!sfEast) {
                            c1 = h4;
                            c2 = h3;
                            x1 = 0.999F;
                            x2 = x1;
                            z1 = 0.0F;
                            z2 = 1.0F;
                            break;
                        }
                    default:
                        continue;
                }
        
                int adjX = posX + dir.getOffsetX();
                int adjY = posY + dir.getOffsetY();
                int adjZ = posZ + dir.getOffsetZ();
                Sprite sprite = sprites[1];
                boolean isOverlay = false;
                if (sprites.length > 2) {
                    BlockPos adjPos = this.scratchPos.set(adjX, adjY, adjZ);
                    BlockState adjBlock = world.getBlockState(adjPos);
                    if (FluidRenderHandlerRegistry.INSTANCE.isBlockTransparent(adjBlock.getBlock())) {
                        sprite = sprites[2];
                        isOverlay = true;
                    }
                }
        
                u1 = sprite.getFrameU(0.0);
                float u2 = sprite.getFrameU(8.0);
                float v1 = sprite.getFrameV(((1.0F - c1) * 16.0F * 0.5F));
                float v2 = sprite.getFrameV(((1.0F - c2) * 16.0F * 0.5F));
                float v3 = sprite.getFrameV(8.0);
                quad.setSprite(sprite);
                setVertex(quad, 0, x2, c2, z2, u2, v2);
                setVertex(quad, 1, x2, yOffset, z2, u2, v3);
                setVertex(quad, 2, x1, yOffset, z1, u1, v3);
                setVertex(quad, 3, x1, c1, z1, u1, v1);
                float br = dir.getAxis() == Direction.Axis.Z ? 0.8F : 0.6F;
                ModelQuadFacing facing = ModelQuadFacing.fromDirection(dir);
                
                int[] previousQuadColors = this.quadColors;
                QuadLightData light = this.quadLightData;
                lighter.calculate(quad, pos, light, null, dir, false);
                int[] biomeColors = this.colorBlender.getColors(world, pos, quad, colorizer, fluidState);

                this.calculateAlphaQuadColors(biomeColors, br, 1.0F, 0.3F);
                this.writeQuad(buffers, offset.offset(Direction.DOWN, 1), quad, facing, ModelQuadWinding.CLOCKWISE);
                if (!isOverlay) {
                    this.writeQuad(buffers, offset.offset(Direction.DOWN, 1), quad, facing.getOpposite(), ModelQuadWinding.COUNTERCLOCKWISE);
                }

                this.calculateAlphaQuadColors(biomeColors, br, 0.3F, 0.0F);
                this.writeQuad(buffers, offset.offset(Direction.DOWN, 2), quad, facing, ModelQuadWinding.CLOCKWISE);
                if (!isOverlay) {
                    this.writeQuad(buffers, offset.offset(Direction.DOWN, 2), quad, facing.getOpposite(), ModelQuadWinding.COUNTERCLOCKWISE);
                }
                
                this.quadColors[0] = previousQuadColors[0];
                this.quadColors[1] = previousQuadColors[1];
                this.quadColors[2] = previousQuadColors[2];
                this.quadColors[3] = previousQuadColors[3];
            }
        }
    }
    
    private void calculateAlphaQuadColors(int[] biomeColors, float brightness, float alpha1, float alpha2) {
        for(int i = 0; i < 4; ++i) {
            this.quadColors[i] = ColorABGR.mul(biomeColors != null ? biomeColors[i] : -1, brightness);
            float a = i == 0 || i == 3 ? alpha1 : alpha2;
            this.quadColors[i] = ColorABGR.pack(
                    ColorABGR.unpackRed(this.quadColors[i]) / 255F,
                    ColorABGR.unpackGreen(this.quadColors[i]) / 255F,
                    ColorABGR.unpackBlue(this.quadColors[i]) / 255F,
                    ColorABGR.unpackAlpha(this.quadColors[i]) / 255F * a
            );
        }
    }

}
