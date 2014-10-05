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

package pl.pcd.alcohol;

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
import pl.pcd.alcohol.alcoapi.ApiToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;

public class JSONTransmitter {
    /**
     * @param JSON        json as a string to post to a WebService
     * @param url         url to connect
     * @param connTimeout connectionTimeout. Connection Establishing time
     * @param soTimeout   SocketTimeout. Overall connetion time
     * @return null if there was an error connecting server
     */
    @Nullable
    public static String postJSON(@NotNull String JSON, String url, int connTimeout, int soTimeout)
    //TODO:Throwning Exceptions: SocketTimeoutException,IOException(More general)
    {
        InputStream inputStream;
        String result = "";
        try {
            JSONObject obj = new JSONObject(JSON);
            obj.put("api_token", Encryption.encodeBase64(Encryption.encodeBase64(ApiToken.TOKEN)));
            JSON = obj.toString();
            final HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, connTimeout);
            HttpConnectionParams.setSoTimeout(httpParams, soTimeout);
            HttpClient httpclient = new DefaultHttpClient(httpParams);

            HttpPost httpPost = new HttpPost(url);

            httpPost.setEntity(new StringEntity(JSON));

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json; charset=utf-8 ");

            HttpResponse httpResponse = httpclient.execute(httpPost);
            inputStream = httpResponse.getEntity().getContent();

            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "ERROR!";
        } catch (SocketTimeoutException e) {
            Log.d("InputStream", e.toString());
            return null;
        } catch (IOException e) {
            Log.d("InputStream", e.toString());
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String convertInputStreamToString(@NotNull InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    /**
     * @param url url to request GET method
     * @return full response, null if network error
     */
    public static String simpleGetRequest(String url) {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        try {

            HttpResponse execute = client.execute(httpGet);
            InputStream content = execute.getEntity().getContent();
            return convertInputStreamToString(content);
        } catch (IOException e) {
            Log.d("Network", e.toString());
        }
        return "";
    }

}
