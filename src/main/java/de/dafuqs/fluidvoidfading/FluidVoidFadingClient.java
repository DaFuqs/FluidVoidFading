package de.dafuqs.fluidvoidfading;

import de.dafuqs.fluidvoidfading.config.*;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.serializer.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.*;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.*;

@Environment(EnvType.CLIENT)
public class FluidVoidFadingClient implements ClientModInitializer {

    private static final Logger LOGGER = LogManager.getLogger();
    public static Config CONFIG;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(Config.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(Config.class).getConfig();

        for(String additionalTransparentFluidString : CONFIG.AdditionalTransparentFluids) {
            try {
                Identifier identifier = Identifier.tryParse(additionalTransparentFluidString);
                Fluid fluid = BuiltInRegistries.FLUID.getValue(identifier);
                BlockRenderLayerMap.putFluid(fluid, ChunkSectionLayer.TRANSLUCENT);
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, "Could not find fluid '" + additionalTransparentFluidString + "' and make it transparent.");
            }
        }

    }

}
