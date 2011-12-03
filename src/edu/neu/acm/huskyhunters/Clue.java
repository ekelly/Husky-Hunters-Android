package edu.neu.acm.huskyhunters;

import java.util.HashMap;

import android.os.Parcel;
import android.util.Log;

public class Clue {

	private Integer clueNum;
	private String answer;
	private String originalClue;
	private Integer points;
	private String location;
	private Integer numTeamMembers;
	private Boolean solved;
	
	public Integer clueNum() {
		return clueNum;
	}
	public Integer points() {
		return points;
	}
	public Integer numTeamMembers() {
		return numTeamMembers;
	}
	public String answer() {
		return answer;
	}
	public String originalClue() {
		return originalClue;
	}
	public String location() {
		return location;
	}
	public Boolean solved() {
		return solved;
	}
	
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
	
	public HashMap<String, String> toMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("clueNum", clueNum.toString());
		map.put("answer", answer);
		map.put("originalClue", originalClue);
		map.put("points", points.toString());
		map.put("location", location);
		map.put("numTeamMembers", numTeamMembers.toString());
		map.put("solved", solved.toString());
		return map;
	}
	
}
