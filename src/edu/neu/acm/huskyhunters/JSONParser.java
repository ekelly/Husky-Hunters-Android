package edu.neu.acm.huskyhunters;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser {

	/*
	public ArrayList<Clue> generate(Integer cluenum) {
		ArrayList<Clue> clues = new ArrayList<Clue>();
		try {
			//clues.add();
			//object.put("score", new Integer(200));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(clues);
		return clues;
	}
	*/
	
	public ClueArray parse(String line) {
		ClueArray clues = new ClueArray();
		try {
			JSONArray jsonArray = new JSONArray(line);
			Log.i(JSONFetcher.class.getName(),
					"Number of entries " + jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				
				int clueNum = obj.getInt("clueNum");
				String answer = obj.getString("answer");
				String originalClue = obj.getString("originalClue");
				int points = obj.getInt("points");
				String location = obj.getString("location");
				int numTeamMembers = obj.getInt("numTeamMembers");
				boolean solved = obj.getBoolean("solved");
				
				new Clue(clueNum, answer, originalClue, points, 
						location, numTeamMembers, solved);
				Log.i(JSONFetcher.class.getName(), obj.getString("originalClue"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return clues;
	}
	
}
