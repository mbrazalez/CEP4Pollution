package esper.api4eventprocessing;

import esper.api4eventprocessing.controllers.EventTypeController;
import esper.api4eventprocessing.petitions.EventTypePetition;
import esper.api4eventprocessing.services.EsperService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EventTypeTests {

    @Autowired
    EventTypeController eventTypeController;

    @Autowired
    EsperService esperService;

    @Test
    void compileEventTypeJsonCorrectly() {
        String name = "UnitTest1";
        String schema = "@public @buseventtype create json schema UnitTest1(val int)";
        ResponseEntity<?> response = this.compileEventType(name,schema);
        // Check if it's compiled successfully
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void compileEventTypeJsonWithSyntaxError(){
        String name = "UnitTest2";
        String schema = "@public @buseventtype create shcema UnitTest2(val int)";
        ResponseEntity<?> response = this.compileEventType(name, schema);
        //Check if we get an error for trying to compile an event type with syntax errors
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void compileExistingEventType(){
        String name = "UnitTest3";
        String schema = "@public @buseventtype create json schema UnitTest3(val int)";
        this.compileEventType(name, schema);
        ResponseEntity<?> response = this.compileEventType(name, schema);;
        // Check if we get an error for trying to compile an existing event type
        assertEquals(HttpStatus.CONFLICT,response.getStatusCode());
    }

    @Test
    void compileEmptyEventType(){
        EventTypePetition petition = new EventTypePetition();
        ResponseEntity<?> response = this.eventTypeController.newEventTypeJson(petition);
        // Check if we get an error for giving a petition with an empty body
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deployEventTypeCorrectly(){
        String name = "UnitTest4";
        String schema = "@public @buseventtype create json schema UnitTest4(val int)";
        this.compileEventType(name, schema);
        ResponseEntity<?> response = this.deployEventType(name);
        // Check if the event type is deployed successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deployEventTypeWithoutCompiling(){
        String name = "UnitTest5";
        ResponseEntity<?> response = this.deployEventType(name);
        // Check that there is no event type deployed called UnitTest5
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getDeployedEventTypes(){
        String name = "UnitTest6";
        String schema = "@public @buseventtype create json schema UnitTest6(val int)";
        this.compileEventType(name, schema);
        this.deployEventType(name);
        ResponseEntity<?> response = this.eventTypeController.getDeployedEventTypes();
        // Check that the list of event types deployed is not empty
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void undeployEventType(){
        String name = "UnitTest7";
        String schema = "@public @buseventtype create json schema UnitTest7(val int)";
        this.compileEventType(name, schema);
        this.deployEventType(name);
        String deployedId = this.eventTypeController.getDeployedId(name).getBody();
        ResponseEntity<?> response = this.eventTypeController.undeployEventType(deployedId);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    void undeployNilPattern(){
        String name = "UnitTest8";
        String deployedId = this.eventTypeController.getDeployedId(name).getBody();
        ResponseEntity<?> response = this.eventTypeController.getDeployedId(deployedId);
        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    void getDeployedEventTypesEmpty(){
        this.esperService.undeployAll();
        ResponseEntity<?> response = this.eventTypeController.getDeployedEventTypes();
        // Check that the list of event types deployed is empty
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Helpers:
    public ResponseEntity<?> compileEventType(String name, String schema){
        EventTypePetition petition = new EventTypePetition();
        petition.name = name;
        petition.schema = schema;
        return this.eventTypeController.newEventTypeJson(petition);
    }

    public ResponseEntity<?> deployEventType(String name){
        return this.eventTypeController.deployEventType(name);
    }

}
