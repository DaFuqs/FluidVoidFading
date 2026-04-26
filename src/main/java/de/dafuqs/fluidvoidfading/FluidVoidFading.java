package de.dafuqs.fluidvoidfading;

import net.minecraft.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.level.material.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.*;
import net.neoforged.fml.common.*;
import net.neoforged.fml.config.*;
import net.neoforged.fml.event.lifecycle.*;
import net.neoforged.neoforge.common.*;
import org.slf4j.*;

import java.util.*;

@Mod(value = FluidVoidFading.MODID, dist = Dist.CLIENT)
public class FluidVoidFading {
    public static final String MODID = "fluidvoidfading";
    private static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    
    private static final ModConfigSpec SPEC;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> TRANSPARENT_FLUIDS;
    
    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        TRANSPARENT_FLUIDS = builder.comment("If you notice a fluid not rendering transparent, try adding its identifier here")
                .defineListAllowEmpty("additionalTransparentFluids", List.of("minecraft:flowing_lava"), () -> "", o -> {
                    if (o instanceof String)
                        try {
                            Identifier.parse((String) o);
                            return true;
                        } catch (IdentifierException ignored) {
                        }
                    return false;
                });
        SPEC = builder.build();
    }
    
    public FluidVoidFading(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC);
        bus.addListener(FluidVoidFading::clientStartup);
    }
    
    public static void clientStartup(FMLClientSetupEvent event) {
        for (String s : TRANSPARENT_FLUIDS.get()) {
            Identifier rl = Identifier.parse(s);
            Fluid fluid = BuiltInRegistries.FLUID.getValue(rl);
            if (fluid == Fluids.EMPTY) {
                LOGGER.error("Fluid '{}' not found!", s);
            } else {
                //ItemBlockRenderTypes.setRenderLayer(fluid, ChunkSectionLayer.TRANSLUCENT);
            }
        }
    }
}