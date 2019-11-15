package overskyet.earthquakemonitor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import overskyet.earthquakemonitor.adapters.RecyclerAdapter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static String time = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
    private static final String USGS_URL =
            "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=" + time;
    private String sortBy = "";

    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<Earthquake> earthquakeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        initRecyclerView();
        new GettingEarthquakes().execute();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.main_collapsing_toolbar);
        collapsingToolbar.setTitle(getResources().getString(R.string.app_name));
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerAdapter = new RecyclerAdapter(earthquakeList, MainActivity.this);
        recyclerView.setAdapter(recyclerAdapter);
    }

    private void openCreditsDialog() {
        CreditsAlertDialog dialog = new CreditsAlertDialog();
        dialog.show(getSupportFragmentManager(), getResources().getString(R.string.credits_header));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!menu.findItem(R.id.menu_sort_by_magnitude).isChecked())
        menu.findItem(R.id.menu_sort_by_time).setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_toolbar_menu_refresh:
                earthquakeList.clear();
                recyclerAdapter.notifyDataSetChanged();
                new GettingEarthquakes().execute();
                break;
            case R.id.main_toolbar_menu_credits:
                openCreditsDialog();
                break;
            case R.id.menu_sort_by_time:
                sortBy = "&orderby=time";
                earthquakeList.clear();
                recyclerAdapter.notifyDataSetChanged();
                new GettingEarthquakes().execute();
                item.setChecked(true);
                break;
            case R.id.menu_sort_by_magnitude:
                item.setChecked(true);
                sortBy = "&orderby=magnitude";
                earthquakeList.clear();
                recyclerAdapter.notifyDataSetChanged();
                new GettingEarthquakes().execute();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void initSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                earthquakeList.clear();
                recyclerAdapter.notifyDataSetChanged();
                new GettingEarthquakes().execute();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public class GettingEarthquakes extends AsyncTask<URL, Void, List<Earthquake>> {

        @Override
        protected List<Earthquake> doInBackground(URL... urls) {
            String stringUrl = USGS_URL + sortBy;
            URL url = createUrl(stringUrl);
            String stringJsonObject = makeHttpRequest(url);

            return getListOfEarthquakes(stringJsonObject);
        }

        @Override
        protected void onPostExecute(List<Earthquake> earthquakes) {
            earthquakeList.clear();
            earthquakeList.addAll(earthquakes);
            recyclerAdapter.notifyDataSetChanged();
            initSwipeRefresh();
        }

        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException e) {
                Log.e(TAG, "createUrl: Can't create url ", e);
                return null;
            }
            return url;
        }

        private String makeHttpRequest(URL url) {
            // Do http request
            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            BufferedReader bufferedReader = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.connect();
                inputStream = urlConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                jsonResponse = bufferedReader.lines().collect(Collectors.joining());
            } catch (IOException e) {
                // TODO: Handle the exception
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        // TODO: Handle the exception
                    }
                }
            }
            return jsonResponse;
        }

        private List<Earthquake> getListOfEarthquakes(String stringJsonObject) {
            List<Earthquake> list = new ArrayList<>();

            try {
                JSONObject jsonRootObject = new JSONObject(stringJsonObject);
                JSONArray jsonArray = jsonRootObject.optJSONArray("features");

                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject arrRootObj = jsonArray.getJSONObject(i);
                        JSONObject targetObj = arrRootObj.getJSONObject("properties");

                        String place = targetObj.optString("place");
                        long date = targetObj.optLong("time");
                        double magnitude = targetObj.optDouble("mag");
                        String url = targetObj.optString("url");

                        list.add(new Earthquake(place, date, magnitude, url));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return list;
        }
    }
}
