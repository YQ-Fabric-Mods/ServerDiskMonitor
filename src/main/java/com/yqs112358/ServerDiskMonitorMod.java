package com.yqs112358;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerDiskMonitorMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("ServerDiskMonitor");
	public static final DiskMonitor monitor = new DiskMonitor();

	@Override
	public void onInitialize() {
		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

		ServerTickEvents.START_SERVER_TICK.register((server)->{
			monitor.tick(server);
		});

		monitor.loadConfigs();
		monitor.monitorLoop();

		LOGGER.info("ServerDiskMonitor loaded.");
	}
}