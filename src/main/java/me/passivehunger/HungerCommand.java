package me.passivehunger;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class HungerCommand implements CommandExecutor, TabCompleter {

    private final PassiveHunger plugin;

    public HungerCommand(PassiveHunger plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("passivehunger.admin")) {
            sender.sendMessage(Component.text("You do not have permission to execute this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "reload":
                plugin.loadPluginConfig();
                sender.sendMessage(Component.text("PassiveHunger configuration has been reloaded.", NamedTextColor.GREEN));
                break;

            case "toggle":
                boolean newState = !plugin.isDepletionEnabled();
                plugin.setDepletionEnabled(newState);
                sender.sendMessage(Component.text("PassiveHunger has been ", NamedTextColor.GOLD)
                        .append(Component.text(newState ? "ENABLED" : "DISABLED", newState ? NamedTextColor.GREEN : NamedTextColor.RED, TextDecoration.BOLD))
                        .append(Component.text(".", NamedTextColor.GOLD)));
                break;

            case "status":
                sendStatus(sender);
                break;

            default:
                sendUsage(sender);
                break;
        }

        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text("--- PassiveHunger Commands ---", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/ph reload ", NamedTextColor.YELLOW)
                .append(Component.text("- Reloads configuration", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/ph toggle ", NamedTextColor.YELLOW)
                .append(Component.text("- Enables/disables hunger depletion globally", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/ph status ", NamedTextColor.YELLOW)
                .append(Component.text("- Shows the current configuration status", NamedTextColor.GRAY)));
    }

    private void sendStatus(CommandSender sender) {
        boolean enabled = plugin.isDepletionEnabled();
        double time = plugin.getTotalDepletionTimeSeconds();
        double minutes = time / 60.0;

        sender.sendMessage(Component.text("--- PassiveHunger Status ---", NamedTextColor.GOLD));
        
        sender.sendMessage(Component.text("Status: ", NamedTextColor.YELLOW)
                .append(Component.text(enabled ? "ENABLED" : "DISABLED", enabled ? NamedTextColor.GREEN : NamedTextColor.RED)));
        
        sender.sendMessage(Component.text("Depletion Rate: ", NamedTextColor.YELLOW)
                .append(Component.text(time + " seconds (~" + String.format("%.2f", minutes) + " minutes) to go from 40 to 0 points.", NamedTextColor.WHITE)));
        
        sender.sendMessage(Component.text("Exclusions:", NamedTextColor.YELLOW));
        
        sender.sendMessage(Component.text(" - Creative/Spectator: ", NamedTextColor.GRAY)
                .append(Component.text(plugin.isExcludeCreativeSpectator() ? "Excluded" : "Included", plugin.isExcludeCreativeSpectator() ? NamedTextColor.GREEN : NamedTextColor.RED)));
        
        sender.sendMessage(Component.text(" - Peaceful Difficulty: ", NamedTextColor.GRAY)
                .append(Component.text(plugin.isExcludePeaceful() ? "Excluded" : "Included", plugin.isExcludePeaceful() ? NamedTextColor.GREEN : NamedTextColor.RED)));
        
        sender.sendMessage(Component.text(" - Bypass Permission: ", NamedTextColor.GRAY)
                .append(Component.text(plugin.isExcludeBypassPermission() ? "Excluded" : "Included", plugin.isExcludeBypassPermission() ? NamedTextColor.GREEN : NamedTextColor.RED)));

        sender.sendMessage(Component.text("Thresholds:", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text(" - Min Food Threshold: ", NamedTextColor.GRAY)
                .append(Component.text(plugin.getMinFoodThreshold() + " food points", NamedTextColor.WHITE)));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("passivehunger.admin")) {
            return completions;
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            if ("reload".startsWith(partial)) completions.add("reload");
            if ("toggle".startsWith(partial)) completions.add("toggle");
            if ("status".startsWith(partial)) completions.add("status");
        }

        return completions;
    }
}
