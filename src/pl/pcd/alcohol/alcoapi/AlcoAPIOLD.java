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

package pl.pcd.alcohol.alcoapi;


import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import pl.pcd.alcohol.Encryption;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;

public class AlcoAPIOLD {

    /**
     * @param JSON        json as a string to post to a WebService
     * @param connTimeout connectionTimeout. Connection Establishing time
     * @param soTimeout   SocketTimeout. Overall connetion time
     * @return null if there was an error connecting server
     */
    @SuppressWarnings("DuplicateThrows")
    @Nullable
    public static InputStream postApiRequest(@NotNull JSONObject JSON, String action, int connTimeout, int soTimeout) throws IOException, SocketTimeoutException, JSONException {
        String url = APICfg.API_URL;
        InputStream inputStream = null;
        String result = "";
        try {
            JSON.put("api_token", Encryption.encodeBase64(Encryption.encodeBase64(ApiToken.TOKEN)));

            final HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, connTimeout);
            HttpConnectionParams.setSoTimeout(httpParams, soTimeout);
            HttpClient httpclient = new DefaultHttpClient(httpParams);

            HttpPost httpPost = new HttpPost(url);

            httpPost.setEntity(new StringEntity(JSON.toString()));

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json; charset=utf-8 ");

            HttpResponse httpResponse = httpclient.execute(httpPost);
            inputStream = httpResponse.getEntity().getContent();

        } catch (SocketTimeoutException e) {
            Log.d("InputStream", e.toString());
            throw e;
        } catch (IOException e) {
            Log.d("InputStream", e.toString());
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return inputStream;
    }

}
