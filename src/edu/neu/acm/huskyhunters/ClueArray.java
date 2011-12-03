package edu.neu.acm.huskyhunters;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class ClueArray extends ArrayList<Clue> implements Parcelable {
	
	/**
	 *   Generated serial id
	 */
	private static final long serialVersionUID = 5431915875340174154L;

	@Override
	public int describeContents() {
		return 0;
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

	@Override
	public void writeToParcel(Parcel out, int arg1) {
		
		int size = this.size();
		out.writeInt(size);
		for (int i = 0; i < size; i++) {
            Clue c = this.get(i);
            
            out.writeInt(c.clueNum);
    		out.writeString(c.answer);
    		out.writeString(c.originalClue);
    		out.writeInt(c.points);
    		out.writeString(c.location);
    		out.writeInt(c.numTeamMembers);
    		
    		// Convert the boolean to a byte
    		byte convBool = -1;
    		if (c.solved) {
    		    convBool = 1;
    		} else {
    		    convBool = 0;
    		}
    		out.writeByte(convBool);
		}
	}

}
