package org.rage.pluginstats.stats;

/**
 * @author Afonso Batista
 * 2021
 */
public class BlockStats {
	
	private long blocksDestroyed,
		 		 blocksPlaced,
		 		 redstoneUsed;

	public BlockStats() {
		blocksDestroyed = 0;
		blocksPlaced = 0;
		redstoneUsed = 0;
	}
	
	public BlockStats(long blocksDestroyed, long blocksPlaced, long redstoneUsed) {
		this.blocksDestroyed = blocksDestroyed;
		this.blocksPlaced = blocksPlaced;
		this.redstoneUsed = redstoneUsed;
	}
	
	public long getBlocksDestroyed() {
		return blocksDestroyed;
	}
	
	public long getBlocksPlaced() {
		return blocksPlaced;
	}
	
	public long getRedstoneUsed() {
		return redstoneUsed;
	}
	
	public long breakBlock() {
		return blocksDestroyed++;
	}
	
	public long placeBlock() {
		return blocksPlaced++;
	}
	
	public long useRedstone() {
		return redstoneUsed++;
	}
	
	public void setBlocksDestroyed(long blocksNum) {
		blocksDestroyed = blocksNum;
	}
	
	public void setBlocksPlaced(long blocksNum) {
		blocksPlaced = blocksNum;
	}
	
	public void setRedstoneUsed(long redstoneNum) {
		redstoneUsed = redstoneNum;
	}
}
