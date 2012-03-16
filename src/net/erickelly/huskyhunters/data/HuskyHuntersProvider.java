package net.erickelly.huskyhunters.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContentProvider;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import net.erickelly.huskyhunters.data.DatabaseProvider;

public class HuskyHuntersProvider extends ContentProvider {

	public static final String AUTHORITY = "net.erickelly.huskyhunters.provider";
	
	private static final int URI_CLUES = 1;
	private static final int URI_CLUE_ID = 2;
	private static final int URI_CLUE_PHOTOS = 3;
	
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sUriMatcher.addURI(AUTHORITY, "clues", URI_CLUES);
		sUriMatcher.addURI(AUTHORITY, "clues/*", URI_CLUE_ID);
		sUriMatcher.addURI(AUTHORITY, "clues/#/photos", URI_CLUE_PHOTOS);
	}
	
	private final DatabaseProvider db = new DatabaseProvider(getContext());
	
	@Override
	public boolean onCreate() {
		return false;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		switch(sUriMatcher.match(uri)) {
			case URI_CLUES:
				db.fetchAllClues();
				break;
			case URI_CLUE_ID:
				db.fetchClue(uri.getLastPathSegment());
				break;
			case URI_CLUE_PHOTOS:
				String clueid = uri.getPathSegments().get(1);
				db.fetchCluePhotos(clueid);
				break;
			default:
				break;
		}
		
		return null;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
