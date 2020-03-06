package com.hiccup01.MultiSwap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

public class SwapPlayersTask implements Runnable {
	private final MultiSwap plugin;
	public SwapPlayersTask(JavaPlugin plugin) {
		this.plugin = (MultiSwap)plugin;
	}
	@Override
	public void run() {
		plugin.swapPlayers();
	}
}
