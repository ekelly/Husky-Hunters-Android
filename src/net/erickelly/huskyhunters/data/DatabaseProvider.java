package net.erickelly.huskyhunters.data;

import java.io.Closeable;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

/**
 * Simple clue database helper class.  Defines CRUD options for the database, 
 * and gives ability to list all clues, list a specific clue, or filter 
 * for a given clueId expression.
 * Also includes a time table in which last update time is stored, 
 * and getTime() and setTime() methods for updating.  Time is automatically 
 * set to zero when the database is first created.
 * @author francis
 */
public class DatabaseProvider implements Closeable {
	// Debugging TAG
	public static final String TAG = "ClueDbHelper";

	private static final String DB_NAME = "cluedata";
	private static final String CLUE_TABLE = "clueTable";
	private static final String TIME_TABLE = "timeTable";
	private static final String PHOTO_TABLE = "photoTable";
	private static final Integer DB_VERSION = 1;

	// photo path is a URI, but saved as a String
	// use Uri.parse(s) and myUri.toString()
	
	// time is saved in UTC milliseconds time format (long)
	// time value of 0 indicates database has never been synced

	// Database creation command
	/*private static final String DB_CREATE = 
			"create table clues (_id integer primary key autoincrement, " + 
			"num text not null, cluetext text not null, ans text not null, " +
			"locationX real not null, locationY real not null, " +
			"solved string not null, points integer not null, " + 
			"photo_path text, uploaded integer not null);";*/

	private static final String CLUEDB_CREATE = makeClueDatabaseCreator();
	private static final String TIMEDB_CREATE = makeTimeDatabaseCreator();
	private static final String PHOTODB_CREATE = makePhotoDatabaseCreator();
	private static final String SET_TIME_ZERO = 
			"INSERT INTO " + TIME_TABLE + " VALUES(0);";

	// CONTENT PROVIDER FINALS
	private static final String AUTHORITY = "net.erickelly.huskyhunters";
	private static final String CLUES_BASE_PATH = "tutorials";
	private static final int CLUES = 1;
	private static final int CLUE_ID = 2;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + CLUES_BASE_PATH);
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
	        + "/clue";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
	        + "/clue";
	
	private static final UriMatcher sURIMatcher = new UriMatcher(
	        UriMatcher.NO_MATCH);
	static {
	    sURIMatcher.addURI(AUTHORITY, CLUES_BASE_PATH, CLUES);
	    sURIMatcher.addURI(AUTHORITY, CLUES_BASE_PATH + "/#", CLUE_ID);
	}

	private static final String makeClueDatabaseCreator() {
		StringBuilder sb = new StringBuilder();
		sb.append("create table ");
		sb.append(CLUE_TABLE);
		sb.append(" (");
		sb.append(Constants.KEY_ROWID);
		sb.append(" integer primary key autoincrement, ");
		sb.append(Constants.KEY_CLUEID);
		sb.append(" text not null, ");
		sb.append(Constants.KEY_ANS);
		sb.append(" text not null, ");
		sb.append(Constants.KEY_TEXT);
		sb.append(" text not null, ");
		sb.append(Constants.KEY_POINTS);
		sb.append(" integer not null, ");
		sb.append(Constants.KEY_LOCATION_X);
		sb.append(" integer not null, ");
		sb.append(Constants.KEY_LOCATION_Y);
		sb.append(" integer not null, ");
		sb.append(Constants.KEY_SOLVED);
		sb.append(" text not null, ");
		sb.append(Constants.KEY_PHOTO_PATH);
		sb.append(" text, ");
		sb.append(Constants.KEY_UPLOADED);
		sb.append(" integer not null);");
		return sb.toString();
	}
	
	private static final String makeTimeDatabaseCreator() {
		StringBuilder sb = new StringBuilder("create table ");
		sb.append(TIME_TABLE);
		sb.append(" (");
		sb.append(Constants.TIME_TIME);
		sb.append(" integer primary key);");
		return sb.toString();
	}
	
	private static final String makePhotoDatabaseCreator() {
		StringBuilder sb = new StringBuilder("create table ");
		sb.append(PHOTO_TABLE);
		sb.append(" (");
		sb.append(Constants.KEY_CLUEID);
		sb.append(" text primary key not null, ");
		sb.append(Constants.KEY_PHOTO_PATH);
		sb.append(" text);");
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
			db.execSQL(PHOTODB_CREATE);
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
			db.execSQL("DROP TABLE IF EXISTS " + PHOTO_TABLE);
			onCreate(db);
		}

	}

	/**
	 * Constructor - takes a context from which the database is opened/created.
	 * @param ctx The context within which to work.
	 */
	public DatabaseProvider(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the clues database.  If it cannot be opened, try to create a 
	 * new instance of the database.  If that fails, throws an SQLException.
	 * @return this (self reference), to allow for chaining in initialization calls.
	 * @throws SQLException if the database could not be opened or created.
	 */
	public DatabaseProvider open() throws SQLException {
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
			Integer points, Double[] location, String solved,
			List<String> photos, Boolean uploaded) {
		ContentValues cv = new ContentValues();
		cv.put(Constants.KEY_CLUEID, clueId);
		cv.put(Constants.KEY_ANS, answer);
		cv.put(Constants.KEY_TEXT, originalClue);
		cv.put(Constants.KEY_POINTS, points);
		cv.put(Constants.KEY_LOCATION_X, location[0]);
		cv.put(Constants.KEY_LOCATION_Y, location[1]);
		cv.put(Constants.KEY_SOLVED, solved);
		cv.put(Constants.KEY_UPLOADED, uploaded);
		
		ContentValues pics = new ContentValues();
		for(String photo : photos) {
			pics.put(clueId, photo);
		}

		mDb.insert(PHOTO_TABLE, null, pics);
		return mDb.insert(CLUE_TABLE, null, cv);
	}
	
	public long insertClue(Clue clue) {
		String clueId = clue.clueNum();
		String answer = clue.answer();
		String originalClue = clue.clue();
		Integer points = clue.points();
		Double[] latlng = clue.latlng();
		String solved = clue.solved();
		List<String> photos = clue.photo();
		Boolean uploaded = false;
		return this.insertClue(clueId, answer, originalClue, points, 
				latlng, solved, photos, uploaded);
	}

	/**
	 * Deletes a clue from the database based on row ID.
	 * @param rowID the Row ID of the clue to be deleted.
	 * @return true if successful, false otherwise
	 */
	public boolean deleteClue(long rowID) {
		return mDb.delete(CLUE_TABLE, Constants.KEY_ROWID + "=" + rowID, null) > 0;
	}

	/**
	 * Deletes a clue from the database based on clue ID.
	 * @param clueId the clue ID of the clue to be deleted.
	 * @return true if successful, false otherwise
	 */
	public boolean deleteClue(String clueId) {
		return mDb.delete(CLUE_TABLE, Constants.KEY_CLUEID + "=" + clueId, null) > 0;
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
	 * @param location the location of the clue (Double[])
	 * @param solved whether the clue is solved or not
	 * @param photo_path the path to the photo as a serialized URI (may be null)
	 * @param uploaded a boolean indicating whether the photo has been uploaded
	 * @return true if the clue was successfully updated, false otherwise
	 */
	public boolean updateClue(String clueId, String answer, String originalClue,
			Integer points, Double[] location, String solved,
			String photo_path, Boolean uploaded) {
		ContentValues cv = new ContentValues();
		cv.put(Constants.KEY_ANS, answer);
		cv.put(Constants.KEY_TEXT, originalClue);
		cv.put(Constants.KEY_POINTS, points);
		cv.put(Constants.KEY_LOCATION_X, location[0]);
		cv.put(Constants.KEY_LOCATION_Y, location[1]);
		cv.put(Constants.KEY_SOLVED, solved);
		cv.put(Constants.KEY_PHOTO_PATH, photo_path);
		cv.put(Constants.KEY_UPLOADED, uploaded);

		return mDb.update(CLUE_TABLE, cv, Constants.KEY_CLUEID + "=" + clueId, null) > 0;
	}

	/**
	 * Fetches all clues in the database.
	 * @return a cursor over all clues in the database.
	 */
	public Cursor fetchAllClues() {
		return mDb.query(CLUE_TABLE,
				new String[] { Constants.KEY_ROWID, 
					Constants.KEY_CLUEID, Constants.KEY_ANS, Constants.KEY_TEXT,
					Constants.KEY_POINTS, Constants.KEY_LOCATION_X, 
					Constants.KEY_LOCATION_Y, Constants.KEY_SOLVED, 
					Constants.KEY_PHOTO_PATH, Constants.KEY_UPLOADED },
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
				new String[] { Constants.KEY_ROWID, 
					Constants.KEY_CLUEID, Constants.KEY_ANS, Constants.KEY_TEXT,
					Constants.KEY_POINTS, Constants.KEY_LOCATION_X, 
					Constants.KEY_LOCATION_Y, Constants.KEY_SOLVED, 
					Constants.KEY_PHOTO_PATH, Constants.KEY_UPLOADED },
				Constants.KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null)
			mCursor.moveToFirst();
		return mCursor;
	}
	
	/**
	 * Returns a Cursor of all clues that match the given ClueID string in the given table.
	 * @param clueId id to pattern match clues to retrieve
	 * @param table table to filter from
	 * @return Cursor of all clues that match the given expression
	 * @throws SQLException if clue could not be found/retrieved
	 */
	public Cursor filterClues(String clueId) throws SQLException {
		Cursor mCursor = mDb.query(true, CLUE_TABLE,
				new String[] { Constants.KEY_ROWID, 
					Constants.KEY_CLUEID, Constants.KEY_ANS, Constants.KEY_TEXT,
					Constants.KEY_POINTS, Constants.KEY_LOCATION_X, 
					Constants.KEY_LOCATION_Y, Constants.KEY_SOLVED, 
					Constants.KEY_PHOTO_PATH, Constants.KEY_UPLOADED },
				Constants.KEY_CLUEID + " LIKE \"" + clueId + "%\"", null, null, null, null, null);
		return mCursor;
	}
	
	/**
	 * Returns Cursor of all photos associated with the given ClueID string
	 * @param String clueId
	 * @return Cursor of all clues that match the given expression
	 * @throws SQLException if clue could not be found/retrieved
	 */
	public Cursor fetchCluePhotos(String clueId) throws SQLException {
		Cursor mCursor = mDb.query(true, PHOTO_TABLE,
				new String[] { Constants.KEY_CLUEID, Constants.KEY_PHOTO_PATH },
				Constants.KEY_CLUEID + " LIKE \"" + clueId + "%\"", null, null, null, null, null);
		return mCursor;
	}
	
	public void setTime(long t) {
		ContentValues cv = new ContentValues();
		cv.put(Constants.TIME_TIME, t);
		mDb.update(TIME_TABLE, cv, null, null);
	}
	
	public long getTime() {
		Cursor c = mDb.rawQuery("SELECT * FROM " + TIME_TABLE, null);
		if(c.getCount() < 1) {
			return -1;
		}
		c.moveToFirst();
		long t = c.getLong(c.getColumnIndex(Constants.TIME_TIME));
		c.close();
		return t;
	}

	/*
	// CONTENT PROVIDER FUNCTIONS
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
	    queryBuilder.setTables(CLUE_TABLE);
	 
	    int uriType = sURIMatcher.match(uri);
	    switch (uriType) {
	    case CLUE_ID:
	        queryBuilder.appendWhere(Constants.KEY_CLUEID + "="
	                + uri.getLastPathSegment());
	        break;
	    case CLUES:
	        // no filter
	        break;
	    default:
	        throw new IllegalArgumentException("Unknown URI");
	    }
	 
	    Cursor cursor = queryBuilder.query(mDbHelper.getReadableDatabase(),
	            projection, selection, selectionArgs, null, null, sortOrder);
	    cursor.setNotificationUri(getContext().getContentResolver(), uri);
	    return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	*/
}
