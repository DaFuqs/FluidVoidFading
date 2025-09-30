package de.dafuqs.fluidvoidfading.fabric;

import de.dafuqs.fluidvoidfading.FluidVoidFading;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class FluidVoidFadingImpl implements ClientModInitializer {
    public static final boolean FLUID_API_LOADED = FabricLoader.getInstance().isModLoaded("fabric-rendering-fluids-v1");

    @Override
    public void onInitializeClient() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FluidVoidFading.MOD_ID + "-client.txt");
        List<Identifier> l;
        config:
        {
            if (Files.exists(path))
                try (Stream<String> stream = Files.lines(path)) {
                    l = stream.map(String::trim)
                              .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                              .map(Identifier::tryParse)
                              .filter(Objects::nonNull)
                              .toList();
                    break config;
                } catch (IOException e) {
                    FluidVoidFading.LOGGER.error("Unable to read config! Writing defaults!", e);
                }

            try {
                Files.writeString(path, """
                    # If you notice a fluid not rendering transparent, try adding its identifier here
                    # The identifiers are separated by newlines and lines beginning with a hashtag are ignored
                    
                    #minecraft:lava
                    minecraft:flowing_lava
                    """, StandardOpenOption.CREATE);
                l = List.of(Identifier.ofVanilla("flowing_lava"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for (Identifier id : l) {
            Fluid fluid = Registries.FLUID.get(id);
            if (fluid == Fluids.EMPTY)
                FluidVoidFading.LOGGER.error("Fluid '{}' not found!", id);
            BlockRenderLayerMap.putFluid(fluid, BlockRenderLayer.TRANSLUCENT);
        }
    }

    public static int getColor(Fluid f, BlockRenderView view, BlockPos pos, int fallback) {
        return FLUID_API_LOADED ? FluidVariantRendering.getColor(FluidVariant.of(f), view, pos) : fallback | 0xFF000000;
    }
}
