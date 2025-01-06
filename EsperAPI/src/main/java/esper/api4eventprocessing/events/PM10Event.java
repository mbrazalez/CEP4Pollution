package esper.api4eventprocessing.events;

public class PM10Event {
    public long timestamp;
    public float value;
    public String station;

    public PM10Event (long timestamp, float value, String station){
        this.timestamp = timestamp;
        this.value = value;
        this.station = station;
    }

    public String getStation() { return this.station; }
    public long getTimestamp() {
        return this.timestamp;
    }
    public float getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "PM10Event [" + this.getTimestamp() + " " + this.getValue() + " " + this.getStation() + "]";
    }
}
