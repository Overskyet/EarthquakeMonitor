package overskyet.earthquakemonitor;

public class Earthquake {

    private String place;
    private long timestamp;
    private double magnitude;
    private String url;

    Earthquake(String place, long timestamp, double magnitude, String url) {
        this.place = place;
        this.timestamp = timestamp;
        this.magnitude = magnitude;
        this.url = url;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getPlace() {
        return place;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "Earthquake{" +
                "place='" + place + '\'' +
                ", timestamp=" + timestamp +
                ", magnitude=" + magnitude +
                ", url='" + url + '\'' +
                '}';
    }
}