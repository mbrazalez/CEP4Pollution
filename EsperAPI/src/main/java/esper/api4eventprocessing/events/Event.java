package esper.api4eventprocessing.events;

public class Event {
    public String  station;
    public long timestamp;
    public float value;

    public Event(String station, long timestamp, float value){
        this.station = station;
        this.timestamp = timestamp;
        this.value = value;
    }

    public String getStation() { return this.station; }
    public long getTimestamp() {
        return this.timestamp;
    }
    public float getValue() {
        return this.value;
    }
}
