package com.hiccup01.MultiSwap;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
	private ArrayList<Player> playingPlayers = new ArrayList<>();
	private boolean gameInProgress = false;
	private int triggersLeft;
	private BukkitScheduler scheduler;

	public void multiSwap() {

	}

	@Override
	public void onEnable() {
		getLogger().info("MultiSwap has been enabled!");
		playingPlayers.clear();
		scheduler = getServer().getScheduler();
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		scheduler.cancelTasks(this);
		playingPlayers.clear();
		getLogger().info("MultiSwap has been disabled...");
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		if(this.gameInProgress) {
			event.getEntity().setGameMode(GameMode.SPECTATOR);
			playingPlayers.remove(event.getEntity());
			if(playingPlayers.size() == 1) {
				getServer().broadcastMessage(playingPlayers.get(0).getDisplayName() + " wins!");
				this.gameInProgress = false;
				playingPlayers.clear();
				scheduler.cancelTasks(this);
			}
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if(this.gameInProgress && !playingPlayers.contains(event.getPlayer())) {
			event.getPlayer().setGameMode(GameMode.SPECTATOR);
		} else {
			event.getPlayer().setGameMode(GameMode.SURVIVAL);
		}
	}

	private void setTriggers() {
		int minTimeBeforeSwap = getConfig().getInt("minTimeBeforeSwap");
		int maxTimeBeforeSwap = getConfig().getInt("maxTimeBeforeSwap");
		this.triggersLeft = ThreadLocalRandom.current().nextInt(minTimeBeforeSwap / 10, (maxTimeBeforeSwap / 10) + 1);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("startMultiSwap")) {
			if(this.gameInProgress) {
				sender.sendMessage("There is already a game in progress. Do /reload to stop it.");
				return true;
			}
			if(getServer().getOnlinePlayers().size() < 2) {
				sender.sendMessage("You need at least 2 players to start the game.");
				return true;
			}
			this.gameInProgress = true;
			this.playingPlayers = new ArrayList<>(getServer().getOnlinePlayers());
			this.setTriggers();
			scheduler.scheduleSyncRepeatingTask(this, new SwapPlayersTask(this), 0L, 10 * 20L);
			return true;
		}
		return false;
	}

	public void swapPlayers() {
		if(this.triggersLeft > 0) {
			this.triggersLeft--;
		} else {
			ArrayList<Player> swappingPlayers = playingPlayers;
			swappingPlayers.retainAll(getServer().getOnlinePlayers());
			List<Location> locations = new ArrayList<>();
			for (int i = 0; i < swappingPlayers.size(); i++) {
				locations.add(swappingPlayers.get(i).getLocation());
			}
			Collections.shuffle(locations);
			getLogger().info("Swapping!");
			getServer().broadcastMessage("Swapping!");
			for (int i = 0; i < swappingPlayers.size(); i++) {
				swappingPlayers.get(i).teleport(locations.get(i));
			}
			this.setTriggers();
		}
	}
}
