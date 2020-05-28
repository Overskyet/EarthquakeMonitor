package overskyet.earthquakemonitor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import overskyet.earthquakemonitor.adapters.RecyclerAdapter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String USGS_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=";
    private static final String ORDER_BY_TIME_PARAMETER = "&orderby=time";
    private static final String ORDER_BY_MAGNITUDE_PARAMETER = "&orderby=magnitude";
    private static final String END_TIME_PARAMETER = "&endtime=";
    private boolean mIsMenuMagnitudeChecked = false;
    private boolean mIsDateSet = false;
    private static String mTotalUsgsUrl;
    private static String mSortBy = "";
    private String mCustomDateCurrent;
    private String mCustomDateNext;

    private TextView mEmptyRecyclerViewPlaceholder;
    private RecyclerAdapter mRecyclerAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    private List<Earthquake> mEarthquakeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mIsDateSet = savedInstanceState.getBoolean("isDateSet");
            mIsMenuMagnitudeChecked = savedInstanceState.getBoolean("isMenuMagnitudeChecked");
            mTotalUsgsUrl = savedInstanceState.getString("mTotalUsgsUrl");
            mSortBy = savedInstanceState.getString("mSortBy");
            mCustomDateCurrent = savedInstanceState.getString("mCustomDateCurrent");
            mCustomDateNext = savedInstanceState.getString("mCustomDateNext");
        }
        setContentView(R.layout.activity_main);

        mEmptyRecyclerViewPlaceholder = findViewById(R.id.empty_recycler_view_placeholder);

        initToolbar();
        initRecyclerView();
        setCurrentDate();
        startEarthquakeAsyncTask();
        initSwipeRefresh();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isDateSet", mIsDateSet);
        outState.putBoolean("isMenuMagnitudeChecked", mIsMenuMagnitudeChecked);
        outState.putString("mTotalUsgsUrl", mTotalUsgsUrl);
        outState.putString("mSortBy", mSortBy);
        outState.putString("mCustomDateCurrent", mCustomDateCurrent);
        outState.putString("mCustomDateNext", mCustomDateNext);
    }

    private void startEarthquakeAsyncTask() {
        new EarthquakesAsyncTask(MainActivity.this).execute(mTotalUsgsUrl + mSortBy);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.main_collapsing_toolbar);
        collapsingToolbar.setTitle(getResources().getString(R.string.app_name));
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mRecyclerAdapter = new RecyclerAdapter(mEarthquakeList, MainActivity.this);
        recyclerView.setAdapter(mRecyclerAdapter);
    }

    private void setCurrentDate() {
        if (!mIsDateSet) {
            @SuppressLint("SimpleDateFormat")
            String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
            mTotalUsgsUrl = USGS_URL + currentDate;
            mIsDateSet = true;
        }
    }

    private void setCustomDate() {
        mTotalUsgsUrl = USGS_URL + mCustomDateCurrent + END_TIME_PARAMETER + mCustomDateNext;
        startEarthquakeAsyncTask();
    }

    private void displayDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int y, int m, int d) {
                m = m + 1;
                mCustomDateCurrent = y + "-" + m + "-" + d;
                mCustomDateNext = y + "-" + m + "-" + (d + 1);
                setCustomDate();
            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                MainActivity.this,
                R.style.datePickerDialog,
                mDateSetListener,
                year, month, day);
        Objects.requireNonNull(datePickerDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        datePickerDialog.show();
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
        if (!mIsMenuMagnitudeChecked) menu.findItem(R.id.menu_sort_by_time).setChecked(true);
        else menu.findItem(R.id.menu_sort_by_magnitude).setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_toolbar_menu_refresh:
                startEarthquakeAsyncTask();
                break;
            case R.id.menu_select_date:
                displayDatePickerDialog();
                break;
            case R.id.main_toolbar_menu_credits:
                openCreditsDialog();
                break;
            case R.id.menu_sort_by_time:
                mIsMenuMagnitudeChecked = false;
                mSortBy = ORDER_BY_TIME_PARAMETER;
                item.setChecked(true);
                startEarthquakeAsyncTask();
                break;
            case R.id.menu_sort_by_magnitude:
                mIsMenuMagnitudeChecked = true;
                mSortBy = ORDER_BY_MAGNITUDE_PARAMETER;
                item.setChecked(true);
                startEarthquakeAsyncTask();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void initSwipeRefresh() {
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startEarthquakeAsyncTask();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public static class EarthquakesAsyncTask extends AsyncTask<String, Void, List<Earthquake>> {

        private WeakReference<MainActivity> activityWeakReference;

        EarthquakesAsyncTask(MainActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected List<Earthquake> doInBackground(String... urls) {
            URL url;
            if (urls.length == 0) {
                url = createUrl(mTotalUsgsUrl + mSortBy);
            } else {
                url = createUrl(urls[0]);
            }
            String stringJsonObject = url == null ? "" : makeHttpRequest(url);

            return getListOfEarthquakes(stringJsonObject);
        }

        @Override
        protected void onPostExecute(List<Earthquake> earthquakes) {
            MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing() || earthquakes == null) return;

            if (earthquakes.isEmpty()) {
                //set placeholder visible
                activity.mEarthquakeList.clear();
                activity.mRecyclerAdapter.notifyDataSetChanged();
                activity.mEmptyRecyclerViewPlaceholder.setVisibility(View.VISIBLE);
            } else {
                // set placeholder gone
                activity.mEmptyRecyclerViewPlaceholder.setVisibility(View.GONE);
                activity.mEarthquakeList.clear();
                activity.mEarthquakeList.addAll(earthquakes);
                activity.mRecyclerAdapter.notifyDataSetChanged();
            }
        }

        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException e) {
                Log.e(TAG, "createUrl: Can't create url ", e);
            }
            return url;
        }

        private String makeHttpRequest(URL url) {
            String jsonResponse = "";
            HttpURLConnection urlConnection;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.connect();
                if (urlConnection.getResponseCode() != 200) {
                    Log.e(TAG, "makeHttpRequest: " + urlConnection.getResponseCode() + " response code.");
                    urlConnection.disconnect();
                    return jsonResponse;
                }
                inputStream = urlConnection.getInputStream();
                jsonResponse = readStream(inputStream);
            } catch (IOException e) {
                Log.e(TAG, "makeHttpRequest: HttpUrlConnection: ", e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "makeHttpRequest: inputStream.close(): ", e);
                    }
                }
            }
            return jsonResponse;
        }

        private String readStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line = bufferedReader.readLine();
                while (line != null) {
                    output.append(line);
                    line = bufferedReader.readLine();
                }
            }
            return output.toString();
        }

        private List<Earthquake> getListOfEarthquakes(String stringJsonObject) {
            if (stringJsonObject.isEmpty()) return null;

            List<Earthquake> list = null;

            try {
                list = new ArrayList<>();
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
                Log.e(TAG, "getListOfEarthquakes: JSONException", e);
            }

            return list;
        }
    }
}
