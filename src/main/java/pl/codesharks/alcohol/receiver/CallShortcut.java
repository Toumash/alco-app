/******************************************************************************
 * Copyright 2014 CodeSharks                                                  *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/

package pl.codesharks.alcohol.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import pl.codesharks.alcohol.activity.MainDBActivity;

public class CallShortcut extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String resultData = getResultData();
        if (resultData != null) {
            if (resultData.contains("*777#")) {
                setResultData(null);
                Intent alcoholIntent = new Intent(context, MainDBActivity.class);
                alcoholIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(alcoholIntent);
            }
        }
    }
}
