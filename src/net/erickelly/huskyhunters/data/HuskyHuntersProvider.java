package net.erickelly.huskyhunters.data;

import java.net.URI;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.ContentProvider;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import net.erickelly.huskyhunters.data.DatabaseWrapper;

public class HuskyHuntersProvider extends ContentProvider {

	// Constants
	public static final String AUTHORITY = "net.erickelly.huskyhunters.provider";
	private static final int URI_CLUES = 1;
	private static final int URI_CLUE_ID = 2;
	private static final int URI_CLUE_PHOTOS = 3;
	private static final int URI_TIME = 4;
	
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sUriMatcher.addURI(AUTHORITY, "clues", URI_CLUES);
		sUriMatcher.addURI(AUTHORITY, "clues/*", URI_CLUE_ID);
		sUriMatcher.addURI(AUTHORITY, "clues/#/photos", URI_CLUE_PHOTOS);
		sUriMatcher.addURI(AUTHORITY, "time", URI_TIME);
	}
	
	private DatabaseWrapper db;
	
	@Override
	public boolean onCreate() {
		db = new DatabaseWrapper(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch(sUriMatcher.match(uri)) {
			case URI_CLUES:
				return "vnd.android.cursor.dir/vnd.erickelly.huskyhunters.provider." + Constants.CLUE_TABLE;
			case URI_CLUE_ID:
				return "vnd.android.cursor.item/vnd.erickelly.huskyhunters.provider." + Constants.CLUE_TABLE;
			case URI_CLUE_PHOTOS:
				return "vnd.android.cursor.dir/vnd.erickelly.huskyhunters.provider." + Constants.PHOTO_TABLE;
			default:
				return "vnd.android.cursor.dir/vnd.erickelly.huskyhunters.provider." + Constants.TIME_TABLE;
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		Cursor c;
		
		switch(sUriMatcher.match(uri)) {
			case URI_CLUES:
				c = db.fetchAllClues();
				break;
			case URI_CLUE_ID:
				c = db.fetchClue(uri.getLastPathSegment());
				break;
			case URI_CLUE_PHOTOS:
				String clueid = uri.getPathSegments().get(1);
				c = db.fetchCluePhotos(clueid);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		
		return c;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if(sUriMatcher.match(uri) != URI_CLUES) { throw new IllegalArgumentException("Unknown URI " + uri); }
		
		ContentValues newValues;
		if(values != null) {
			newValues = new ContentValues(values);
		} else {
			newValues = new ContentValues();
		}
		
		long rowId = db.insertClue(newValues);
		if(rowId > 0) {
			Uri clueUri = ContentUris.withAppendedId(Uri.parse("content://" + AUTHORITY + "/clues"), rowId);
			getContext().getContentResolver().notifyChange(clueUri, null);
			return clueUri;
		}
		
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		boolean result;
		switch(sUriMatcher.match(uri)) {
			case URI_CLUE_ID:
				result = db.updateClue(uri.getLastPathSegment(), values);
				break;
			case URI_CLUE_PHOTOS:
				throw new UnsupportedOperationException();
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		if(result) { return 1; } else { return 0; }
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Boolean result;
		switch(sUriMatcher.match(uri)) {
			case URI_CLUE_ID:
				result = db.deleteClue(uri.getLastPathSegment());
				break;
			default:
				result = false;
				break;
		}
		
		if(result == false) { 
			return 0; 
		} else { 
			return 1; 
		}
	}

}
