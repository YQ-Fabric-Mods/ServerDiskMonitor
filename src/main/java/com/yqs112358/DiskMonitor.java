package com.yqs112358;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DiskMonitor {
    private final Logger LOGGER;
    private final TaskScheduler scheduler = new TaskScheduler();
    private final String customDelayCommand = "/delay ";
    public MinecraftServer minecraftServer = null;
    // configs
    private int intervalInTicks;
    private long thresholdInBytes;
    private ArrayList<String> alertCommands;

    public DiskMonitor(Logger logger) {
        LOGGER = logger;
    }

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
        LOGGER.info("Configuration loaded.");
    }

    public static long getCurrentDiskFreeSpace() {
        File rootFile = FabricLoader.getInstance().getConfigDir().getRoot().toFile();
        return rootFile.getTotalSpace() - rootFile.getUsableSpace();
    }

    public void logAlert(long currentFreeSpace) {
        String capacityString = Utils.capacityToReadable(currentFreeSpace);
        String alertString = Text.translatable("logs.diskMonitor.consoleAlert", capacityString).toString();
        LOGGER.warn(alertString);
        try {
            File alertFile = FabricLoader.getInstance().getGameDir().resolve("logs").toFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(alertFile, true));
            String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
            out.write("[" + datetime + "] " + alertString);
        } catch (IOException ignored) {}
    }

    public void monitorLoop() {
        boolean serverStopping = false;
        long currentFreeSpace = getCurrentDiskFreeSpace();
        if(currentFreeSpace < thresholdInBytes) {
            // alert: not enough free space!
            logAlert(currentFreeSpace);
            // schedule alert commands
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
