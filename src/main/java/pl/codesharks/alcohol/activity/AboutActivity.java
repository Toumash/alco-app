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

package pl.codesharks.alcohol.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.loopj.android.http.AsyncHttpClient;
import pl.codesharks.alcohol.Const;
import pl.codesharks.alcohol.R;
import pl.codesharks.alcohol.Utils;
import pl.codesharks.alcohol.activity.base.ThemeActivity;
import pl.codesharks.alcohol.alcoapi.AlcoAPI;

/**
 * Created by Tomasz on 2014-08-19.
 */
public class AboutActivity extends ThemeActivity {
    Button bt_license;
    ImageView iv_app;
    Context context = this;
    AsyncHttpClient asyncHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.asyncHttpClient = AlcoAPI.getAsyncHttpClient(context);

        setContentView(R.layout.activ_about);
        bt_license = (Button) findViewById(R.id.about_bt_license);
        iv_app = (ImageView) findViewById(R.id.about_iv_app);
        bt_license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(Utils.readResource(context, R.raw.notice, "\n"));
                builder.setNeutralButton(R.string.apache_license, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                        builder1.setMessage(Utils.readResource(context, R.raw.license, "\n"));
                        builder1.show();
                    }
                });
                builder.show();
            }
        });
        iv_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = Const.API.URL_ABOUT;
                // USEFUL: if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }


}
