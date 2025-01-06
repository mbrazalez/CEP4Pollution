package esper.api4eventprocessing;

import esper.api4eventprocessing.controllers.EventController;
import esper.api4eventprocessing.controllers.EventTypeController;
import esper.api4eventprocessing.events.PM10Event;
import esper.api4eventprocessing.events.PM25Event;
import esper.api4eventprocessing.events.WindSpeedEvent;
import esper.api4eventprocessing.events.HumidityEvent;
import esper.api4eventprocessing.petitions.EventJsonPetition;
import esper.api4eventprocessing.petitions.EventTypePetition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class EventControllerTests {

    @Autowired
    private EventController eventController;

    @Autowired
    private EventTypeController eventTypeController;

    @Test
    void sendEventJsonWithValidData() {
        this.deployTicker();
        EventJsonPetition petition = new EventJsonPetition();
        petition.eventTypeName = "Ticker";
        petition.content = "{\"symbol\":\"ACME\", \"tstamp\":\"2011-04-01 00:00:00.0\"}";
        ResponseEntity<?> response = eventController.sendEventJson(petition);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void sendEventJsonWithMissingParameters() {
        EventJsonPetition petition = new EventJsonPetition();
        ResponseEntity<?> response = eventController.sendEventJson(petition);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void sendEventWithWrongJSONFormat() {
        EventJsonPetition petition = new EventJsonPetition();
        petition.eventTypeName = "Ticker";
        petition.content = "{bad json}";
        ResponseEntity<?> response = eventController.sendEventJson(petition);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void sendPM10EventWithValidData() {
        PM10Event pm10Event = new PM10Event(System.currentTimeMillis(),42.0f,"station1");
        ResponseEntity<?> response = eventController.pm10Event(pm10Event);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void sendPM10EventWithInvalidData() {
        PM10Event pm10Event = new PM10Event(System.currentTimeMillis(),42.0f,"station1");
        pm10Event.station = null;
        ResponseEntity<?> response = eventController.pm10Event(pm10Event);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void sendPM25EventWithValidData() {
        PM25Event pm25Event = new PM25Event(System.currentTimeMillis(),42.0f,"station1");
        ResponseEntity<?> response = eventController.sendPM25Event(pm25Event);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void sendPM25EventWithInvalidData() {
        PM25Event pm25Event = new PM25Event(System.currentTimeMillis(),42.0f,"station1");
        pm25Event.station = null;
        ResponseEntity<?> response = eventController.sendPM25Event(pm25Event);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void sendWindSpeedEventWithValidData() {
        WindSpeedEvent windSpeedEvent = new WindSpeedEvent(System.currentTimeMillis(),42.0f,"station1",41.0f);
        ResponseEntity<?> response = eventController.sendPM25Event(windSpeedEvent);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void sendWindSpeedEventWithInvalidData() {
        WindSpeedEvent windSpeedEvent = new WindSpeedEvent(System.currentTimeMillis(),42.0f,"station1",41.0f);
        windSpeedEvent.station = null;
        ResponseEntity<?> response = eventController.sendPM25Event(windSpeedEvent);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void sendHumidityEventWithValidData() {
        HumidityEvent humidityEvent = new HumidityEvent(System.currentTimeMillis(),42.0f,"station1");
        ResponseEntity<?> response = eventController.sendPM25Event(humidityEvent);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void sendHumidityEventWithInvalidData() {
        HumidityEvent humidityEvent = new HumidityEvent(System.currentTimeMillis(),42.0f,"station1");
        humidityEvent.station = null;
        ResponseEntity<?> response = eventController.sendPM25Event(humidityEvent);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // Helpers:
    public void deployTicker(){
        EventTypePetition eventTypePetition = new EventTypePetition();
        eventTypePetition.name = "Ticker";
        eventTypePetition.schema = "@public @buseventtype create json schema Ticker(symbol string, tstamp string, total int);";
        eventTypeController.newEventTypeJson(eventTypePetition);
        eventTypeController.deployEventType("Ticker");
    }

}
