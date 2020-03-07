package com.hiccup01.MultiSwap;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
		this.triggersLeft = ThreadLocalRandom.current().nextInt(minTimeBeforeSwap / 10, (maxTimeBeforeSwap / 10) + 1) - 1;
	}

	private void distributePlayers(int seed) {
		int i = 0;
		for(Player player : this.playingPlayers) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5*20, 999, false, false));
			player.teleport(new Location(getServer().getWorld("world"), (seed + 1) * (double)2000, (double)128, i * (double)2000));
			player.getInventory().clear();
			i++;
		}
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
			scheduler.scheduleSyncRepeatingTask(this, new SwapPlayersTask(this), 10 * 20L, 10 * 20L);
			return true;
		} else if(command.getName().equalsIgnoreCase("distribute")) {
			if(args.length > 0) {
				this.distributePlayers(Integer.parseInt(args[0]));
			} else {
				this.distributePlayers(0);
			}
			return true;
		}
		return false;
	}

	public void swapPlayers() {
		if(this.triggersLeft > 0) {
//			getServer().broadcastMessage(String.valueOf(this.triggersLeft * 10) + " seconds left.");
			this.triggersLeft--;
		} else {
			ArrayList<Player> swappingPlayers = playingPlayers;
			swappingPlayers.retainAll(getServer().getOnlinePlayers());
			ArrayList<Player> playerLocations = new ArrayList<>(playingPlayers);
			boolean valid = false;
			while(!valid) {
				Collections.shuffle(playerLocations);
				valid = true;
				for(int i = 0; i < swappingPlayers.size(); i++) {
					if(swappingPlayers.get(i).equals(playerLocations.get(i))) valid = false;
				}
			}
			List<Location> locations = new ArrayList<>();
			for (int i = 0; i < swappingPlayers.size(); i++) {
				locations.add(playerLocations.get(i).getLocation());
			}
			getLogger().info("Swapping!");
			getServer().broadcastMessage("Swapping!");
			for (int i = 0; i < swappingPlayers.size(); i++) {
				swappingPlayers.get(i).teleport(locations.get(i));
			}
			this.setTriggers();
		}
	}
}
