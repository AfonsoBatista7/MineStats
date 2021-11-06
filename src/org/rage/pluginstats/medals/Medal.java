package org.rage.pluginstats.medals;

/**
 * @author Afonso Batista
 * 2021
 */
public class Medal {
	
	private Medals medal;
	private MLevel level;
	
	public Medal(Medals medal) {
		this.medal = medal;
		this.level = medal.getMedalLevel();
	}
	
	public Medal(Medals medal, MLevel level) {
		this.medal = medal;
		this.level = level;
	}
	
	public MLevel getMedalLevel() {
		return level;
	}
	
	public Medals getMedal() {
		return medal;
	}
	
	public int getIndex() {
		return medal.getIndex();
	}
	
	public boolean checkLevelTransition(long stat) {
		
		double multiplyer = Math.pow(medal.getMultiplyer(), level.getNumber());
		long transition = (long) ((int) medal.getTransition() * multiplyer);

		if(stat >= transition && level!=MLevel.GOD) {
			levelUp(level);
			checkLevelTransition(stat);
			return true;
		}
		
		return false;
	}
	
	public void levelUp(MLevel level) {
		switch(level) {
		case I:
			this.level = MLevel.II;
			break;
		case II:
			this.level = MLevel.III;
			break;
		case III:
			this.level = MLevel.GOD;
			break;
		default:
		}
	}
	
}
