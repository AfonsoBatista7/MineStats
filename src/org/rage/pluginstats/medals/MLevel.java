package org.rage.pluginstats.medals;

/**
 * @author Afonso Batista
 * 2021
 */
public enum MLevel {
	
	// index, sound level
	I(1, 1), II(2, 15), III(3, 30), GOD(4, 50);
	
	private int number, soundLevel;
	
	MLevel(int number, int soundLevel) {
		this.number = number;
		this.soundLevel = soundLevel;
	}
	
	public int getNumber() {
		return number;
	}
	
	public int getSoudLevel() {
		return soundLevel;
	}
	
}
