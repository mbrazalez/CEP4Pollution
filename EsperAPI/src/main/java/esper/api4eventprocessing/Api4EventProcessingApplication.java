package esper.api4eventprocessing;

import esper.api4eventprocessing.services.MqttService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Api4EventProcessingApplication {
    @Autowired
    private MqttService mqttService;
    public static void main(String[] args) {
        SpringApplication.run(Api4EventProcessingApplication.class, args);
    }

    @PostConstruct
    public void subscribeToTopics() {
        try {
            mqttService.subscribeToTopics();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}