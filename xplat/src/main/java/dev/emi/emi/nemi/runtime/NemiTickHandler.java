package dev.emi.emi.nemi.runtime;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import dev.emi.emi.nemi.NemiRecipe;

public class NemiTickHandler {

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			NemiRecipe.tickHandlers();
		}
	}
}
