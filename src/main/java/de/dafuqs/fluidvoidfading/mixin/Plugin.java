package de.dafuqs.fluidvoidfading.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class Plugin implements IMixinConfigPlugin {
	
	private static final FabricLoader LOADER = FabricLoader.getInstance();

    @Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return LOADER.isModLoaded("sodium") || !mixinClassName.contains("Sodium");
	}

    @Override
	public List<String> getMixins() {
		return List.of();
	}

}