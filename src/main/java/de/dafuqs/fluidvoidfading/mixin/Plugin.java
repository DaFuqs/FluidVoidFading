package de.dafuqs.fluidvoidfading.mixin;

import net.neoforged.fml.*;
import net.neoforged.fml.loading.*;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.*;

import java.util.*;

public final class Plugin implements IMixinConfigPlugin {
	
	@Override
	public void onLoad(String mixinPackage) {
	}
	
	@Override
	public String getRefMapperConfig() {
		return null;
	}
	
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		boolean sodiumLoaded = FMLLoader.getCurrent().getLoadingModList().getModFileById("sodium") != null;
		
		if (mixinClassName.contains("SodiumDefaultFluidRendererMixin")) {
			return sodiumLoaded;
		}
		return !sodiumLoaded;
	}
	
	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}
	
	@Override
	public List<String> getMixins() {
		return List.of();
	}
	
	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
	
	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
	
}