package esper.api4eventprocessing.controllers;

import esper.api4eventprocessing.events.HumidityEvent;
import esper.api4eventprocessing.events.PM10Event;
import esper.api4eventprocessing.events.PM25Event;
import esper.api4eventprocessing.events.WindSpeedEvent;
import esper.api4eventprocessing.petitions.EventJsonPetition;
import esper.api4eventprocessing.services.EsperService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController {

    private final EsperService esperService;

    public EventController(EsperService esperService) {
        this.esperService = esperService;
    }

    @PostMapping("/api/v1/send_event_json")
    public ResponseEntity<?> sendEventJson(@RequestBody EventJsonPetition eventJsonPetition){
        String name = eventJsonPetition.eventTypeName;
        String content = eventJsonPetition.content;

        if (name == null || content == null)
            return new ResponseEntity<>("Parameters missing in the request body", HttpStatus.BAD_REQUEST);

        try {
            esperService.sendEventJson(name,content);
            return new ResponseEntity<>("Event received by the Esper Engine", HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Wrong JSON format or the given event doesn't match with any event type", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/api/v1/send_pm10-event")
    public ResponseEntity<?> pm10Event(@RequestBody PM10Event pm10Event){
        if (pm10Event.station == null)
            return new ResponseEntity<>("Parameters missing in the request body", HttpStatus.BAD_REQUEST);

        try {
            esperService.sendEvent(pm10Event,"PM10Event");
            return new ResponseEntity<>("Event received by the Esper Engine", HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Wrong JSON format or the given event doesn't match with any event type", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/api/v1/send_pm25-event")
    public ResponseEntity<?> sendPM25Event(@RequestBody PM25Event pm25Event){
        if (pm25Event.station == null)
            return new ResponseEntity<>("Parameters missing in the request body", HttpStatus.BAD_REQUEST);
        try {
            esperService.sendEvent(pm25Event,"PM25Event");
            return new ResponseEntity<>("Event received by the Esper Engine", HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Wrong JSON format or the given event doesn't match with any event type", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/api/v1/send_windspeed-event")
    public ResponseEntity<?> sendPM25Event(@RequestBody WindSpeedEvent windSpeedEvent){
        if (windSpeedEvent.station == null)
            return new ResponseEntity<>("Parameters missing in the request body", HttpStatus.BAD_REQUEST);

        try {
            esperService.sendEvent(windSpeedEvent,"WindSpeedEvent");
            return new ResponseEntity<>("Event received by the Esper Engine", HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Wrong JSON format or the given event doesn't match with any event type", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/api/v1/send_humidity-event")
    public ResponseEntity<?> sendPM25Event(@RequestBody HumidityEvent humidityEvent){
        if (humidityEvent.station == null)
            return new ResponseEntity<>("Parameters missing in the request body", HttpStatus.BAD_REQUEST);

        try {
            esperService.sendEvent(humidityEvent,"HumidityEvent");
            return new ResponseEntity<>("Event received by the Esper Engine", HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Wrong JSON format or the given event doesn't match with any event type", HttpStatus.BAD_REQUEST);
        }
    }

}
