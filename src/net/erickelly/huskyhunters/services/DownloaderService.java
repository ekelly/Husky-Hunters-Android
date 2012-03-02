package net.erickelly.huskyhunters.services;

import net.erickelly.huskyhunters.data.CluesData;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

public class DownloaderService extends Service {
	
	//private static final String TAG = "DownloaderService";
	
	CluesData clues;

	class DownloadCluesTask extends AsyncTask<String, Integer, CluesData>{

		@Override
		protected CluesData doInBackground(String... params) {
			String groupHash = params[0];
			clues.sync(groupHash);
			return clues;
		}
		
		@Override
		protected void onPostExecute(CluesData result) {
			clues = result;
			// Update the list
			//setListAdapter(clues.getAdapter());
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle extras = intent.getExtras();
		String GROUP_HASH = extras.getString("grouphash");
		new DownloadCluesTask().execute(GROUP_HASH);
        return Service.START_FLAG_REDELIVERY;
    }

}
