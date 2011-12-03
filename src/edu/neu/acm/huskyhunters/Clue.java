package edu.neu.acm.huskyhunters;

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Clue implements Parcelable {

	private Integer clueNum;
	private String answer;
	private String clue;
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
	public String clue() {
		return clue;
	}
	public String location() {
		return location;
	}
	public Boolean solved() {
		return solved;
	}
	
	public Clue(int clueNum, String answer, String clue, int points, 
			String location, int numTeamMembers, boolean solved) {
		this.clueNum = clueNum;
		this.answer = answer;
		this.clue = clue;
		this.points = points;
		this.location = location;
		this.numTeamMembers = numTeamMembers;
		this.solved = solved;
	}
	
	Clue(Parcel in) {
		clueNum = in.readInt();
		answer = in.readString();
		clue = in.readString();
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
		map.put("clue", clue);
		map.put("points", points.toString());
		map.put("location", location);
		map.put("numTeamMembers", numTeamMembers.toString());
		map.put("solved", solved.toString());
		return map;
	}
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(clueNum());
		out.writeString(answer());
		out.writeString(clue());
		out.writeInt(points());
		out.writeString(location());
		out.writeInt(numTeamMembers());
		
		// Convert the boolean to a byte
		byte convBool = -1;
		if (solved()) {
		    convBool = 1;
		} else {
		    convBool = 0;
		}
		out.writeByte(convBool);
	}
	
	public static final Parcelable.Creator<Clue> CREATOR
	    = new Parcelable.Creator<Clue>() {
		public Clue createFromParcel(Parcel in) {
		    return new Clue(in);
		}
		
		public Clue[] newArray(int size) {
		    return new Clue[size];
		}
	};
	
}
