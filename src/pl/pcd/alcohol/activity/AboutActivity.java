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

package pl.pcd.alcohol.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import org.json.JSONObject;
import pl.pcd.alcohol.Const;
import pl.pcd.alcohol.R;
import pl.pcd.alcohol.Utils;
import pl.pcd.alcohol.activity.base.ThemeActivity;
import pl.pcd.alcohol.alcoapi.APIFactory;
import pl.pcd.alcohol.alcoapi.AlcoAPI;
import pl.pcd.alcohol.alcoapi.AlcoAPIAdapter.TypedJsonString;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Tomasz on 2014-08-19.
 */
public class AboutActivity extends ThemeActivity {
    Button bt_license;
    ImageView iv_app;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        {
            final AlcoAPI api = APIFactory.getAlcoAPIClient();
            if (Utils.isConnected(context)) {
                final ProgressDialog pd = new ProgressDialog(context);
                pd.setIndeterminate(true);
                pd.setMessage("Waiting for server resonse...");
                pd.show();
                api.getAllAlcohols(new TypedJsonString("{\"x\":\"s\"}"), new Callback<JSONObject>() {
                    @Override
                    public void success(JSONObject string, Response response) {
                        pd.dismiss();
                        Log.d("xd", string.toString());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        pd.dismiss();
                        Log.d("xd", "ERROR!!!!!!!!" + retrofitError.toString());
                    }
                });
            } else {
                Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        }
        setContentView(R.layout.activ_about);
        bt_license = (Button) findViewById(R.id.about_bt_license);
        iv_app = (ImageView) findViewById(R.id.about_iv_app);
        bt_license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(Utils.readResource(context, R.raw.notice, false, "\n"));
                builder.setNeutralButton(R.string.apache_license, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                        builder1.setMessage(Utils.readResource(context, R.raw.license, false, "\n"));
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
