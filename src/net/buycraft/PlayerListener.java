package net.buycraft;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class PlayerListener implements Listener {
	
	public PlayerListener()
	{
		Plugin.getInstance();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event)
	{
		if (event.getPlayer().getName().equalsIgnoreCase("Buycraft"))
		{
			event.disallow(Result.KICK_OTHER, ChatColor.RED + "This user has been disabled due to security reasons.");
		}
	}
}