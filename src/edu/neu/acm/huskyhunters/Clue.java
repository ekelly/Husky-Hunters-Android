package edu.neu.acm.huskyhunters;

import android.os.Parcel;
import android.util.Log;

public class Clue {

	int clueNum;
	String answer;
	String originalClue;
	int points;
	String location;
	int numTeamMembers;
	boolean solved;
	
	public Clue(int clueNum, String answer, String originalClue, int points, 
			String location, int numTeamMembers, boolean solved) {
		this.clueNum = clueNum;
		this.answer = answer;
		this.originalClue = originalClue;
		this.points = points;
		this.location = location;
		this.numTeamMembers = numTeamMembers;
		this.solved = solved;
	}
	
	Clue(Parcel in) {
		clueNum = in.readInt();
		answer = in.readString();
		originalClue = in.readString();
		points = in.readInt();
		location = in.readString();
		numTeamMembers = in.readInt();
		
		byte convBool = in.readByte();
		if(convBool == 0) {
			solved = false;
		} else if(convBool == 1) {
			solved = true;
		} else {
			Log.i("Clues Unpack", "Boolean solved error");
		}
	}
	
}
