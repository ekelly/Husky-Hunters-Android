package edu.neu.acm.huskyhunters;

import java.io.Closeable;

import android.content.Context;
import android.database.SQLException;
import android.text.format.Time;
import android.util.Log;

/**
 * A database system for loading and accessing clues.  Includes methods for 
 * retrieving clues from server, storing them in database, and 
 * @author francis
 *
 */
public class ClueDb implements Closeable {
	private static final String TAG = "ClueDb";
	
	// Context from which to work
	private final Context mCtx;
	
	// long variable for storing last update time, saved in Unix format
	// Should be updated with database on every sync.
	private Time lastUpdateTime;
	
	private ClueDbAdapter mDbHelper;
	
	public ClueDb(Context ctx) {
		this.mCtx = ctx;
		mDbHelper = new ClueDbAdapter(mCtx);
		try {
			mDbHelper.open();
			lastUpdateTime = new Time();
			lastUpdateTime.set(mDbHelper.getTime());
		}
		catch(SQLException ex) {
			Log.e(TAG, "Failed to open clue database.", ex);
		}
	}

	@Override
	public void close() {
		
		
	}
	// Updates the time store in the database to indicate the current time.
	// Should be used when a sync is completed
	public void setTimeToNow() {
		lastUpdateTime.setToNow();
		mDbHelper.setTime(lastUpdateTime.toMillis(false));
	}
	
	

}
