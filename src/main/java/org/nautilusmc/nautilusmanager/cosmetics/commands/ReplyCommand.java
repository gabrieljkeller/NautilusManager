package org.nautilusmc.nautilusmanager.cosmetics.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;

import java.util.*;

public class ReplyCommand extends NautilusCommand {

    private static final Map<UUID, UUID> lastMessager = new HashMap<>();
    private static final Map<UUID, BukkitRunnable> runnables = new HashMap<>();
    public static final int LAST_MESSAGER_TIMEOUT = 60; //seconds

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text("Only players can use this command").color(NautilusCommand.ERROR_COLOR));
            return true;
        }
        if (strings.length == 0) return false;

        Player player = (Player) commandSender;

        if (!lastMessager.containsKey(player.getUniqueId())) {
            player.sendMessage(Component.text("You have no one to reply to").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        OfflinePlayer recipient = Bukkit.getOfflinePlayer(lastMessager.get(player.getUniqueId()));
        if (!recipient.isOnline()) {
            player.sendMessage(Component.text(recipient.getName()+" is no longer online").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        player.performCommand("msg " + recipient.getName() + " " + String.join(" ", strings));
        return true;
    }

    public static void messaged(UUID sender, UUID receiver) {
        lastMessager.put(receiver, sender);
        if (runnables.containsKey(receiver)) {
            runnables.get(receiver).cancel();
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    lastMessager.remove(receiver);
                    runnables.remove(receiver);
                }
            };
            runnable.runTaskLater(NautilusManager.INSTANCE, LAST_MESSAGER_TIMEOUT * 20L);
            runnables.put(receiver, runnable);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}
