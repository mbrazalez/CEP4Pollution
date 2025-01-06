package esper.api4eventprocessing.repositories;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.client.util.EventTypeBusModifier;
import com.espertech.esper.common.client.util.NameAccessModifier;
import com.espertech.esper.compiler.client.*;
import com.espertech.esper.runtime.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import esper.api4eventprocessing.events.*;
import esper.api4eventprocessing.interfaces.MqttPublisherCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class EsperEngineRepository {
    private static final Logger log = LoggerFactory.getLogger(EsperEngineRepository.class);
    private static EsperEngineRepository instance;
    private Configuration configuration;
    private CompilerArguments compilerArguments;
    private EPCompiler epCompiler;
    private EPRuntime epRuntime;

    private EsperEngineRepository() {
        initializeConfiguration();
    }

    public static synchronized EsperEngineRepository getInstance() {
        if (instance == null) {
            instance = new EsperEngineRepository();
        }
        return instance;
    }

    private void initializeConfiguration() {
        this.configuration = new Configuration();
        this.configuration.getCompiler().getByteCode().setAccessModifierEventType(NameAccessModifier.PUBLIC);
        this.configuration.getCompiler().getByteCode().setBusModifierEventType(EventTypeBusModifier.BUS);
        this.initializePollutionControlEventTypes();
        this.compilerArguments = new CompilerArguments(this.configuration);
        this.epCompiler = EPCompilerProvider.getCompiler();
        this.epRuntime = EPRuntimeProvider.getDefaultRuntime(this.configuration);
        System.out.printf("########################################\n");
        System.out.printf("### Esper Singleton Engine is ready! ###\n");
        System.out.printf("########################################\n");
    }

    private void initializePollutionControlEventTypes() {
        this.configuration.getCommon().addEventType(HumidityEvent.class);
        this.configuration.getCommon().addEventType(PM10Event.class);
        this.configuration.getCommon().addEventType(WindSpeedEvent.class);
        this.configuration.getCommon().addEventType(PM25Event.class);
        this.configuration.getCommon().addEventType(StationMeasurement.class);
    }



    public EPCompiled compile(String epl) throws EPCompileException {
        this.compilerArguments = new CompilerArguments(this.configuration);
        this.compilerArguments.getPath().add(this.epRuntime.getRuntimePath());
       // this.compilerArguments.getConfiguration().getCompiler().getByteCode().setAllowSubscriber(true);
        return epCompiler.compile(epl, this.compilerArguments);
    }

    public EPDeployment deploy(EPCompiled epCompiled) throws EPDeployException {
        return this.epRuntime.getDeploymentService().deploy(epCompiled);
    }

    public String[] getDeployedEventTypes(){
        return epRuntime.getDeploymentService().getDeployments();
    }

    public boolean isDeployed(String id){
        return epRuntime.getDeploymentService().isDeployed(id);
    }

    public String undeploy(String deploymentId) throws EPUndeployException {
        epRuntime.getDeploymentService().undeploy(deploymentId);
        return deploymentId;
    }

    public void sendEvent(Object event, String name) {
        epRuntime.getEventService().sendEventBean(event, name);
    }

    public void sendEventJson(String name, String event) throws Exception {
        epRuntime.getEventService().sendEventJson(event,name);
    }

    public void undeployAll(){
        try {
            epRuntime.getDeploymentService().undeployAll();
        } catch (EPUndeployException e) {
            throw new RuntimeException(e);
        }
    }

    public void addListener(String name, String id, MqttPublisherCallback callback){
        EPStatement deployedStmt = this.epRuntime.getDeploymentService().getStatement(id, name);
        deployedStmt.addListener((newEvents, oldEvents, stmt, runtime) -> {
            if (newEvents != null){
                EventBean lastEvent = newEvents[0];
                System.out.printf("##################################\n");
                System.out.printf("Evento complejo detectado \n");
                System.out.printf("##################################\n");
                try {
                    Map<String, Object> eventProperties = new HashMap<>();
                    String eventType = lastEvent.getEventType().getName();
                    String topic = "ceptopic";

                    if (eventType.equals("Measurements4FIS")){
                        eventProperties.put("eventTime", lastEvent.get("eventTime"));
                        eventProperties.put("stationId", lastEvent.get("stationId"));
                        eventProperties.put("avgPM10", lastEvent.get("pm10Avg"));
                        eventProperties.put("avgPM25", lastEvent.get("pm25Avg"));
                        eventProperties.put("avgHumidity", lastEvent.get("humidityAvg"));

                        topic = "fistopic";

                    }else if (eventType.equals("HighWindSpeed")){
                        eventProperties.put("eventTime", lastEvent.get("eventTime"));
                        eventProperties.put("stationId", lastEvent.get("stationId"));
                        eventProperties.put("speed1", lastEvent.get("speed1"));
                        eventProperties.put("speed2", lastEvent.get("speed2"));
                        eventProperties.put("speed3", lastEvent.get("speed3"));
                        eventProperties.put("speed4", lastEvent.get("speed4"));


                    }else{
                        eventProperties.put("eventTime", lastEvent.get("eventTime"));
                        eventProperties.put("stationId", lastEvent.get("stationId"));
                        eventProperties.put("avgValue", lastEvent.get("avgValue"));
                        eventProperties.put("type", eventType);
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    String eventString = objectMapper.writeValueAsString(eventProperties);

                    callback.publishAsync(eventString.getBytes(), topic);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
