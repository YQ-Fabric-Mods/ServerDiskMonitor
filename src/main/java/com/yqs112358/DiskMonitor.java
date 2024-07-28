package com.yqs112358;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.io.File;
import java.util.ArrayList;

public class DiskMonitor {
    private final TaskScheduler scheduler = new TaskScheduler();
    private final String customDelayCommand = "/delay ";
    public MinecraftServer minecraftServer = null;
    // configs
    private int intervalInTicks;
    private long thresholdInBytes;
    private ArrayList<String> alertCommands;

    public void tick(MinecraftServer server) {
        if(minecraftServer == null)
            minecraftServer = server;
        scheduler.tick();
    }

    public void loadConfigs() {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        intervalInTicks = config.monitorInterval * 20;
        thresholdInBytes = Utils.parseCapacity(config.spaceAlertThreshold);
        alertCommands = config.alertCommands;
    }

    public long getCurrentFreeSpace() {
        File rootFile = FabricLoader.getInstance().getConfigDir().getRoot().toFile();
        return rootFile.getTotalSpace() - rootFile.getUsableSpace();
    }

    public void monitorLoop() {
        boolean serverStopping = false;
        if(getCurrentFreeSpace() < thresholdInBytes) {
            // alert: not enough free space!
            // execute alert commands
            int currentDelayTicks = 0;
            for(String command : alertCommands){
                if(command.startsWith("/"))
                    command = command.substring(1);
                if(command.equals("stop"))
                    serverStopping = true;

                if(command.startsWith(customDelayCommand))
                {
                    // custom delay command
                    int delaySeconds = Integer.parseInt(command.substring(customDelayCommand.length()));
                    currentDelayTicks += delaySeconds * 20;
                }
                else
                {
                    // common command
                    String finalCommand = command;
                    scheduler.addDelayedTask(currentDelayTicks, () -> {
                        ServerCommandSource source = minecraftServer.getCommandSource();
                        minecraftServer.getCommandManager().executeWithPrefix(source, finalCommand);
                    });
                }
            }
        }
        // schedule next check
        if(!serverStopping)
            scheduler.addDelayedTask(intervalInTicks, this::monitorLoop);
    }
}
