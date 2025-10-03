package de.dafuqs.fluidvoidfading.neoforge;

import de.dafuqs.fluidvoidfading.FluidVoidFading;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

@Mod(value = FluidVoidFading.MOD_ID, dist = Dist.CLIENT)
public class FluidVoidFadingImpl {
    private static final ModConfigSpec SPEC;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> TRANSPARENT_FLUIDS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        TRANSPARENT_FLUIDS = builder.comment(
                                        "If you notice a fluid not rendering transparent, try adding its identifier here")
                                    .defineListAllowEmpty("additionalTransparentFluids",
                                                          List.of("minecraft:flowing_lava"), () -> "", o -> {
                                            if (o instanceof String)
                                                try {
                                                    Identifier.of((String)o);
                                                    return true;
                                                } catch (InvalidIdentifierException ignored) {
                                                }
                                            return false;
                                        });
        SPEC = builder.build();
    }

    public FluidVoidFadingImpl(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC);
        bus.addListener(FluidVoidFadingImpl::clientStartup);
    }

    public static void clientStartup(FMLClientSetupEvent event) {
        for (String s : TRANSPARENT_FLUIDS.get()) {
            Identifier rl = Identifier.of(s);
            Fluid fluid = Registries.FLUID.get(rl);
            if (fluid == Fluids.EMPTY)
                FluidVoidFading.LOGGER.error("Fluid '{}' not found!", s);
            RenderLayers.setRenderLayer(fluid, BlockRenderLayer.TRANSLUCENT);
        }
        TRANSPARENT_FLUIDS.clearCache();
    }

    public static int getColor(Fluid f, BlockRenderView view, BlockPos pos, int fallback) {
        return fallback;
    }
}
