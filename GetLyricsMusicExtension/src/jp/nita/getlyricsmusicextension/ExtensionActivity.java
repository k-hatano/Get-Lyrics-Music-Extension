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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ExtensionActivity extends Activity implements OnClickListener {
	
	private static final int PICKUP_LAUNCHING_BROWSER = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		findViewById(R.id.songInfo).setOnClickListener(this);
		findViewById(R.id.poweredBy).setOnClickListener(this);

		findViewById(R.id.reSearch).setOnClickListener(this);
		findViewById(R.id.reSearchWithKeywords).setOnClickListener(this);

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
					ImageView iv = (ImageView)findViewById(R.id.albumArtwork);
					iv.setImageBitmap(bm);

					// And retrieve the wanted information

					String trackName = trackCursor.getString(trackCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
					String artistName = trackCursor.getString(trackCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));

					((TextView)findViewById(R.id.track)).setText(trackName);
					((TextView)findViewById(R.id.artist)).setText(artistName);

					String params[]={trackName,artistName};

					AsyncLyricsSearcher searcher = new AsyncLyricsSearcher(this);
					searcher.execute(params);
				}
			} finally {
				trackCursor.close();
			}
		}
	}

	@Override
	public void onClick(View v) {
		if(v==findViewById(R.id.songInfo)){
			if(AsyncLyricsSearcher.lastResult.size()<=0){
				showResearchWithKeywordsDialog();
			}else{
				List<String> titleList = new ArrayList<String>();
				for(int i=0;i<AsyncLyricsSearcher.lastResult.size();i++){
					titleList.add(AsyncLyricsSearcher.lastResult.get(i).title);
				}
				titleList.add(getString(R.string.re_search_with_keywords));

				new AlertDialog.Builder(ExtensionActivity.this)
				.setTitle(getString(R.string.get_other))
				.setItems(titleList.toArray(new String[1]),new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if(arg1>=AsyncLyricsSearcher.lastResult.size()){
							showResearchWithKeywordsDialog();
						}else{
							AsyncLyricsGetter getter=new AsyncLyricsGetter(ExtensionActivity.this,new Handler());
							getter.execute(AsyncLyricsSearcher.lastResult.get(arg1).title,AsyncLyricsSearcher.lastResult.get(arg1).anchor);
						}
					}
				}).show();
			}
		}
		if(v==findViewById(R.id.reSearch)){
			String params[]={((TextView)findViewById(R.id.track)).getText().toString(),
					((TextView)findViewById(R.id.artist)).getText().toString()};
			AsyncLyricsSearcher searcher = new AsyncLyricsSearcher(this);
			searcher.execute(params);
		}
		if(v==findViewById(R.id.reSearchWithKeywords)){
			showResearchWithKeywordsDialog();
		}
		if(v==findViewById(R.id.poweredBy)){
			Uri trackUri = Uri.parse(AsyncLyricsGetter.getLastUriString());
			Intent intent = new Intent(Intent.ACTION_VIEW,trackUri);
			startActivityForResult(intent,PICKUP_LAUNCHING_BROWSER);
		}
	}

	public boolean showResearchWithKeywordsDialog(){
		AsyncLyricsSearcher.clearCache();
		AsyncLyricsGetter.clearCache();
		final EditText title=new EditText(this);
		final EditText artist=new EditText(this);
		title.setText(((TextView)findViewById(R.id.track)).getText());
		artist.setText(((TextView)findViewById(R.id.artist)).getText());
		final LinearLayout layout=new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(title);
		layout.addView(artist);
		new AlertDialog.Builder(ExtensionActivity.this)
		.setTitle(getString(R.string.re_search_with_keywords))
		.setView(layout)
		.setMessage(getString(R.string.input_title_and_artist))
		.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AsyncLyricsSearcher getter=new AsyncLyricsSearcher(ExtensionActivity.this);
				getter.execute(title.getText().toString(),artist.getText().toString());
			}
		})
		.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		}).show();
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode,resultCode,data);
		
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.KEYCODE_CAMERA){
			if(e.getAction()==KeyEvent.ACTION_UP){
				KeyEventSender sender = new KeyEventSender();
				sender.execute(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
				return true;
			}else{
				return true;
			}
		}

		return super.dispatchKeyEvent(e);
	}
	
	private class KeyEventSender extends AsyncTask<Integer, Object, Object> {
		@Override
		protected Object doInBackground(Integer... params) {
			int keycode = (Integer)(params[0]);
			Instrumentation ist = new Instrumentation();
			ist.sendKeyDownUpSync(keycode);
			return null;
		}
	}

}
