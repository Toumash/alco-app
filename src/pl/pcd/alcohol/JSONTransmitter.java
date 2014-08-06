package pl.pcd.alcohol;

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pl.pcd.alcohol.webapi.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;

public class JSONTransmitter {
    /**
     * @return null if there was an error connecting server
     */
    @Nullable
    public static String postJSON(@NotNull String JSON, String url) {
        InputStream inputStream = null;
        String result = "";
        try {
            JSONObject obj = new JSONObject(JSON);
            obj.put("api_token", Config.TOKEN);
            JSON = obj.toString();
            final HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 1000 * 7);
            HttpConnectionParams.setSoTimeout(httpParams, 1000 * 10);
            HttpClient httpclient = new DefaultHttpClient(httpParams);

            HttpPost httpPost = new HttpPost(url);

            httpPost.setEntity(new ByteArrayEntity(new String(JSON.getBytes(), "utf-8").getBytes()));

            JSONArray postjson = new JSONArray();
            postjson.put(JSON);
            httpPost.setHeader("json", new String(JSON.getBytes(), "utf-8"));
            httpPost.getParams().setParameter("jsonpost", postjson);
            // 5. set json to StringEntity
            // StringEntity se = new StringEntity(JSON);

            // 6. set httpPost Entity
            //httpPost.setEntity(new UrlEncodedFormEntity(JSON.toString(), HTTP.UTF_8));

            // 7. Set some headers to inform server about the type of the content
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

    private static String convertInputStreamToString(@NotNull InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public static String downloadWebsite(String url) {
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
