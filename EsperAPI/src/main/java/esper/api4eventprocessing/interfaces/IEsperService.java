package esper.api4eventprocessing.interfaces;

import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.runtime.client.EPDeployException;
import esper.api4eventprocessing.responses.PatternResponse;
import esper.api4eventprocessing.responses.EventTypeResponse;

import java.util.List;


public interface IEsperService {
    EventTypeResponse newEventTypeJson(String eventTypeName, String schema) throws EPCompileException;
    EventTypeResponse deployEventType(String eventTypeName) throws EPDeployException;
    List<String> getDeployed(String type);
    boolean isDeployed(String id);
    String getDeployedIdEventType(String name);
    String undeploy(String id, String type);
    PatternResponse addNewPattern(String patternName, String query) throws EPCompileException;
    PatternResponse deployPattern(String patternName, MqttPublisherCallback callback) throws EPDeployException;
    String getDeployedIdPattern(String name);
    void sendEventJson(String eventTypeName, String content) throws Exception;
    void sendEvent(Object event, String eventType);
    void undeployAll();
}
