package de.dafuqs.liquidvoidrenderer;

import de.dafuqs.liquidvoidrenderer.config.*;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.serializer.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.*;
import net.minecraft.client.render.*;
import net.minecraft.fluid.*;
import net.minecraft.util.*;
import net.minecraft.util.registry.*;

@Environment(EnvType.CLIENT)
public class LiquidVoidRendererClient implements ClientModInitializer {

    public static Config CONFIG;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(Config.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(Config.class).getConfig();

        for(String additionalTransparentFluidString : CONFIG.AdditionalTransparentFluids) {
            Identifier identifier = Identifier.tryParse(additionalTransparentFluidString);
            Fluid fluid = Registry.FLUID.get(identifier);
            BlockRenderLayerMap.INSTANCE.putFluid(fluid, RenderLayer.getTranslucent());
        }

    }

}
