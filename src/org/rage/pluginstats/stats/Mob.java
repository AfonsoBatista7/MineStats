package org.rage.pluginstats.stats;

import org.bson.Document;

/**
 * @author Afonso Batista
 * 2021 - 2023
 */
public class Mob {
	
	private String mName;
	private int mId;
	private long mNumKilled;
	
	public Mob(int mId, String mName, long mNumKilled) {
		this.mId = mId;
		this.mName = mName;
		this.mNumKilled = mNumKilled;
	}
	
	public Mob(int mId, String mName) {
		this.mId = mId;
		this.mName = mName;		
		this.mNumKilled = 0;
	}
	
	public Document createMobDocument() {
		return new Document("mId", mId)
				.append("mName", mName)
				.append("mNumKilled", mNumKilled);
	}
	
	public int getMobId() {
		return mId;
	}
	
	public String getMobName() {
		return mName;
	}
	
	public long getMobKilled() {
		return mNumKilled;
	}
	
	public void setMobId(int mId) {
		this.mId = mId;
	}
	
	public void setMobName(String mName) {
		this.mName = mName;
	}
	
	public void setNumMobKilled(long mNumKilled) {
		this.mNumKilled = mNumKilled;
	}
	
	public void incNumMobKilled() {
		this.mNumKilled++;
	}
}
