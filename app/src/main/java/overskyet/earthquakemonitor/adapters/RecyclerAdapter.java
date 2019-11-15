package overskyet.earthquakemonitor.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import overskyet.earthquakemonitor.Earthquake;
import overskyet.earthquakemonitor.R;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private static final String TAG = RecyclerAdapter.class.getSimpleName();

    private List<Earthquake> earthquakeList;
    private Context mContext;

    public RecyclerAdapter(List<Earthquake> earthquakes, Context context) {
        this.earthquakeList = earthquakes;
        this.mContext = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_activity_recycler_item, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Earthquake earthquake = earthquakeList.get(position);
        double mag = earthquake.getMagnitude();
        holder.imageBackground.setColor(getBackgroundColor(mag));
        holder.imageText.setText(String.format("%2.1f", mag));
        holder.distanceText.setText(getDistance(earthquake.getPlace()));
        holder.locationText.setText(getLocation(earthquake.getPlace()));
        holder.dateText.setText(getDate(earthquake.getTimestamp()));
        holder.timeText.setText(getTime(earthquake.getTimestamp()));
        setContainerListener(holder.container, earthquake.getUrl());
    }

    @Override
    public int getItemCount() {
        return earthquakeList.size();
    }

    private int getBackgroundColor(double mag) {
        int colorResId;
        switch ( (int) Math.floor(mag) ) {
            case 0:
            case 1:
                colorResId = R.color.mag_1_0;
                break;
            case 2:
                colorResId = R.color.mag_2_0;
                break;
            case 3:
                colorResId = R.color.mag_3_0;
                break;
            case 4:
                colorResId = R.color.mag_4_0;
                break;
            case 5:
                colorResId = R.color.mag_5_0;
                break;
            case 6:
                colorResId = R.color.mag_6_0;
                break;
            case 7:
                colorResId = R.color.mag_7_0;
                break;
            case 8:
                colorResId = R.color.mag_8_0;
                break;
            case 9:
                colorResId = R.color.mag_9_0;
                break;
            default:
                colorResId = R.color.mag_10_0;
                break;
        }
        return ContextCompat.getColor(mContext, colorResId);
    }

    private String getDistance(String prm) {
        if (prm == null) return "No data";
        if (!(prm.contains("of"))) return "Near the";
        int index = prm.indexOf("of");
        return prm.substring(0, index + 2);
    }

    private String getLocation(String prm) {
        if (prm == null) return "No data";
        if (prm.contains("of")) {
            int index = prm.indexOf("of");
            return prm.substring(index + 3);
        }
        return prm;
    }

    private String getDate(long timestamp) {
        Date date = new Date(timestamp);
        DateFormat formatter = DateFormat.getDateInstance();
        return formatter.format(date);
    }

    private String getTime(long timestamp) {
        Date date = new Date(timestamp);
        DateFormat formatter = new SimpleDateFormat("h:mm a");
        return formatter.format(date);
    }

    private void setContainerListener(LinearLayout container, String link) {
        String url = link;
        final Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
        sendIntent.setData(Uri.parse(url));

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendIntent.resolveActivity(mContext.getPackageManager()) != null) {
                    mContext.startActivity(sendIntent);
                }
            }
        });
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout container;
        private TextView imageText;
        private GradientDrawable imageBackground;
        private TextView distanceText;
        private TextView locationText;
        private TextView dateText;
        private TextView timeText;

        public MyViewHolder(@NonNull View v) {
            super(v);
            container = v.findViewById(R.id.linear_layout_container);
            imageText = v.findViewById(R.id.main_recycler_image);
            imageBackground = (GradientDrawable) imageText.getBackground();
            distanceText = v.findViewById(R.id.main_recycler_distance_text_view);
            locationText = v.findViewById(R.id.main_recycler_location_text_view);
            dateText = v.findViewById(R.id.main_recycler_date_text_view);
            timeText = v.findViewById(R.id.main_recycler_time_text_view);
        }
    }
}
