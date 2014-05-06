/*
 * Copyright (C) 2011 Sony Ericsson Mobile Communications AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package jp.nita.getlyricsmusicextension;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class ExtensionActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Intent intent = getIntent();

		// Retrieve the URI from the intent, this is a URI to a MediaStore audio
		// file
		Uri trackUri = intent.getData();

		// Use it to query the media provider
		Cursor trackCursor = getContentResolver().query(
				trackUri,
				new String[] {
						MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID
				}, null, null, null);

		if (trackCursor != null) {
			try {
				if (trackCursor.moveToFirst()) {

					Cursor albumCursor = getContentResolver().query(
							MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
							null, MediaStore.Audio.Albums._ID + "=?", 
							new String[]{ trackCursor.getString(trackCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)) }, null);

					Bitmap bm;
					if(albumCursor!=null && albumCursor.getCount()>0){
						albumCursor.moveToFirst();
						int albumArtIndex = albumCursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART);
						String albumArt = albumCursor.getString(albumArtIndex);

						bm = BitmapFactory.decodeFile( albumArt );
					}else{
						Resources r = getResources();
						bm = BitmapFactory.decodeResource(r, R.drawable.ic_launcher);
					}
					ImageView iv = (ImageView)findViewById(R.id.album_artwork);
					iv.setImageBitmap(bm);

					// And retrieve the wanted information

					String trackName = trackCursor.getString(trackCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
					String artistName = trackCursor.getString(trackCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
					String artistAndAlbumName = 
							trackCursor.getString(trackCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
							+" "+getString(R.string.separator)+" "+
							trackCursor.getString(trackCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));

					((TextView)findViewById(R.id.track)).setText(trackName);
					((TextView)findViewById(R.id.artist)).setText(artistAndAlbumName);
					
					String params[]={trackName,artistName};
					
					AsyncLyricsSearcher searcher = new AsyncLyricsSearcher(this);
					searcher.execute(params);
				}
			} finally {
				trackCursor.close();
			}
		}
	}

}
