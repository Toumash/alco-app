package pl.pcd.alcohol.webapi;

import android.os.AsyncTask;
import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pl.pcd.alcohol.*;
import pl.pcd.alcohol.webapi.contract.Rating;

import java.util.ArrayList;

public class RatingsDownloader extends AsyncTask<Void, Void, Void> {
    protected String result = "error";
    protected ArrayList<Rating> ratingList;
    protected long alcoholId = 0;
    protected int limit = 200;
    @NotNull
    protected String TAG = "RatingsDownloader";

    public RatingsDownloader(long alcoholId, int limit) {
        super();
        this.alcoholId = alcoholId;
        this.limit = limit;
    }

    @Nullable
    @Override
    protected Void doInBackground(Void... voids) {
        JSONObject json = new JSONObject();
        try {
            json.put("action", Const.API.Actions.FETCH_RATINGS);
            json.put("alcohol_id", this.alcoholId);
            json.put("count", this.limit);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String received = JSONTransmitter.postJSON(json.toString(), Const.API.URL_JSON);
        String receivedJSONtext = Utils.substringBetween(received, "<json>", "</json>");
        if (Cfg.DEBUG) Log.d(TAG, "response:" + receivedJSONtext);
        if (receivedJSONtext != null) {
            parseJSON(receivedJSONtext);
        } else if (received == null) {
            this.result = "timeout";
        }
        return null;
    }

    protected void parseJSON(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            this.result = jsonObject.getString("result");
            if (jsonObject.getString("result").equals(Const.API.LoginResult.OK)) {

                JSONArray comments = jsonObject.getJSONArray("data");
                ratingList = new ArrayList<Rating>();
                final int length = comments.length();

                for (int i = 0; i < length; i++) {
                    JSONObject obj = comments.getJSONObject(i);
                    ratingList.add(new Rating(obj.getString("a"), obj.getString("c"), obj.getString("d"), obj.getInt("r")));
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, e.toString());
        }
    }
}