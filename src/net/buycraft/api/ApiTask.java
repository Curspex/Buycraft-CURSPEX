package net.buycraft.api;

import net.buycraft.Plugin;
import net.buycraft.util.Language;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Logger;

public abstract class ApiTask implements Runnable {

	public BukkitTask sync(Runnable r)
	{
		if (getPlugin().isEnabled())
		{
			return Bukkit.getScheduler().runTask(getPlugin(), r);
		}
		return null;
	}

	public BukkitTask syncTimer(Runnable r, long delay, long period)
	{
		if (getPlugin().isEnabled())
		{
			return Bukkit.getScheduler().runTaskTimer(getPlugin(), r, delay, period);
		}
		return null;
	}

	public void addTask(ApiTask task)
	{
		Plugin.getInstance().addTask(task);
	}

	public Plugin getPlugin()
	{
		return Plugin.getInstance();
	}

	public Language getLanguage()
	{
		return Plugin.getInstance().getLanguage();
	}

	public Api getApi()
	{
		return Plugin.getInstance().getApi();
	}

	public Logger getLogger()
	{
		return Plugin.getInstance().getLogger();
	}

}