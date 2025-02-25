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
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DiskMonitor {
    private final static Logger LOGGER = ServerDiskMonitorMod.LOGGER;
    private static File rootDiskFile = null;
    private static MinecraftServer minecraftServer = null;
    private final TaskScheduler scheduler = new TaskScheduler();
    private final String customDelayCommand = "delay ";
    // configs
    private int intervalInTicks;
    private long thresholdInBytes;
    private ArrayList<String> alertCommands;

    public DiskMonitor() {
        Path rootDir = FabricLoader.getInstance().getConfigDir().toAbsolutePath().getRoot();
        rootDiskFile = rootDir.toFile();
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
        return rootDiskFile.getUsableSpace();
    }

    public void logAlert(long currentFreeSpace) {
        String capacityString = Utils.capacityToReadable(currentFreeSpace);
        String alertString = Text.translatable("logs.diskMonitor.consoleAlert", capacityString).getString();
        LOGGER.warn(alertString);
        try {
            File logsDir = FabricLoader.getInstance().getGameDir().resolve("logs").toFile();
            if(!logsDir.exists())
                logsDir.mkdirs();
            File alertFile = logsDir.toPath().resolve("ServerDiskMonitor.log").toFile();
            FileWriter out = new FileWriter(alertFile, true);
            String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
            out.write("[" + datetime + "] " + alertString + "\n");
            out.close();
        } catch (IOException exception) {
            LOGGER.error(exception.toString());
        }
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
