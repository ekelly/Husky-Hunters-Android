package edu.neu.acm.huskyhunters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

public class CluesData {
	private static final String TAG = "CluesData";

	private static CluesData sInstance = null;

	private Context mContext;
	private ClueArray mClues = new ClueArray();
	private ArrayList<? extends Map<String, ?>> mClueArrayMap;
	private boolean mIsLoaded = false;
	
	class ClueAdapter extends SimpleAdapter {
    	
		public ClueAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		  View view = super.getView(position, convertView, parent);
		  
		  @SuppressWarnings("unchecked")
		  HashMap<String, String> c = (HashMap<String, String>) getItem(position);
		  
		  if (Boolean.parseBoolean(c.get("solved"))) {
			  view.setBackgroundColor(Color.GREEN);
		  }
		  
		  return view;
		}
    }

	public ArrayList<Clue> getArray() {
		return mClues;
	}
	
	private CluesData(Context context) {
		Log.i(TAG, "CluesData instantiated");
		mContext = context;
	}
	
	public void setData(ClueArray newClues) {
		this.mClues = newClues;
		this.mClueArrayMap = mClues.mappify();
	}
	
	public SimpleAdapter getAdapter() {
				
		return new ClueAdapter(mContext, mClueArrayMap, 
				R.layout.clue_item, 
		    	new String[] { "clueNum", "answer", "points" }, 
		    	new int[] { R.id.cluenum, R.id.answer, R.id.points });
	}
	
	protected List<Map<String, String>> getClueMap() {
		List<Map<String,String>> clueMaps = new ArrayList<Map<String, String>>();
		
		for(Clue c : mClues) { 
			clueMaps.add(c.toMap());
		}
		
		return clueMaps;
	}
	
	public static CluesData getInstance(Context context) { 
		if ( sInstance == null ) { 
			sInstance = new CluesData(context);
		}
		
		return sInstance;
	}
	
	public boolean isLoaded() { return mIsLoaded; }
	
	public boolean load(String groupHash) {
		// Send HTTP Request to server
		// Parse JSON Response
		// Store in mClues
		
		try {
			mClues = parseClues(requestClues(groupHash));
			mClueArrayMap = mClues.mappify();
		} catch(Exception e) {
			e.printStackTrace();
			mIsLoaded = false;
			return false;
		}
		
		mIsLoaded = true;
		return true;
	}
	
	protected String getCluesUrl(String groupHash) {
		return "http://hillcrest.roderic.us/api/teams/" + groupHash + "/clues/";
	}
	
	protected String requestClues(String groupHash) {
		StringBuilder builder = new StringBuilder();
		
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(getCluesUrl(groupHash));
		
		try {
			HttpResponse response = client.execute(request);

			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e(TAG, "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return builder.toString();
	}
	
	protected ClueArray parseClues(String data) {
		ClueArray clues = new ClueArray();
		
		try {
			JSONArray jsonArray = new JSONArray(data);
			Log.i(TAG, "Number of entries " + jsonArray.length());
			
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				
				int clueNum = obj.getInt("clue_number");
				String answer = obj.getString("answer");
				String originalClue = obj.getString("original_text");
				int points = obj.getInt("points");
				String location = obj.getString("location");
				boolean solved = obj.getBoolean("is_solved");
				JSONArray ll = obj.getJSONArray("latlng");
				double[] latlng = { (Double) ll.get(0), (Double) ll.get(1) };
				
				clues.add(new Clue(clueNum, answer, originalClue, points, 
						location, solved, latlng));
				
				Log.i(TAG, obj.getString("original_text"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return clues;
	}
}
