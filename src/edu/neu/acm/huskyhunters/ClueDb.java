package edu.neu.acm.huskyhunters;

import java.io.Closeable;

import android.content.Context;
import android.database.Cursor;
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
	
	// long variable for storing last update time, saved in UTC millis format
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
			Log.e(TAG, ex.getMessage());
		}
	}
	
	/**
	 * Opens the clues database for writing.  If it cannot be opened, attempt 
	 * to create a new instance.  If this is unsuccessful, throws an
	 * exception.
	 * @throws SQLException if database cannot be opened/created.
	 */
	public void open() throws SQLException {
		try {
			mDbHelper.open();
			lastUpdateTime = new Time();
			lastUpdateTime.set(mDbHelper.getTime());
		}
		catch(SQLException ex) {
			Log.e(TAG, ex.getMessage());
		}
	}
	
	/**
	 * Closes the database.
	 */
	@Override
	public void close() {
		mDbHelper.close();
	}
	/**
	 * Updates the time store in the database to indicate the current time.
	 * Should be used when a sync is completed.
	 */
	public void setTimeToNow() {
		lastUpdateTime.setToNow();
		mDbHelper.setTime(lastUpdateTime.toMillis(false));
	}
	
	public void firstTimeSync() {
		throw new UnsupportedOperationException();
		//setTimeToNow(); // when sync is done, update last updated time
	}
	
	public void updateSync() {
		throw new UnsupportedOperationException();
		//setTimeToNow(); // when sync is done, update last updated time
	}
	
	/**
	 * Syncs to server.  Calls appropriate sync method.
	 */
	public void sync() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Fetches all clues in the database.
	 * @return A cursor over all clues in the database.
	 */
	public Cursor fetchAllClues() {
		return mDbHelper.fetchAllClues();
	}
	
	/**
	 * Fetches a given clue by database row id.
	 * @param rowId id of clue to retrieve
	 * @return A cursor positioned at the clue, if found
	 * @throws SQLException if clue could not be found/retrieved
	 */
	public Cursor fetchClue(long rowId) throws SQLException {
		return mDbHelper.fetchClue(rowId);
	}
	
	/**
	 * Returns a Cursor of all clues whose Clue ID begins with the given String
	 * @param clueId Clue ID to pattern match clues to retrieve
	 * @return A Cursor over all matching clues
	 * @throws SQLException if clue could not be found/retrieved
	 */
	public Cursor filterClues(String clueId) throws SQLException {
		return mDbHelper.filterClues(clueId);
	}
	
	

}
