package org.rage.pluginstats.tags;

public enum Tags {
	
	MEMBER(""),
	ADMIN("admin"),
	BETA("beta", "&a"),
	GOD("god", "&f&l"),
	DONATOR("donator"),
	BUILDER("builder", "&e"),
	DESTROYER("destroyer"),
	PVP("pvp", "&1"),
	MOBSLAYER("mobslayer"),
	TRAVELLER("traveller", "&2"),
	ARCHIVIST("archivist"),
	REDSTONE("redstone", "&c"),
	VETERAN("veteran"),
	SKIPDEATH("skipdeath"),
	ENDERKILLER("enderkiller", "&5"),
	NETHERKILLER("netherkiller", "&4"),
	TIMEWALKER("timewalker"),
	FISHERMAN("fisherman", "&b"),
	MINER("miner", "&7");
	
	private String tag, color;
	private boolean haveCustomColor;
	
	Tags(String tag) {
		this.tag = tag;
		haveCustomColor = false;
	}
	
	Tags(String tag, String color) {
		this.tag = tag;
		this.color = color;
		haveCustomColor = true;
	}
	
	public String getTag() {
		return tag;
	}
	
	public String getColor() {
		return color;
	}
	
	public boolean hasCustomColor() {
		return haveCustomColor;
	}

}
