package com.yqs112358;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yqs112358.DiskFreeSpaceCommand.register;

public class ServerDiskMonitorMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("ServerDiskMonitor");
	public static final DiskMonitor monitor = new DiskMonitor();

	@Override
	public void onInitialize() {
		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
		monitor.loadConfigs();

		ServerTickEvents.START_SERVER_TICK.register((server)->{
			monitor.tick(server);
		});

		ServerLifecycleEvents.SERVER_STARTED.register((server -> {
			monitor.monitorLoop();		// start a forever loop
		}));

		LOGGER.info("ServerDiskMonitor loaded.");
	}
}