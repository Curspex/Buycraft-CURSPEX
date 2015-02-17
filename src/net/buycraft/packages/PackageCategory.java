package net.buycraft.packages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;

public class PackageCategory {

	private final List<PackageModal> packages;
	protected int niceId;
	private final int id;
	private final String name, description;
	private final Material guiItem;

	public PackageCategory(int id, String name, String description, int guiItemId)
	{
		this.packages = new ArrayList<PackageModal>(1);
		this.id = id;
		this.name = name;
		this.description = description != null && description.length() > 0 ? description : null;
		this.guiItem = Material.getMaterial(guiItemId);
	}

	public int getNiceId()
	{
		return niceId;
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}

	public Material getGuiItem()
	{
		return guiItem;
	}

	protected void addPackage(PackageModal p)
	{
		packages.add(p);
	}

	public List<PackageModal> getPackages()
	{
		return Collections.unmodifiableList(packages);
	}

}