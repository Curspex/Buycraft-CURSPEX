package net.buycraft.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.buycraft.Plugin;
import net.buycraft.packages.ItemParser;
import net.buycraft.packages.ItemParser.ItemType;
import net.buycraft.packages.PackageCategory;
import net.buycraft.util.BuycraftInventoryCreator;
import net.buycraft.util.Chat;
import net.buycraft.util.BuycraftInventoryCreator.BuycraftInventoryType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BuyInventoryUI extends AbstractBuyUI implements Listener, InventoryHolder {

	private int expectedInventoryHolderId = 0;
	private HashMap<String, Inventory> buyMenus;
	private HashMap<String, String> menuKeys;

	private boolean useMainMenu = false;

	public BuyInventoryUI() 
	{
		// Register the listener
		Bukkit.getPluginManager().registerEvents(this, Plugin.getInstance());
	}

	/**
	 * Listens for inventory clicks which occur within BuyCraft inventories
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public synchronized void onInventoryClick(InventoryClickEvent event)
	{
		Inventory inv = event.getInventory();
		// Check if the player is in a Buycraft Inventory
		/*if (inv.getType() != InventoryType.CHEST || !(inv.getHolder() instanceof BuyMenuInventoryHolder) || !(event.getWhoClicked() instanceof Player))
		{
			return;
		}*/

		if (!inv.getName().contains("Buycraft"))
		{
			return;
		}

		event.setCancelled(true);
		event.getCurrentItem().setAmount(0);
		((Player) event.getWhoClicked()).updateInventory();

		BuyMenuInventoryHolder holder = (BuyMenuInventoryHolder) inv.getHolder();

		// Check that the inventory is valid
		if (holder.id != expectedInventoryHolderId)
		{
			while (!inv.getViewers().isEmpty())
			{
				inv.getViewers().get(0).closeInventory();
			}
			return;
		}

		String key = menuKeys.get(event.getInventory().getName());

		// Check the player is clicking inside our inventory
		if (event.getRawSlot() >= inv.getSize())
		{
			return;
		}

		// Find out what the player is doing
		BuycraftInventoryType type = BuycraftInventoryType.getType(key);
		key = type.stripType(key);
		if (type == BuycraftInventoryType.MAIN_MENU)
		{
			handleCategoryMenuClick(event, Integer.valueOf(key));
		}
		
		else
		{
			int currentCategoryId = -1;
			int currentPage = 0;

			int index = key.indexOf('-');
			if (index == -1)
				currentPage = Integer.valueOf(key);
			else
			{
				currentCategoryId = Integer.valueOf(key.substring(0, index));
				currentPage = Integer.valueOf(key.substring(index+1));
			}
			handleCategoryViewClick(event, currentCategoryId, currentPage);
		}
	}

	private synchronized void handleCategoryMenuClick(InventoryClickEvent event, int currentPage)
	{
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
		{
			return;
		}

		ItemType t = ItemType.checkType(event.getCurrentItem());

		// Clicking in an invalid place
		if (t == null)
		{
			return;
		}

		if (t != ItemType.OTHER)
		{
			currentPage += t == ItemType.NEXT ? 1 : -1;
			showCategoryPage((Player) event.getWhoClicked(), currentPage);
			return;
		}

		// Show category items
		PackageCategory c = ItemParser.getCategory(event.getCurrentItem());

		// Invalid category
		if (c == null)
		{
			Plugin.getInstance().getLogger().severe("Failed to find PackageCategory shown in inventory");
			event.getInventory().setItem(event.getSlot(), null);
			return;
		}
		
		Player player = (Player) event.getWhoClicked();
		player.playSound(player.getLocation(), Sound.CLICK, 0.5f, 1f);
		
		event.getWhoClicked().closeInventory();
		showPage((Player) event.getWhoClicked(), c.getNiceId(), 1);
	}

	private synchronized void handleCategoryViewClick(InventoryClickEvent event, int currentCategoryId, int currentPage)
	{

		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
		{
			return;
		}
		
		Player player = (Player) event.getWhoClicked();
		player.playSound(player.getLocation(), Sound.CLICK, 0.5f, 1f);
		
		ItemType t = ItemType.checkType(event.getCurrentItem());

		// Clicking in an invalid place
		if (t == null)
		{
			return;
		}

		if (t == ItemType.HOME)
		{
			event.getWhoClicked().closeInventory();
			showCategoryPage((Player) event.getWhoClicked(), 1);
			return;
		}

		if (t != ItemType.OTHER)
		{
			currentPage += t == ItemType.NEXT ? 1 : -1;
			event.getWhoClicked().closeInventory();
			showPage((Player) event.getWhoClicked(), currentCategoryId, currentPage);
			return;
		}

		int orderId = ItemParser.getPackage(event.getCurrentItem());

		event.getWhoClicked().closeInventory();
		if (!showPackage((Player) event.getWhoClicked(), orderId))
		{
			// Invalid package
			event.getInventory().setItem(event.getSlot(), null);
		}
	}

	public synchronized void showCategoryPage(Player player, int pageNumber)
	{
		if (!useMainMenu)
		{
			showPage(player, 0, 1);
			return;
		}

		if (!checkReady(player))
		{
			return;
		}

		String key = BuycraftInventoryType.MAIN_MENU.toString() + pageNumber;
		Inventory inv = buyMenus.get(key);

		if (inv == null)
		{
			player.sendMessage(Chat.header());
			player.sendMessage(Chat.seperator());
			player.sendMessage(Chat.seperator() + Plugin.getInstance().getLanguage().getString("pageNotFound"));
			player.sendMessage(Chat.seperator());
			player.sendMessage(Chat.footer());
		}

		else
		{
			player.openInventory(inv);
		}
	}

	public synchronized void showPage(Player player, int categoryId, int pageNumber)
	{
		if (!checkReady(player))
		{
			return;
		}

		if (pageNumber == 0)
		{
			if (useMainMenu)
			{
				showCategoryPage(player, 1);
				return;
			}
			pageNumber = 1;
		}

		String key = BuycraftInventoryType.CATEGORY_MENU.toString() + categoryId + "-" + pageNumber;
		Inventory inv = buyMenus.get(key);

		if (inv == null)
		{
			player.sendMessage(Chat.header());
			player.sendMessage(Chat.seperator());
			player.sendMessage(Chat.seperator() + Plugin.getInstance().getLanguage().getString("pageNotFound"));
			player.sendMessage(Chat.seperator());
			player.sendMessage(Chat.footer());
		}
		
		else
		{
			player.openInventory(inv);
		}
	}

	public synchronized void packagesReset()
	{
		HashMap<String, Inventory> newBuyMenus = new HashMap<String, Inventory>();

		List<PackageCategory> categories = Plugin.getInstance().getPackageManager().getCategories();
		boolean useMainMenu = false;
		BuyMenuInventoryHolder invHolder = new BuyMenuInventoryHolder(++expectedInventoryHolderId);

		if (categories.size() > 1)
		{
			useMainMenu = true;
			BuycraftInventoryCreator.createMainMenu(invHolder, newBuyMenus, categories);
		}

		for (PackageCategory c : categories)
		{
			BuycraftInventoryCreator.createPackagePages(invHolder, newBuyMenus, c, useMainMenu);
		}

		HashMap<String, String> newMenuKeys = new HashMap<String, String>(newBuyMenus.size());

		for (Entry<String, Inventory> e : newBuyMenus.entrySet())
		{
			newMenuKeys.put(e.getValue().getName(), e.getKey());
		}

		this.useMainMenu = useMainMenu;
		this.menuKeys = newMenuKeys;
		this.buyMenus = newBuyMenus;
	}

	public synchronized void pluginReloaded()
	{
		if (buyMenus != null)
		{
			for (Inventory inv : buyMenus.values())
			{
				while (!inv.getViewers().isEmpty())
				{
					inv.getViewers().get(0).closeInventory();
				}
			}
		}
	}

	public synchronized boolean checkReady(Player player)
	{
		if (buyMenus == null)
		{
			player.sendMessage(Chat.header());
			player.sendMessage(Chat.seperator());
			player.sendMessage(Chat.seperator() + ChatColor.RED + Plugin.getInstance().getLanguage().getString("inventoryMenuNotReady"));
			player.sendMessage(Chat.seperator());
			player.sendMessage(Chat.footer());
			return false;
		}
		return true;
	}

	public Inventory getInventory()
	{
		throw new UnsupportedOperationException("BuyInventoryUI does not support this method call");
	}

	private class BuyMenuInventoryHolder implements InventoryHolder
	{
		public final int id;

		BuyMenuInventoryHolder(int id)
		{
			this.id = id;
		}

		public Inventory getInventory()
		{
			return null;
		}
	}

}