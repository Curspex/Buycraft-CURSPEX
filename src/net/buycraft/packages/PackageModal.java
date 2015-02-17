package net.buycraft.packages;

import org.bukkit.Material;

public class PackageModal {

	private final PackageCategory category;
	private final int id, order;
	private final Material material;
	private final String name, description, price;

	@SuppressWarnings("deprecation")
	public PackageModal(PackageCategory category, int id, int itemId, String name, String description, String price, int order)
	{
		this.category = category;
		this.id = id;
		this.material = Material.getMaterial(itemId);
		this.name = name;
		this.description = description != null && description.length() > 0 ? description : null;
		this.price = price;
		this.order = order;

		if (category != null)
			category.addPackage(this);
	}

	public PackageCategory getCategory()
	{
		return category;
	}

	public int getId()
	{
		return id;
	}

	public Material getMaterial()
	{
		return material;
	}

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}

	public String getPrice()
	{
		return price;
	}

	public int getOrder()
	{
		return order;
	}

}