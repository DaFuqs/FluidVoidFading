package de.dafuqs.fluidvoidfading.mixin.client;

import me.jellysquid.mods.sodium.client.model.color.*;
import me.jellysquid.mods.sodium.client.model.light.*;
import me.jellysquid.mods.sodium.client.model.light.data.*;
import me.jellysquid.mods.sodium.client.model.quad.*;
import me.jellysquid.mods.sodium.client.model.quad.properties.*;
import me.jellysquid.mods.sodium.client.render.chunk.compile.*;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.*;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.*;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.*;
import me.jellysquid.mods.sodium.client.util.*;
import me.jellysquid.mods.sodium.client.world.*;
import net.caffeinemc.mods.sodium.api.util.*;
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
@Mixin(value = FluidRenderer.class, remap = false)
public abstract class SodiumFluidRendererMixin {
    
    @Shadow @Final private BlockPos.Mutable scratchPos;
    
    @Shadow protected abstract ColorProvider<FluidState> getColorProvider(Fluid fluid, FluidRenderHandler handler);
    
    @Shadow @Final private ModelQuadViewMutable quad;
    
    @Shadow @Final private LightPipelineProvider lighters;
    
    @Shadow @Final private QuadLightData quadLightData;
    
    @Shadow @Final private int[] quadColors;
    
    @Shadow
    private static void setVertex(ModelQuadViewMutable quad, int i, float x, float y, float z, float u, float v) {
    }
    
    @Shadow protected abstract void writeQuad(ChunkModelBuilder builder, Material material, BlockPos offset, ModelQuadView quad, ModelQuadFacing facing, boolean flip);
    
    @Inject(method = "render", at = @At("HEAD"))
    public void render(WorldSlice world, FluidState fluidState, BlockPos pos, BlockPos offset, ChunkBuildBuffers buffers, CallbackInfo cir) {
        if (pos.getY() == world.getBottomY()) {
            renderFluidInVoid(world, fluidState, pos, offset, buffers);
        }
    }

    @Inject(method = "isSideExposed", at = @At("HEAD"), cancellable = true)
    private void isSideExposed(BlockRenderView world, int x, int y, int z, Direction dir, float height, CallbackInfoReturnable<Boolean> cir) {
        if (dir == Direction.DOWN && y == world.getBottomY()) {
            cir.setReturnValue(false);
        }
    }
    
    @Unique
    private void renderFluidInVoid(WorldSlice world, @NotNull FluidState fluidState, BlockPos blockPos, BlockPos offset, ChunkBuildBuffers buffers) {
        Fluid fluid = fluidState.getFluid();
        Material material = DefaultMaterials.forFluidState(fluidState);
        ChunkModelBuilder meshBuilder = buffers.get(material);
        if (fluid != Fluids.EMPTY) {
            int posX = blockPos.getX();
            int posY = blockPos.getY();
            int posZ = blockPos.getZ();
            
            BlockState northBlockState = world.getBlockState(blockPos.offset(Direction.NORTH));
            FluidState northFluidState = northBlockState.getFluidState();
            BlockState southBlockState = world.getBlockState(blockPos.offset(Direction.SOUTH));
            FluidState southFluidState = southBlockState.getFluidState();
            BlockState westBlockState = world.getBlockState(blockPos.offset(Direction.WEST));
            FluidState westFluidState = westBlockState.getFluidState();
            BlockState eastBlockState = world.getBlockState(blockPos.offset(Direction.EAST));
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
            
            ColorProvider<FluidState> colorizer = this.getColorProvider(fluid, handler);
            Sprite[] sprites = handler.getFluidSprites(world, blockPos, fluidState);
            float northWestHeight;
            float southWestHeight;
            float southEastHeight;
            float northEastHeight;
            float yOffset = 0.0F;
            northWestHeight = 1.0F;
            southWestHeight = 1.0F;
            southEastHeight = 1.0F;
            northEastHeight = 1.0F;
    
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
                
                        c1 = northWestHeight;
                        c2 = northEastHeight;
                        x1 = 0.0F;
                        x2 = 1.0F;
                        z1 = 0.001F;
                        z2 = z1;
                        break;
                    case SOUTH:
                        if (sfSouth) {
                            continue;
                        }
                
                        c1 = southEastHeight;
                        c2 = southWestHeight;
                        x1 = 1.0F;
                        x2 = 0.0F;
                        z1 = 0.999F;
                        z2 = z1;
                        break;
                    case WEST:
                        if (sfWest) {
                            continue;
                        }
                
                        c1 = southWestHeight;
                        c2 = northWestHeight;
                        x1 = 0.001F;
                        x2 = x1;
                        z1 = 1.0F;
                        z2 = 0.0F;
                        break;
                    case EAST:
                        if (!sfEast) {
                            c1 = northEastHeight;
                            c2 = southEastHeight;
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
        
                u1 = sprite.getFrameU(0.F);
                float u2 = sprite.getFrameU(0.5F);
                float v1 = sprite.getFrameV((1.0F - c1) * 0.5F);
                float v2 = sprite.getFrameV((1.0F - c2) * 0.5F);
                float v3 = sprite.getFrameV(0.5F);
                quad.setSprite(sprite);
                setVertex(quad, 0, x2, c2, z2, u2, v2);
                setVertex(quad, 1, x2, yOffset, z2, u2, v3);
                setVertex(quad, 2, x1, yOffset, z1, u1, v3);
                setVertex(quad, 3, x1, c1, z1, u1, v1);
                float br = dir.getAxis() == Direction.Axis.Z ? 0.8F : 0.6F;
                ModelQuadFacing facing = ModelQuadFacing.fromDirection(dir);
                
                int[] previousQuadColors = this.quadColors;
                lighter.calculate(quad, blockPos, this.quadLightData, null, dir, false);
                colorizer.getColors(world, blockPos, fluidState, quad, previousQuadColors);
                int[] biomeColors = previousQuadColors;

                this.calculateAlphaQuadColors(biomeColors, br, 1.0F, 0.3F);
                this.writeQuad(meshBuilder, material, offset.offset(Direction.DOWN, 1), quad, facing, false);
                if (!isOverlay) {
                    this.writeQuad(meshBuilder, material, offset.offset(Direction.DOWN, 1), quad, facing.getOpposite(), true);
                }

                this.calculateAlphaQuadColors(biomeColors, br, 0.3F, 0.0F);
                this.writeQuad(meshBuilder, material, offset.offset(Direction.DOWN, 2), quad, facing, false);
                if (!isOverlay) {
                    this.writeQuad(meshBuilder, material, offset.offset(Direction.DOWN, 2), quad, facing.getOpposite(), true);
                }
                
                this.quadColors[0] = previousQuadColors[0];
                this.quadColors[1] = previousQuadColors[1];
                this.quadColors[2] = previousQuadColors[2];
                this.quadColors[3] = previousQuadColors[3];
            }
        }
    }
    
    @Unique
    private void calculateAlphaQuadColors(int[] biomeColors, float brightness, float alpha1, float alpha2) {
        for(int i = 0; i < 4; ++i) {
            this.quadColors[i] = ColorABGR.withAlpha(biomeColors != null ? biomeColors[i] : -1, brightness);
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
