package pl.pcd.alcohol.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.pcd.alcohol.*;
import pl.pcd.alcohol.ui.base.ThemeActivity;
import pl.pcd.alcohol.webapi.RatingsDownloader;

import java.util.ArrayList;

public class MoreRatingsActivity extends ThemeActivity {
    long alcoholID;
    @NotNull
    String TAG = "MoreRatingsActivity";
    ArrayList<Rating> ratingList;
    @NotNull
    Context context = this;
    LinearLayout linear_for_info;
    ListView lv_ratings;

    @Override
    protected void onCreate(Bundle x) {
        super.onCreate(x);
        setContentViewWithTitle(context, R.layout.activ_more_ratings, R.string.more_ratings_title);
        linear_for_info = (LinearLayout) findViewById(R.id.more_ratings_linear_for_info);
        lv_ratings = (ListView) findViewById(R.id.more_ratings_lv_ratings);
        alcoholID = getIntent().getExtras().getLong("id");
        handleFetchingComments(alcoholID);
    }

    @SuppressWarnings("ResourceType")
    private void handleFetchingComments(long alcoholID) {
        if (Utils.isConnected(context)) {
            RatingsDownloader downloader = new RatingsDownloader(alcoholID, 300) {
                @Nullable
                ProgressBar progressBar;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    this.progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyle);
                    this.progressBar.setIndeterminate(true);
                    if (findViewById(111) != null)
                        linear_for_info.removeView(findViewById(111));
                    this.progressBar.setId(111);
                    linear_for_info.addView(this.progressBar, 0);
                }

                @Override
                protected void onPostExecute(Void x) {
                    super.onPostExecute(x);
                    if (this.result.equals(Const.API.LoginResult.OK)) {
                        linear_for_info.removeAllViews();
                        lv_ratings.setAdapter(new RatingsAdapter(context, ratingList));
                    } else if (this.result.equals("timeout")) {
                        TextView error = new TextView(context);
                        error.setText(R.string.network_error);
                        error.setGravity(Gravity.CENTER_HORIZONTAL);
                        error.setId(111);
                        if (findViewById(111) != null)
                            linear_for_info.removeView(findViewById(111));
                        linear_for_info.addView(error, 0);
                    } else if (this.result.equals("no_comments")) {
                        TextView error = new TextView(context);
                        error.setText(R.string.alcoholinfo_no_ratings);
                        error.setGravity(Gravity.CENTER_HORIZONTAL);
                        error.setId(111);
                        if (findViewById(111) != null)
                            linear_for_info.removeView(findViewById(111));
                        linear_for_info.addView(error, 0);
                    }
/*                        else lv_comments.setAdapter(new ArrayAdapter<String>(context, -1,  ArrayList<String> x = new ArrayList<String>().add(getString(R.string.alcoholinfo_no_comments))));*/
                }
            };
            downloader.execute();
        } else {
            if (findViewById(111) != null)
                linear_for_info.removeView(findViewById(111));
            TextView tv = new TextView(context);
            tv.setText(R.string.no_internet);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setId(111);
            linear_for_info.addView(tv, 0);
            // Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
            /*
            Comment x = new Comment();
            x.author = "Toumash";
            x.date = "asfasafd";
            x.content = "do dupy wgl to piwo";
            ArrayList<Comment> arrayList = new ArrayList<Comment>();
            arrayList.add(x);
            */
            lv_ratings.setAdapter(new RatingsAdapter(context, ratingList));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //getMenuInflater().inflate(R.menu.alcohol_info, menu);
        getSupportMenuInflater().inflate(R.menu.alcohol_ratings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_more_ratings_refresh:
                handleFetchingComments(this.alcoholID);
                break;
        }
        return true;
    }

    class RatingsAdapter extends ArrayAdapter<Rating> {
        int i = 100;
        private ArrayList<Rating> items;

        public RatingsAdapter(@NotNull Context context, ArrayList<Rating> items) {
            super(context, R.layout.rating_item, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder holder;

            if (v == null) {
                LayoutInflater vi =
                        (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.rating_item, null);
                // cache view fields into the holder
                holder = new ViewHolder();
                holder.author = (TextView) v.findViewById(R.id.comment_author);
                holder.content = (TextView) v.findViewById(R.id.comment_content);
                holder.date = (TextView) v.findViewById(R.id.comment_date);
                holder.rating = (RatingBar) v.findViewById(R.id.rating_rating);

                // associate the holder with the view for later lookup
                v.setTag(holder);
            } else {
                // view already exists, get the holder instance from the view
                holder = (ViewHolder) v.getTag();
            }

            Rating rating = items.get(position);
            holder.author.setText(rating.author);
            holder.content.setText(rating.content);
            holder.date.setText(rating.date);
            holder.rating.setRating(rating.rating);
            i++;
            holder.localID = i;


            return v;
            // no local variables with findViewById here

            // use holder.nameText where you were
            // using the local variable nameText before
        }
    }

    protected class ViewHolder {
        TextView author;
        TextView date;
        TextView content;
        RatingBar rating;
        int localID;
    }
}