package org.rage.pluginstats.tags;

public enum Tags {
	
	MEMBER(""),
	ADMIN("admin"),
	BETA("beta"),
	GOD("god", "&f&l"),
	DONATOR("donator"),
	BUILDER("builder"),
	DESTROYER("destroyer"),
	PVP("pvp"),
	MOBSLAYER("mobslayer"),
	TRAVELLER("traveller"),
	ARCHIVIST("archivist"),
	REDSTONE("redstone", "&c"),
	VETERAN("veteran"),
	SKIPDEATH("skipdeath"),
	ENDERKILLER("enderkiller"),
	NETHERKILLER("netherkiller"),
	TIMEWALKER("timewalker"),
	FISHERMAN("fisherman", "&b"),
	NAMEHOLDER("nameholder"),
	MINER("miner");
	
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
