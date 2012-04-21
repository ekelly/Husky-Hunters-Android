package net.erickelly.huskyhunters.data;

public final class Constants {
	
	// Define database name
	public static final String DB_NAME = "cluedata";
	public static final Integer DB_VERSION = 1;
	
	// Define table names
	public static final String CLUE_TABLE = "clueTable";
	public static final String TIME_TABLE = "timeTable";
	public static final String PHOTO_TABLE = "photoTable";
	
	// Define column names for the SQLite Clue DB
	public static final String KEY_CLUEID = "clueid"; // String
	public static final String KEY_TEXT = "cluetext";
	public static final String KEY_ANS = "ans";
	public static final String KEY_SOLVED = "solved"; // String
	public static final String KEY_POINTS = "points";
	public static final String KEY_LOCATION_X = "locationX";
	public static final String KEY_LOCATION_Y = "locationY";
	public static final String KEY_UPLOADED = "uploaded"; //boolean
	public static final String KEY_ROWID = "_id";
	
	// Define columns for the SQLite Photo DB
	public static final String KEY_PHOTO_PATH = "photo_path";
	
	// Define column names for the SQLite Time DB
	public static final String TIME_TIME = "time";
	
}
