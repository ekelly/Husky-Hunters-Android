package edu.neu.acm.huskyhunters;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.neu.acm.huskyhunters.Constants;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class Clue implements Parcelable {

	private String clueNum;
	private String answer;
	private String clue;
	private Integer points;
	private String location;
	private String solved;
	private Double[] latlng;
	private List<String> photo;
	
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
	public String solved() {
		return solved;
	}
	public String setSolved(String isSolved) {
		this.solved = isSolved;
		return isSolved;
	}
	
	public Double[] latlng() {
		return latlng;
	}
	
	public List<String> photo() {
		return photo;
	}
	
	public Clue(String clueNum, String answer, String clue, int points, 
			String location, String solved, Double[] ll, LinkedList<String> photo) {
		this.clueNum = clueNum;
		this.answer = answer;
		this.clue = clue;
		this.points = points;
		this.location = location;
		this.solved = solved;
		this.latlng = ll;
		this.photo = photo;
	}
	
	Clue(Parcel in) {
		this.clueNum = in.readString();
		this.answer = in.readString();
		this.clue = in.readString();
		this.points = in.readInt();
		this.location = in.readString();
		this.solved = in.readString();	
		this.latlng[0] = in.readDouble();
		this.latlng[1] = in.readDouble();
		
		this.photo = new LinkedList<String>();
		in.readStringList(this.photo);
	}
	
	Clue(Cursor c, Cursor photos) {
		c.moveToFirst();
		this.clueNum  = c.getString(c.getColumnIndex(Constants.KEY_CLUEID));
		this.answer   = c.getString(c.getColumnIndex(Constants.KEY_ANS));
		this.clue     = c.getString(c.getColumnIndex(Constants.KEY_TEXT));
		this.points   = c.getInt(c.getColumnIndex(Constants.KEY_POINTS));
		this.location = "";
		this.solved   = c.getString(c.getColumnIndex(Constants.KEY_SOLVED));
		
		// Photos are kept in another table
		this.photo = new LinkedList<String>();
		if(photos.getCount() > 0) {
			photos.moveToFirst();
			int col = c.getColumnIndex(Constants.KEY_PHOTO_PATH);
			while(photos.isAfterLast()) {
				this.photo.add(photos.getString(col));
				photos.moveToNext();
			}
		}
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
		out.writeString(solved());
		out.writeDouble(latlng[0]);
		out.writeDouble(latlng[1]);
		out.writeStringList(photo());
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
