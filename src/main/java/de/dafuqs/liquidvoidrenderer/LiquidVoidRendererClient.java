package de.dafuqs.liquidvoidrenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.*;
import net.minecraft.client.render.*;
import net.minecraft.fluid.*;

@Environment(EnvType.CLIENT)
public class LiquidVoidRendererClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putFluid(Fluids.FLOWING_LAVA, RenderLayer.getTranslucent());
    }

}
