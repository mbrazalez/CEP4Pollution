package esper.api4eventprocessing.events;

public class WindSpeedEvent {
    public long timestamp;
    public float value;
    public String  station;
    public float direction;

    public WindSpeedEvent (long timestamp, float value, String station,  float direction){
        this.timestamp = timestamp;
        this.value = value;
        this.station = station;
        this.direction = direction;
    }

    public String getStation() { return this.station; }
    public long getTimestamp() {
        return this.timestamp;
    }
    public float getValue() {
        return this.value;
    }
    public float getDirection(){
        return this.direction;
    }

    @Override
    public String toString() {
        return "WindSpeedEvent [" + this.getTimestamp() + " " + this.getValue() + " " + this.getStation() + " " + this.getDirection() + "]";
    }
}
