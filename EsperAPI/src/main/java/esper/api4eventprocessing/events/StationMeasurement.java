package esper.api4eventprocessing.events;

public class StationMeasurement {
    public long timestamp;
    public String  station;
    public float pm10;
    public float pm25;
    public float humidity;

    public StationMeasurement (long timestamp, String station, float pm10, float pm25, float humidity){
        this.timestamp = timestamp;
        this.station = station;
        this.pm10 = pm10;
        this.pm25 = pm25;
        this.humidity = humidity;
    }

    public String getStation() { return this.station; }
    public long getTimestamp() {
        return this.timestamp;
    }
    public float getPm10() {return this.pm10; }
    public float getPm25() {return this.pm25; }
    public float getHumidity() {return this.humidity; }

    public String toString(){
        return timestamp + " " + station + " " + pm10 + " " + pm25 + " " + humidity;
    }
}
