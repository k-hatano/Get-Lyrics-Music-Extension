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

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
                        MediaStore.Audio.Media.ALBUM
                }, null, null, null);

        if (trackCursor != null) {
            try {
                if (trackCursor.moveToFirst()) {

                    // And retrieve the wanted information
                    String trackName = trackCursor.getString(trackCursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String albumName = trackCursor.getString(trackCursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                    String artistName = trackCursor.getString(trackCursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));

                    ((TextView)findViewById(R.id.track)).setText(trackName);
                    ((TextView)findViewById(R.id.album)).setText(albumName);
                    ((TextView)findViewById(R.id.artist)).setText(artistName);

                }
            } finally {
                trackCursor.close();
            }
        }

    }

}
