package esper.api4eventprocessing.events;

public class PM25Event {
    public long timestamp;
    public float value;
    public String  station;

    public PM25Event (long timestamp, float value, String station){
        this.timestamp = timestamp;
        this.station = station;
        this.value = value;
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
        return "PM25Event [" + this.getTimestamp() + " " + this.getValue() + " " + this.getStation() + "]";
    }
}
