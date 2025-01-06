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
                    System.out.println("Mensaje publicado de forma asíncrona.");
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
        mqttClient.subscribe("windtopic", this::handleMessage);
        mqttClient.subscribe("measurementtopic", this::handleMessage);
    }

    private void handleMessage(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        System.out.printf(payload+"\n");

        switch(topic) {
            case "pm10topic":
                try{
                    PM10Event pm10Event =  this.objectMapper.readValue(payload, PM10Event.class);
                    esperService.sendEvent(pm10Event,"PM10Event");
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case "pm25topic":
                PM25Event pm25Event = this.objectMapper.readValue(payload, PM25Event.class);
                esperService.sendEvent(pm25Event,"PM25Event");
                break;
            case "humiditytopic":
                HumidityEvent humidityEvent = this.objectMapper.readValue(payload, HumidityEvent.class);
                esperService.sendEvent(humidityEvent,"HumidityEvent");
                break;
            case "windtopic":
                WindSpeedEvent windSpeedEvent = this.objectMapper.readValue(payload, WindSpeedEvent.class);
                esperService.sendEvent(windSpeedEvent,"WindSpeedEvent");
                break;
            case "measurementtopic":
                StationMeasurement stationMeasurement = this.objectMapper.readValue(payload, StationMeasurement.class);
                esperService.sendEvent(stationMeasurement,"StationMeasurement");
                break;
        }
    }

    private void initializePollutionControlPatterns() throws EPCompileException, EPDeployException {
        esperService.addNewPattern("HighPM10Level", "@Name('HighPM10Level')" +
                " insert into HighPM10Level" +
                " select a1.timestamp as eventTime," +
                " a1.station as stationId," +
                " avg(a1.value) as avgValue" +
                " from pattern[(every a1 = PM10Event)].win:time(5 minutes)" +
                " group by a1.station" +
                " having AVG(a1.value) > 150;");

        esperService.addNewPattern("HighPM25Level","@Name(\'HighPM25Level\')\n" +
                "insert into HighPM25Level\n" +
                "select a1.timestamp as eventTime,\n" +
                "a1.station as stationId,\n" +
                "avg(a1.value) as avgValue\n" +
                "from pattern[(every a1 = PM25Event)].win:time(5 minutes)\n" +
                "group by a1.station\n" +
                "having AVG(a1.value) > 35;");

        esperService.addNewPattern("HighHumidityPercentage", "@Name(\'HighHumidityPercentage\')\n" +
                "insert into HighHumidityPercentage\n" +
                "select a1.timestamp as eventTime,\n" +
                "a1.station as stationId,\n" +
                "avg(a1.value) as avgValue\n" +
                "from pattern[(every a1 = HumidityEvent)].win:time(5 minutes)\n" +
                "group by a1.station\n" +
                "having AVG(a1.value) > 93;");

        esperService.addNewPattern("HighWindSpeed", "@Name(\'HighWindSpeed\')\n" +
                "insert into HighWindSpeed\n" +
                "select a1.timestamp as eventTime,\n" +
                "a1.station as stationId,\n" +
                "a1.value as speed1, a2.value as speed2, a3.value as speed3, a4.value as speed4\n" +
                "from pattern[\n" +
                "every a1=WindSpeedEvent(a1.value > 25) -> \n" +
                "a2=WindSpeedEvent(a1.station = a2.station and \n" +
                "(\n" +
                "(a1.direction > a2.direction and (a1.direction - a2.direction) <= 45) \n" +
                "or \n" +
                "(a1.direction < a2.direction and (a2.direction - a1.direction) <= 45)\n" +
                ") and a2.value > 25\n" +
                ") -> \n" +
                "a3=WindSpeedEvent(a1.station = a3.station and \n" +
                "(\n" +
                "(a1.direction > a3.direction and (a1.direction - a3.direction) <= 45) \n" +
                "or \n" +
                "(a1.direction < a3.direction and (a3.direction - a1.direction) <= 45)\n" +
                ") and a3.value > 25\n" +
                ") -> \n" +
                "a4=WindSpeedEvent(a1.station = a4.station and \n" +
                "(\n" +
                "(a1.direction > a4.direction and (a1.direction - a4.direction) <= 45) \n" +
                "or \n" +
                "(a1.direction < a4.direction and (a4.direction - a1.direction) <= 45)\n" +
                ") and a4.value > 25\n" +
                ")\n" +
                "].win:time(5 minutes);");

        esperService.addNewPattern("Measurements4FIS", "@Name('Measurements4FIS')\n" +
                "insert into Measurements4FIS\n" +
                "select a1.station as stationId,\n" +
                "a1.timestamp as eventTime,\n"+
                "avg(a1.pm10) as pm10Avg,\n" +
                "avg(a1.pm25) as pm25Avg,\n" +
                "avg(a1.humidity) as humidityAvg\n" +
                "from pattern [(every a1 = StationMeasurement)].win:\n" +
                "time(5 minutes)\n");

        this.deployPattern("HighPM10Level");
        this.deployPattern("HighPM25Level");
        this.deployPattern("HighHumidityPercentage");
        this.deployPattern("HighWindSpeed");
        this.deployPattern("Measurements4FIS");

    }


    }