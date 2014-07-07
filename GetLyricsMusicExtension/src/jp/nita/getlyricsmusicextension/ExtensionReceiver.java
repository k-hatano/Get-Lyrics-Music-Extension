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

import jp.nita.getlyricsmusicextension.R;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class ExtensionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // This extra can be used to have many extension activities registered in
        // the same apk
        if (intent.getStringExtra("com.sonyericsson.media.infinite.EXTRA_ACTIVITY_NAME").equals(
        		ExtensionActivity.class.getName())) {

            Bundle extras = new Bundle();

            // Build a URI for the string resource for the description text
            String description = new Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .authority(context.getPackageName())
                    .appendPath(Integer.toString(R.string.description)).build().toString();

            // And return it to the infinite framework as an extra
            extras.putString("com.sonyericsson.media.infinite.EXTRA_DESCRIPTION", description);
            setResultExtras(extras);

        }

    }

}
