package de.dafuqs.fluidvoidfading;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FluidVoidFading {
    public static final String MOD_ID = "fluidvoidfading";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final float EPSILON = 0.001F;

    @ExpectPlatform
    public static int getColor(Fluid f, BlockRenderView view, BlockPos pos, int fallback) {
        throw new AssertionError();
    }
}
