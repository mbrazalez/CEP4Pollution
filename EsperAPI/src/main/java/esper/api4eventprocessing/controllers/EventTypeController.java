package esper.api4eventprocessing.controllers;

import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.runtime.client.EPDeployException;
import esper.api4eventprocessing.petitions.EventTypePetition;
import esper.api4eventprocessing.responses.EventTypeResponse;
import esper.api4eventprocessing.services.EsperService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EventTypeController {

    private final EsperService esperService;

    public EventTypeController(EsperService esperService) {
        this.esperService = esperService;
    }

    @PostMapping("/api/v1/new_event-type_json")
    public ResponseEntity<?> newEventTypeJson(@RequestBody EventTypePetition newEventType){
        String name = newEventType.name;
        String schema = newEventType.schema;

        if ( name == null ||  schema == null)
            return new ResponseEntity<>("Parameters missing in the request body",HttpStatus.BAD_REQUEST);

        try {
            EventTypeResponse response = esperService.newEventTypeJson(name, schema);

            if (response != null)
                return new ResponseEntity<>(response, HttpStatus.CREATED);

            response = new EventTypeResponse(name, schema, "This event type has been already compiled");
            return new ResponseEntity<>(response,HttpStatus.CONFLICT);

        } catch (EPCompileException e) {
            return new ResponseEntity<>("Failed to compile event type due to syntax or other esper compilation errors." , HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/api/v1/deploy_event-type/{name}")
    public ResponseEntity<?> deployEventType(@PathVariable("name") String eventTypeName){
        try {
            EventTypeResponse response = esperService.deployEventType(eventTypeName);

            if (response != null)
                return new ResponseEntity<>(response, HttpStatus.OK);

            return new ResponseEntity<>("This event type hasn't been compiled yet", HttpStatus.NOT_FOUND);

        } catch (EPDeployException e) {
           return new ResponseEntity<>("Failed during deploying, this event type has been deployed previously" , HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/api/v1/deployed_event-types")
    public ResponseEntity<?> getDeployedEventTypes(){
        List<String> response = esperService.getDeployed("EventType");
        return !response.isEmpty() ? new ResponseEntity<>(response, HttpStatus.OK) : new ResponseEntity<>("There is no event type deployed", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/api/v1/event-type_is_deployed/{id}")
    public boolean isDeployed(@PathVariable("id") String id){
        return esperService.isDeployed(id);
    }

    @GetMapping("/api/v1/event-type_get_deployedid/{name}")
    public ResponseEntity<String> getDeployedId(@PathVariable("name") String name){
        try {
            return new ResponseEntity<>(esperService.getDeployedIdEventType(name), HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>("Event type not found", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/api/v1/undeploy_event-type/{id}")
    public ResponseEntity<?> undeployEventType(@PathVariable("id") String id){
        try {
            String response =  esperService.undeploy(id,"EventType");

            if (response != null)
                return new ResponseEntity<>("The event type " + response + " has been removed successfully", HttpStatus.OK);

        }catch (NullPointerException ex){
            return new ResponseEntity<>("The event type in json format has been removed successfully", HttpStatus.OK);

        }
        return new ResponseEntity<>("There is any event type deployed with the given id or there is a pattern using this event type", HttpStatus.NOT_FOUND);
    }

}

