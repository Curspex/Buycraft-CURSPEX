package net.buycraft.ui;

import org.bukkit.entity.Player;

public interface BuyInterface {

	public boolean showPackage(Player player, int packageId);

	public void showCategoryPage(Player player, int pageNumber);

	public void showPage(Player player, int categoryId, int pageNumber);

	public void packagesReset();

	public void pluginReloaded();

}