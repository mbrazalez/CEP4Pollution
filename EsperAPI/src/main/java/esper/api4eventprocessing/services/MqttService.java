package esper.api4eventprocessing.services;

import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.runtime.client.EPDeployException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esper.api4eventprocessing.events.*;
import esper.api4eventprocessing.responses.PatternResponse;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MqttService {
    private final IMqttClient mqttClient;
    private final EsperService esperService;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public MqttService(IMqttClient mqttClient, EsperService esperService) {
        this.mqttClient = mqttClient;
        this.esperService = esperService;
        try {
            this.initializePollutionControlPatterns();
        } catch (EPCompileException e) {
            throw new RuntimeException(e);
        } catch (EPDeployException e) {
            throw new RuntimeException(e);
        }
    }

    public PatternResponse deployPattern(String patternName) throws EPDeployException {
        return this.esperService.deployPattern(patternName, (message, topic) -> {
            executorService.submit(() -> {
                try {
                    if (!mqttClient.isConnected()) {
                        mqttClient.reconnect();
                    }
                    MqttMessage mqttMessage = new MqttMessage(message);
                    mqttMessage.setQos(0);
                    mqttClient.publish(topic, mqttMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }


    public void subscribeToTopics() throws Exception {
        if (!mqttClient.isConnected()) {
            throw new RuntimeException("MQTT client not connected");
        }
        mqttClient.subscribe("pm10topic", this::handleMessage);
        mqttClient.subscribe("pm25topic", this::handleMessage);
        mqttClient.subscribe("humiditytopic", this::handleMessage);
    }

    private void handleMessage(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());

        switch(topic) {
            case "pm10topic":
                PM10Event pm10Event =  this.objectMapper.readValue(payload, PM10Event.class);
                esperService.sendEvent(pm10Event,"PM10Event");
                break;
            case "pm25topic":
                PM25Event pm25Event = this.objectMapper.readValue(payload, PM25Event.class);
                esperService.sendEvent(pm25Event,"PM25Event");
                break;
            case "humiditytopic":
                HumidityEvent humidityEvent = this.objectMapper.readValue(payload, HumidityEvent.class);
                esperService.sendEvent(humidityEvent,"HumidityEvent");
                break;
        }
    }

    private void initializePollutionControlPatterns() throws EPCompileException, EPDeployException {
        esperService.addNewPattern("HighPM10Level", "@Name('HighPM10Level')" +
                " insert into HighPM10Level" +
                " select a1.timestamp as eventTime," +
                " a1.station as stationId," +
                " avg(a1.value) as avgValue" +
                " from pattern[(every a1 = PM10Event)].win:time(1 minutes)" +
                " group by a1.station" +
                " having AVG(a1.value) > 150;");

        esperService.addNewPattern("HighPM25Level","@Name(\'HighPM25Level\')\n" +
                "insert into HighPM25Level\n" +
                "select a1.timestamp as eventTime,\n" +
                "a1.station as stationId,\n" +
                "avg(a1.value) as avgValue\n" +
                "from pattern[(every a1 = PM25Event)].win:time(1 minutes)\n" +
                "group by a1.station\n" +
                "having AVG(a1.value) > 35;");

        esperService.addNewPattern("HighHumidityPercentage", "@Name(\'HighHumidityPercentage\')\n" +
                "insert into HighHumidityPercentage\n" +
                "select a1.timestamp as eventTime,\n" +
                "a1.station as stationId,\n" +
                "avg(a1.value) as avgValue\n" +
                "from pattern[(every a1 = HumidityEvent)].win:time(1 minutes)\n" +
                "group by a1.station\n" +
                "having AVG(a1.value) > 93;");


        this.deployPattern("HighPM10Level");
        this.deployPattern("HighPM25Level");
        this.deployPattern("HighHumidityPercentage");

    }


    }