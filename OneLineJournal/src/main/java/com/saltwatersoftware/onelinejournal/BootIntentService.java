/*
 * Copyright (C) 2013 The Android Open Source Project
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
 */
package com.saltwatersoftware.onelinejournal;

        import android.app.IntentService;
        import android.content.Intent;


/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class BootIntentService extends IntentService {

    public BootIntentService() {
        super("BootIntentService");
    }

    public static final String TAG = "BootIntentService";

    private GcmController gcmc;

    @Override
    protected void onHandleIntent(Intent intent) {

        gcmc = (GcmController) getApplicationContext();
        gcmc.onBootRegIdTasks();
        //gcmc.registerInBackground();

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        BootReceiver.completeWakefulIntent(intent);
    }
}
