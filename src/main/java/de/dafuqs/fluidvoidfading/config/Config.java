package de.dafuqs.fluidvoidfading.config;

import me.shedaniel.autoconfig.*;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.*;

import java.util.*;

@me.shedaniel.autoconfig.annotation.Config(name = "FluidVoidFading")
public class Config implements ConfigData {
	
	@Comment("If you notice a fluid not rendering transparent try adding it's identifier here")
	public List<String> AdditionalTransparentFluids = new ArrayList<>();
	
	@Override
	public void validatePostLoad() {
		if(AdditionalTransparentFluids.isEmpty()) {
			AdditionalTransparentFluids.add("minecraft:flowing_lava");
		}
	}
	
}
