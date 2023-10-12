package org.rage.pluginstats.stats;

import org.bson.Document;

public class Block {
	private String bName;
	private int bId;
	private long bNumBreaked,
				 bNumPlaced;

	
	public Block(int bId, String bName, long bNumBreaked, long bNumPlaced) {
		this.bId = bId;
		this.bName = bName;
		this.bNumBreaked = bNumBreaked;
		this.bNumPlaced = bNumPlaced;

	}
	
	public Block(int bId, String bName) {
		this.bId = bId;
		this.bName = bName;
	}
	
	public Document createMobDocument() {
		return new Document("bId", bId)
				.append("bName", bName)
				.append("bNumBreaked", bNumBreaked)
				.append("bNumPlaced", bNumPlaced);

	}
	
	public int getBlockId() {
		return bId;
	}
	
	public String getBlockName() {
		return bName;
	}
	
	public long getBlockBreaked() {
		return bNumBreaked;
	}
	
	public long getBlockPlaced() {
		return bNumPlaced;
	}
	
	public void setBlockId(int bId) {
		this.bId = bId;
	}
	
	public void setBlockName(String bName) {
		this.bName = bName;
	}
	
	public void setNumBlocksBreaked(long bNumBreaked) {
		this.bNumBreaked = bNumBreaked;
	}
	
	public void incNumBlocksBreaked() {
		this.bNumBreaked++;
	}
	
	public void setNumBlocksPlaced(long bNumPlaced) {
		this.bNumPlaced = bNumPlaced;
	}
	
	public void incNumBlocksPlaced() {
		this.bNumBreaked++;
	}
}
