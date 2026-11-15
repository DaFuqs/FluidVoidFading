package de.dafuqs.fluidvoidfading.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.*;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.fabric.render.FluidRendererImpl;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(FluidRendererImpl.class)
public abstract class SodiumFluidRendererImplMixin {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/DefaultMaterials;forChunkLayer(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayer;)Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/Material;"))
    private Material useTranslucentMaterialIfMin(ChunkSectionLayer layer, Operation<Material> original, @Local(argsOnly = true, name = "blockPos") BlockPos pos, @Local(argsOnly = true, name = "level") LevelSlice level) {
        return pos.getY() != level.getMinY() ? original.call(layer) : DefaultMaterials.TRANSLUCENT;
    }
}
