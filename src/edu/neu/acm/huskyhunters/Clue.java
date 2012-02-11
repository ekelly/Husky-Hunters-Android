package edu.neu.acm.huskyhunters;

import java.util.HashMap;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Clue implements Parcelable {

	private String clueNum;
	private String answer;
	private String clue;
	private Integer points;
	private String location;
	private Boolean solved;
	//private double[] latlng;
	private String photo;
	
	public String clueNum() {
		return clueNum;
	}
	public Integer points() {
		return points;
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
	public Boolean setSolved(Boolean isSolved) {
		this.solved = isSolved;
		return isSolved;
	}
	/*
	public double[] latlng() {
		return latlng;
	}
	*/
	public String photo() {
		return photo;
	}
	
	public Clue(String clueNum, String answer, String clue, int points, 
			String location, boolean solved, /* double[] ll, */ String photo) {
		this.clueNum = clueNum;
		this.answer = answer;
		this.clue = clue;
		this.points = points;
		this.location = location;
		this.solved = solved;
		//this.latlng = ll;
		this.photo = photo;
	}
	
	Clue(Parcel in) {
		clueNum = in.readString();
		answer = in.readString();
		clue = in.readString();
		points = in.readInt();
		location = in.readString();
		
		byte convBool = in.readByte();
		if(convBool == 0) {
			solved = false;
		} else if(convBool == 1) {
			solved = true;
		} else {
			Log.i("Clues Unpack", "Boolean solved error");
		}
		
		//latlng[0] = in.readDouble();
		//latlng[1] = in.readDouble();
		
		photo = in.readString();
	}
	
	Clue(Cursor c) {
		c.moveToFirst();
		int temp = c.getCount();
		this.clueNum  = c.getString(c.getColumnIndex("clueid"));
		this.answer   = c.getString(c.getColumnIndex("ans"));
		this.clue     = c.getString(c.getColumnIndex("cluetext"));
		this.points   = c.getInt(c.getColumnIndex("points"));
		this.location = "";
		this.solved   = false; // c.getString(c.getColumnIndex("solved"));
		this.photo    = c.getString(c.getColumnIndex("photo_path"));
	}
	
	public HashMap<String, String> toMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("clueNum", clueNum.toString());
		map.put("answer", answer);
		map.put("clue", clue);
		map.put("points", points.toString());
		map.put("location", location);
		//map.put("latlngx", ((Double) latlng[0]).toString());
		//map.put("latlngy", ((Double) latlng[1]).toString());
		map.put("solved", solved.toString());
		return map;
	}
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(clueNum());
		out.writeString(answer());
		out.writeString(clue());
		out.writeInt(points());
		out.writeString(location());
		
		// Convert the solved boolean to a byte
		// 1 = solved, 
		
		if(solved()) {
			out.writeByte((byte) 1);
		} else {
			out.writeByte((byte) 0);
		}
		
		//out.writeDouble(latlng[0]);
		//out.writeDouble(latlng[1]);
		
		out.writeString(photo());
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
