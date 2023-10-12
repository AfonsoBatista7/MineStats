package org.rage.pluginstats.stats;

import org.bson.Document;

public class Block {
	private String bName;
	private int bId;
	private long bNumDestroyed,
				 bNumPlaced;

	
	public Block(int bId, String bName, long bNumDestroyed, long bNumPlaced) {
		this.bId = bId;
		this.bName = bName;
		this.bNumDestroyed = bNumDestroyed;
		this.bNumPlaced = bNumPlaced;

	}
	
	public Block(int bId, String bName) {
		this.bId = bId;
		this.bName = bName;
		this.bNumDestroyed = 0;
		this.bNumPlaced = 0;
	}
	
	public Document createBlockDocument() {
		return new Document("bId", bId)
				.append("bName", bName)
				.append("bNumDestroyed", bNumDestroyed)
				.append("bNumPlaced", bNumPlaced);

	}
	
	public int getBlockId() {
		return bId;
	}
	
	public String getBlockName() {
		return bName;
	}
	
	public long getBlockDestroyed() {
		return bNumDestroyed;
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
	
	public void setNumBlocksDestroyed(long bNumBreaked) {
		this.bNumDestroyed = bNumBreaked;
	}
	
	public void incNumBlocksDestroyed() {
		this.bNumDestroyed++;
	}
	
	public void setNumBlocksPlaced(long bNumPlaced) {
		this.bNumPlaced = bNumPlaced;
	}
	
	public void incNumBlocksPlaced() {
		this.bNumPlaced++;
	}
}
