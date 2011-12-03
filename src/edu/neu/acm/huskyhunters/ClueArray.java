package edu.neu.acm.huskyhunters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClueArray extends ArrayList<Clue> /* implements Parcelable */ {
	
	// Generated serial id
	private static final long serialVersionUID = 5431915875340174154L;

	public ArrayList<? extends Map<String, ?>> mappify() {
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		for(int i = 0; i < this.size(); i++) {
			list.add(this.get(i).toMap());
		}
		return list;
	}
	
/*
	
	@Override
	public int describeContents() {
		return 0;
	}

/*
	@Override
	public void writeToParcel(Parcel out, int arg1) {
		
		int size = this.size();
		out.writeInt(size);
		for (int i = 0; i < size; i++) {
            Clue c = this.get(i);
            //c.writeToParcel(out, 0);
            out.writeInt(c.clueNum());
    		out.writeString(c.answer());
    		out.writeString(c.originalClue());
    		out.writeInt(c.points());
    		out.writeString(c.location());
    		out.writeInt(c.numTeamMembers());
    		
    		// Convert the boolean to a byte
    		byte convBool = -1;
    		if (c.solved()) {
    		    convBool = 1;
    		} else {
    		    convBool = 0;
    		}
    		out.writeByte(convBool);
		}
	}
*/	

}
