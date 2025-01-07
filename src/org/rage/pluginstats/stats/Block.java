package org.rage.pluginstats.stats;

import org.bson.Document;

/**
 * @author Afonso Batista
 * 2021 - 2023
 */
public class Block {
	private String bName;
	private long bNumDestroyed,
				 bNumPlaced;

	
	public Block(String bName, long bNumDestroyed, long bNumPlaced) {
		this.bName = bName;
		this.bNumDestroyed = bNumDestroyed;
		this.bNumPlaced = bNumPlaced;

	}
	
	public Block(String bName) {
		this.bName = bName;
		this.bNumDestroyed = 0;
		this.bNumPlaced = 0;
	}
	
	public Document createBlockDocument() {
		return new Document("bName", bName)
				.append("bNumDestroyed", bNumDestroyed)
				.append("bNumPlaced", bNumPlaced);
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
