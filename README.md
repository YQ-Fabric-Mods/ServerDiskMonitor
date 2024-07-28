# ServerDiskMonitor

> A Fabric mod to monitor and alert server disk usage

## Introduction

- Alert when free disk space reduced to a certain level, to avoid potential data loss when the disk is full.
- Add a command `diskfreespace` to check disk free space value in game and console.

## Config File

At `config/server_disk_monitor.json`. For example:

```
{
  "spaceAlertThreshold": "100MB",	
  "monitorInterval": 5,			// seconds
  "alertCommands": [
    "say Not enough disk space! To protect your data, the server will be shut down after 30 seconds",
    "delay 20",
    "say Not enough disk space! To protect your data, the server will be shut down after 10 seconds",
    "delay 5",
    "say Not enough disk space! To protect your data, the server will be shut down after 5 seconds",
    "delay 5",
    "stop"
  ]
}
```

Add a custom `delay` command to delay command execution for seconds (only can be used in this config file).
