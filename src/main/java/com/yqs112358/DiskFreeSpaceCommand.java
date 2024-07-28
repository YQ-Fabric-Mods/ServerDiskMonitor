package com.yqs112358;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.yqs112358.DiskMonitor.getCurrentDiskFreeSpace;
import static net.minecraft.server.command.CommandManager.*;

public final class DiskFreeSpaceCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(literal("diskfreespace")
                .executes(ctx -> getDiskFreeSpace(ctx.getSource())));
    }

    public static int getDiskFreeSpace(ServerCommandSource source) throws CommandSyntaxException {
        long currentFreeSpace = getCurrentDiskFreeSpace();
        String capacityString = Utils.capacityToReadable(currentFreeSpace);
        source.sendFeedback(() -> Text.translatable("commands.diskFreeSpace.result", capacityString), true);
        return Command.SINGLE_SUCCESS; // 成功
    }
}
