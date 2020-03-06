package com.hiccup01.MultiSwap;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class MultiSwap extends JavaPlugin implements Listener {
	private ArrayList<Player> bannedPlayers = new ArrayList<>();
	private BukkitScheduler scheduler;

	public void multiSwap() {

	}

	@Override
	public void onEnable() {
		getLogger().info("MultiSwap has been enabled!");
		bannedPlayers.clear();
		scheduler = getServer().getScheduler();
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		scheduler.cancelTasks(this);
		getLogger().info("MultiSwap has been disabled...");
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		event.getEntity().setGameMode(GameMode.SPECTATOR);
		bannedPlayers.add((Player)event.getEntity());
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if(bannedPlayers.contains(event.getPlayer())) {
			event.getPlayer().setGameMode(GameMode.SPECTATOR);
		}
		event.getPlayer().setGameMode(GameMode.SURVIVAL);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("startMultiSwap")) {
			final int SECONDS = 10;
			scheduler.scheduleSyncRepeatingTask(this, new SwapPlayersTask(this), 0L, SECONDS * 20L);
			return true;
		}
		return false;
	}

	public void swapPlayers() {
		ArrayList<Player> swappingPlayers = new ArrayList<>(Bukkit.getServer().getOnlinePlayers());
		swappingPlayers.removeAll(bannedPlayers);
		List<Location> locations = new ArrayList<>();
		for(int i = 0; i < swappingPlayers.size(); i++) {
			locations.add(swappingPlayers.get(i).getLocation());
		}
		Collections.shuffle(locations);
		getLogger().info("Swapping!");
		for(int i = 0; i < swappingPlayers.size(); i++) {
			swappingPlayers.get(i).teleport(locations.get(i));
		}
	}
}
