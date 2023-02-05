package de.dafuqs.fluidvoidfading;

import de.dafuqs.fluidvoidfading.config.*;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.serializer.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.*;
import net.minecraft.client.render.*;
import net.minecraft.fluid.*;
import net.minecraft.registry.*;
import net.minecraft.util.*;
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
                Fluid fluid = Registries.FLUID.get(identifier);
                BlockRenderLayerMap.INSTANCE.putFluid(fluid, RenderLayer.getTranslucent());
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, "Could not find fluid '" + additionalTransparentFluidString + "' and make it transparent.");
            }
        }

    }

}
