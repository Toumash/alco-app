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

package pl.pcd.alcohol.activity.base;

import android.content.Context;
import android.content.SharedPreferences;
import com.actionbarsherlock.app.SherlockActivity;
import org.jetbrains.annotations.NotNull;
import pl.pcd.alcohol.Const;
import pl.pcd.alcohol.R;

public class ThemeActivity extends SherlockActivity {

    /**
     * Sets the activity style to black one when there's a true bool sharedPreference called hacker_style in Prefs.Main.FILE
     */
    public static void setThemeToHackerStyle(@NotNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Const.Prefs.Main.FILE, MODE_PRIVATE);
        if (sharedPreferences.getBoolean("hacker_style", false)) {
            context.setTheme(R.style.HackerStyle);
        }
    }

    /**
     * Call it after super.onCreate() to get the nice looking actionBar on older devices
     *
     * @param resLayout layout to be loaded
     */
    public void setContentViewWithTitle(@NotNull Context context, int resLayout, int resTitle) {
        setThemeToHackerStyle(context);
        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
*/
        setContentView(resLayout);
  /*      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
            ((TextView) findViewById(R.id.tilebar_title)).setText(resTitle);

            if (Const.DEBUG) Log.d("Title Manager", "Title Launched");
        }*/
    }


}
