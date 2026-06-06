package me.passivehunger;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HungerTicker extends BukkitRunnable {

    private final PassiveHunger plugin;
    private final Map<UUID, Double> foodDeficit = new HashMap<>();

    public HungerTicker(PassiveHunger plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        double totalDepletionTime = plugin.getTotalDepletionTimeSeconds();
        if (totalDepletionTime <= 0) {
            return;
        }

        // Total capacity is 20 hunger + 20 saturation = 40 points
        double deductionPerSecond = 40.0 / totalDepletionTime;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            // Check eligibility based on config
            if (shouldBypass(player)) {
                continue;
            }

            UUID uuid = player.getUniqueId();
            float saturation = player.getSaturation();
            int foodLevel = player.getFoodLevel();

            // If player is already fully starved, reset deficit and skip
            if (foodLevel <= 0 && saturation <= 0.0f) {
                foodDeficit.put(uuid, 0.0);
                continue;
            }

            if (saturation > 0.0f) {
                float newSaturation = (float) (saturation - deductionPerSecond);
                if (newSaturation < 0.0f) {
                    // Saturation depleted, transfer remainder to food level deficit
                    float leftover = -newSaturation;
                    player.setSaturation(0.0f);
                    
                    double deficit = foodDeficit.getOrDefault(uuid, 0.0) + leftover;
                    if (deficit >= 1.0) {
                        int toSubtract = (int) deficit;
                        deficit -= toSubtract;
                        player.setFoodLevel(Math.max(0, player.getFoodLevel() - toSubtract));
                    }
                    foodDeficit.put(uuid, deficit);
                } else {
                    player.setSaturation(newSaturation);
                    // Reset deficit while player still has active saturation
                    foodDeficit.put(uuid, 0.0);
                }
            } else {
                // Deduct directly from food level via deficit accumulator
                double deficit = foodDeficit.getOrDefault(uuid, 0.0) + deductionPerSecond;
                if (deficit >= 1.0) {
                    int toSubtract = (int) deficit;
                    deficit -= toSubtract;
                    player.setFoodLevel(Math.max(0, player.getFoodLevel() - toSubtract));
                }
                foodDeficit.put(uuid, deficit);
            }
        }
    }

    /**
     * Determines whether a player bypasses passive hunger depletion.
     */
    private boolean shouldBypass(Player player) {
        if (plugin.isExcludeCreativeSpectator() && 
            (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
            return true;
        }

        if (plugin.isExcludePeaceful() && 
            player.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            return true;
        }

        if (plugin.isExcludeBypassPermission() && 
            player.hasPermission("passivehunger.bypass")) {
            return true;
        }

        if (player.getFoodLevel() < plugin.getMinFoodThreshold()) {
            return true;
        }

        return false;
    }

    /**
     * Clears a player's deficit tracking.
     */
    public void removePlayer(UUID uuid) {
        foodDeficit.remove(uuid);
    }

    /**
     * Clears the entire tracking map.
     */
    public void clearCache() {
        foodDeficit.clear();
    }
}
