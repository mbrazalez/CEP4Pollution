package esper.api4eventprocessing;

import esper.api4eventprocessing.controllers.PatternController;
import esper.api4eventprocessing.petitions.PatternPetition;
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
class PatternTests {

    @Autowired
    PatternController patternController;

    @Autowired
    EsperService esperService;

    @Test
    void compilePatternCorrectly() {
        String name = "PM104UnitTest";
        String query = "@name('PM104UnitTest') select * from PM10Event.win:time(10 seconds);";
        ResponseEntity<?> response = this.compilePattern(name,query);
        // Check if it's compiled successfully
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void compileEventTypeJsonWithSyntaxError(){
        String name = "PM104UnitTest2";
        String query = "@name('PM104UnitTest2') select * from FakeEvent.win:time(10 seconds);";
        ResponseEntity<?> response = this.compilePattern(name, query);
        //Check if we get an error for trying to compile a pattern with syntax errors
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void compileExistingEventType(){
        String name = "PM104UnitTest3";
        String query = "@name('PM104UnitTest3') select * from PM10Event.win:time(10 seconds);";
        this.compilePattern(name,query);
        ResponseEntity<?> response = this.compilePattern(name, query);;
        // Check if we get an error for trying to compile an existing pattern
        assertEquals(HttpStatus.CONFLICT,response.getStatusCode());
    }

    @Test
    void compileEmptyPattern(){
        PatternPetition petition = new PatternPetition();
        ResponseEntity<?> response = this.patternController.addNewPattern(petition);
        // Check if we get an error for giving a petition with an empty body
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deployPatternCorrectly(){
        String name = "PM104UnitTest4";
        String query = "@name('PM104UnitTest4') select * from PM10Event.win:time(10 seconds);";
        this.compilePattern(name, query);
        ResponseEntity<?> response = this.deployPattern(name);
        // Check if the pattern is deployed successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deployPatternWithoutCompiling(){
        String name = "PM104UnitTest5";
        ResponseEntity<?> response = this.deployPattern(name);
        // Check that there is no pattern deployed called PM104UnitTest5
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getDeployedPatterns(){
        String name = "PM104UnitTest6";
        String query = "@name('PM104UnitTest6') select * from PM10Event.win:time(10 seconds);";
        this.compilePattern(name, query);
        this.deployPattern(name);
        ResponseEntity<?> response = this.patternController.getDeployedPatterns();
        // Check that the list of patterns deployed is not empty
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getDeployedPatternsEmpty(){
        this.esperService.undeployAll();
        ResponseEntity<?> response = this.patternController.getDeployedPatterns();
        // Check that the list of patterns deployed is empty
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void undeployPattern(){
        String name = "PM104UnitTest7";
        String query = "@name('PM104UnitTest7') select * from PM10Event.win:time(10 seconds);";
        this.compilePattern(name, query);
        this.deployPattern(name);
        String deployedId = this.patternController.getDeployedId("PM104UnitTest7").getBody();
        ResponseEntity<?> response = this.patternController.undeployPattern(deployedId);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    void undeployNilPattern(){
        String name = "PM104UnitTest8";
        String deployedId = this.patternController.getDeployedId(name).getBody();
        ResponseEntity<?> response = this.patternController.getDeployedId(deployedId);
        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    // Helpers:
    public ResponseEntity<?> compilePattern(String name, String query){
        PatternPetition petition = new PatternPetition();
        petition.name = name;
        petition.query = query;
        return this.patternController.addNewPattern(petition);
    }

    public ResponseEntity<?> deployPattern(String name){
        return this.patternController.deployPattern(name);
    }

}
