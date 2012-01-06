package edu.neu.acm.huskyhunters;

import java.io.Closeable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple clue database helper class.  Defines CRUD options for the database, 
 * and gives ability to list all clues, list a specific clue, or filter 
 * for a given clueId expression.</br>
 * Also includes a time table in which last update time is stored, 
 * and getTime() and setTime() methods for updating.  Time is automatically 
 * set to zero when the database is first created.
 * @author francis
 *
 */
public class ClueDbAdapter implements Closeable {
	// Debugging TAG
	public static final String TAG = "ClueDbHelper";
	

	private static final String DB_NAME = "cluedata";
	private static final String CLUE_TABLE = "clueTable";
	private static final String TIME_TABLE = "timeTable";
	private static final Integer DB_VERSION = 1;

	// Define column names for the SQLite Clue DB
	public static final String KEY_CLUEID = "clueid";
	public static final String KEY_TEXT = "cluetext";
	public static final String KEY_ANS = "ans";
	public static final String KEY_SOLVED = "solved"; //boolean
	public static final String KEY_POINTS = "points";
	public static final String KEY_PHOTO_PATH = "photo_path";
	public static final String KEY_LOCATION = "location";
	public static final String KEY_UPLOADED = "uploaded"; //boolean
	public static final String KEY_ROWID = "_id";
	
	// Define column names for the SQLite Time DB
	public static final String TIME_TIME = "time";

	// photo path is a URI, but saved as a String
	// use Uri.parse(s) and myUri.toString()
	
	// time is saved in Unix time format (integer)
	// time value of 0 indicates database has never been synced

	// Database creation command
	/*private static final String DB_CREATE = 
			"create table clues (_id integer primary key autoincrement, " + 
			"num text not null, cluetext text not null, ans text not null, " +
			"solved integer not null, points integer not null, " + 
			"photo_path text not null, location text not null);";*/

	private static final String CLUEDB_CREATE = makeClueDatabaseCreator();
	private static final String TIMEDB_CREATE = makeTimeDatabaseCreator();
	private static final String SET_TIME_ZERO = 
			"INSERT INTO " + TIME_TABLE + "VALUES(0);";

	private static final String makeClueDatabaseCreator() {
		StringBuilder sb = new StringBuilder();
		sb.append("create table ");
		sb.append(CLUE_TABLE);
		sb.append(" (");
		sb.append(KEY_ROWID);
		sb.append(" integer primary key autoincrement, ");
		sb.append(KEY_CLUEID);
		sb.append(" text not null, ");
		sb.append(KEY_ANS);
		sb.append(" text not null, ");
		sb.append(KEY_TEXT);
		sb.append(" text not null, ");
		sb.append(KEY_POINTS);
		sb.append(" integer not null, ");
		sb.append(KEY_LOCATION);
		sb.append(" text not null, ");
		sb.append(KEY_SOLVED);
		sb.append(" integer not null, ");
		sb.append(KEY_PHOTO_PATH);
		sb.append(" text, ");
		sb.append(KEY_UPLOADED);
		sb.append(" integer not null);");
		return sb.toString();
	}
	
	private static final String makeTimeDatabaseCreator() {
		StringBuilder sb = new StringBuilder("create table ");
		sb.append(TIME_TABLE);
		sb.append(" (");
		sb.append(TIME_TIME);
		sb.append(" integer primary key);");
		return sb.toString();
	}

	private final Context mCtx;

	// Create references to a database and a database handler.
	private DbHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static class DbHelper extends SQLiteOpenHelper {

		/**
		 * Constructor: calls the constructor of SQLiteOpenHelper with
		 * name DB_NAME, CursorFactory null, and version DB_VERSION.
		 * 
		 * @param ctx The context with which to work.
		 */
		DbHelper(Context ctx) {
			super(ctx, DB_NAME, null, DB_VERSION);
		}


		/**
		 * Creates the database and the clues and time tables.
		 * 
		 * @param db the database to create.
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CLUEDB_CREATE);
			db.execSQL(TIMEDB_CREATE);
			db.execSQL(SET_TIME_ZERO);
		}

		/**
		 * Handles database upgrades.  When upgrading, deletes all clue data.
		 * 
		 * @param db the database that is being upgraded.
		 * @param oldVersion the old database version number.
		 * @param newVersion the new database version number.
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " 
					+ newVersion + ", which will destroy all data.");
			clear(db);
		}

		/**
		 * Re-initializes the database.
		 * @param db The database to be initialized.
		 */
		public void clear(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS " + CLUE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + TIME_TABLE);
			onCreate(db);
		}

	}

	/**
	 * Constructor - takes a context from which the database is opened/created.
	 * @param ctx The context within which to work.
	 */
	public ClueDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the clues database.  If it cannot be opened, try to create a 
	 * new instance of the database.  If that fails, throws an SQLException.
	 * @return this (self reference), to allow for chaining in initialization calls.
	 * @throws SQLException if the database could not be opened or created.
	 */
	public ClueDbAdapter open() throws SQLException {
		mDbHelper = new DbHelper(mCtx); // initialize the database helper
		mDb = mDbHelper.getWritableDatabase(); // initialize the database field
		return this;
	}
	/**
	 * Closes the database.
	 */
	public void close() {
		mDbHelper.close();
	}

	/**
	 * Inserts a clue into the database.  Returns the row ID if successful,
	 * or -1 if failed.
	 * @param clueId the identifier of the clue being added.
	 * @param answer the answer text of the clue.
	 * @param originalClue the original text of the clue.
	 * @param points the point value of the clue.
	 * @param location the location of the clue (?)
	 * @param solved whether the clue is solved or not
	 * @param photo_path the path to the photo as a serialized URI (may be null)
	 * @param uploaded a boolean indicating whether the photo has been uploaded
	 * @return the row ID of the new clue in the database, or -1 if there is an error.
	 */
	public long insertClue(String clueId, String answer, String originalClue,
			Integer points, String location, Boolean solved,
			String photo_path, Boolean uploaded) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_CLUEID, clueId);
		cv.put(KEY_ANS, answer);
		cv.put(KEY_TEXT, originalClue);
		cv.put(KEY_POINTS, points);
		cv.put(KEY_LOCATION, location);
		cv.put(KEY_SOLVED, solved);
		cv.put(KEY_PHOTO_PATH, photo_path);
		cv.put(KEY_UPLOADED, uploaded);

		return mDb.insert(CLUE_TABLE, null, cv);
	}

	/**
	 * Deletes a clue from the database based on row ID.
	 * @param rowID the Row ID of the clue to be deleted.
	 * @return true if successful, false otherwise
	 */
	public boolean deleteClue(long rowID) {
		return mDb.delete(CLUE_TABLE, KEY_ROWID + "=" + rowID, null) > 0;
	}

	/**
	 * Deletes a clue from the database based on clue ID.
	 * @param clueId the clue ID of the clue to be deleted.
	 * @return true if successful, false otherwise
	 */
	public boolean deleteClue(String clueId) {
		return mDb.delete(CLUE_TABLE, KEY_CLUEID + "=" + clueId, null) > 0;
	}

	/**
	 * Initializes the database of players.
	 */
	public void clear() {
		mDbHelper.clear(mDb);
	}

	/**
	 * Updates the clue database according to the information provided.
	 * @param clueId the identifier of the clue being updated.
	 * @param answer the answer text of the clue.
	 * @param originalClue the original text of the clue.
	 * @param points the point value of the clue.
	 * @param location the location of the clue (?)
	 * @param solved whether the clue is solved or not
	 * @param photo_path the path to the photo as a serialized URI (may be null)
	 * @param uploaded a boolean indicating whether the photo has been uploaded
	 * @return true if the clue was successfully updated, false otherwise
	 */
	public boolean updateClue(String clueId, String answer, String originalClue,
			Integer points, String location, Boolean solved,
			String photo_path, Boolean uploaded) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_ANS, answer);
		cv.put(KEY_TEXT, originalClue);
		cv.put(KEY_POINTS, points);
		cv.put(KEY_LOCATION, location);
		cv.put(KEY_SOLVED, solved);
		cv.put(KEY_PHOTO_PATH, photo_path);
		cv.put(KEY_UPLOADED, uploaded);

		return mDb.update(CLUE_TABLE, cv, KEY_CLUEID + "=" + clueId, null) > 0;
	}

	/**
	 * Fetches all clues in the database.
	 * @return a cursor over all clues in the database.
	 */
	public Cursor fetchAllClues() {
		return mDb.query(CLUE_TABLE,
				new String[] { KEY_ROWID, KEY_CLUEID, KEY_ANS, KEY_TEXT,
				KEY_POINTS, KEY_LOCATION, KEY_SOLVED, KEY_PHOTO_PATH, 
				KEY_UPLOADED },
				null, null, null, null, null);
	}

	/**
	 * Returns a Cursor positioned at the clue that matches the given rowId
	 * @param rowId id of clue to retrieve
	 * @return Cursor positioned at the matching clue, if found
	 * @throws SQLException if clue could not be found/retrieved
	 */
	public Cursor fetchClue(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, CLUE_TABLE,
				new String[] { KEY_ROWID, KEY_CLUEID, KEY_ANS, KEY_TEXT,
				KEY_POINTS, KEY_LOCATION, KEY_SOLVED, KEY_PHOTO_PATH, 
				KEY_UPLOADED },
				KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null)
			mCursor.moveToFirst();
		return mCursor;
	}

	/**
	 * Returns a Cursor of all clues that match the given ClueID string.
	 * @param clueId id to pattern match clues to retrieve
	 * @return Cursor of all clues that match the given expression
	 * @throws SQLException
	 */
	public Cursor fetchClue(String clueId) throws SQLException {
		Cursor mCursor = mDb.query(true, CLUE_TABLE,
				new String[] { KEY_ROWID, KEY_CLUEID, KEY_ANS, KEY_TEXT,
				KEY_POINTS, KEY_LOCATION, KEY_SOLVED, KEY_PHOTO_PATH, 
				KEY_UPLOADED },
				KEY_CLUEID + " LIKE " + clueId + "%", null, null, null, null, null);
		return mCursor;
	}
	
	public void setTime(Integer t) {
		ContentValues cv = new ContentValues();
		cv.put(TIME_TIME, t);
		mDb.update(TIME_TABLE, cv, null, null);
	}
	
	public Integer getTime() {
		Cursor c = mDb.rawQuery("SELECT * FROM " + TIME_TABLE, null);
		c.moveToFirst();
		int t = c.getInt(c.getColumnIndex(TIME_TIME));
		c.close();
		return t;
	}

}
