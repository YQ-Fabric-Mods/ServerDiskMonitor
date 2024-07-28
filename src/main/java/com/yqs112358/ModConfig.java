package com.yqs112358;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

import java.util.ArrayList;

@Config(name = "server_disk_monitor")
class ModConfig implements ConfigData {
    String spaceAlertThreshold = "500M";
    int monitorInterval = 30;   // seconds
    ArrayList<String> alertCommands = new ArrayList<String>();
}