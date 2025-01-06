package esper.api4eventprocessing.services;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPUndeployException;
import esper.api4eventprocessing.interfaces.IEsperService;
import esper.api4eventprocessing.interfaces.MqttPublisherCallback;
import esper.api4eventprocessing.repositories.EventTypeOperationsRepository;
import esper.api4eventprocessing.repositories.EsperEngineRepository;
import esper.api4eventprocessing.repositories.PatternOperationsRepository;
import esper.api4eventprocessing.responses.EventTypeResponse;
import esper.api4eventprocessing.responses.PatternResponse;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
public class EsperService implements IEsperService {
    private final EsperEngineRepository esperEngineRepository;
    private final EventTypeOperationsRepository eventTypeOperationsRepository;
    private final PatternOperationsRepository patternOperationsRepository;

    public EsperService() {
        this.esperEngineRepository = EsperEngineRepository.getInstance();
        this.eventTypeOperationsRepository = new EventTypeOperationsRepository();
        this.patternOperationsRepository = new PatternOperationsRepository();
    }

    @Override
    public EventTypeResponse newEventTypeJson(String eventTypeName, String schema) throws EPCompileException {
        boolean eventAlreadyCompiled = this.eventTypeOperationsRepository.isEventTypeCompiled(eventTypeName);

        if(!eventAlreadyCompiled){
            EPCompiled epCompiled = this.esperEngineRepository.compile(schema);
            this.eventTypeOperationsRepository.addCompiledEventType(eventTypeName, schema, epCompiled);
            return new EventTypeResponse(eventTypeName, schema, "Event type compiled successfully");
        }

        return null;
    }

    @Override
    public EventTypeResponse deployEventType(String eventTypeName) throws EPDeployException {
        EPCompiled epCompiled = this.eventTypeOperationsRepository.findCompiledEventType(eventTypeName);

        if (epCompiled != null){
            EPDeployment epDeployment = this.esperEngineRepository.deploy(epCompiled);
            this.eventTypeOperationsRepository.addDeployedEventType(eventTypeName, epDeployment);
            String schema = this.eventTypeOperationsRepository.getEventTypeSchema(eventTypeName);
            return new EventTypeResponse(eventTypeName, schema, "Event type deployed successfully with id " + epDeployment.getDeploymentId());
        }

        return null;
    }

    @Override
    public List<String> getDeployed(String type){
        String[] deployedEventTypes = this.esperEngineRepository.getDeployedEventTypes();

        if (deployedEventTypes.length > 0){
            if (type.equals("EventType")){
                return this.eventTypeOperationsRepository.getDeployedEventTypesWithName();
            }else {
                return this.patternOperationsRepository.getDeployedPatternsWithName();
            }
        }

        return Collections.emptyList();
    }

    @Override
    public boolean isDeployed(String id){
        return this.esperEngineRepository.isDeployed(id);
    }

    @Override
    public String getDeployedIdEventType(String name){
        return this.eventTypeOperationsRepository.getDeployedId(name);
    }

    @Override
    public String undeploy(String id, String type) {
        try {
            String deployId = this.esperEngineRepository.undeploy(id);

            if (type.equals("EventType")){
                return this.eventTypeOperationsRepository.removeDeployedId(deployId);
            }else {
                return this.patternOperationsRepository.removeDeployedId(deployId);
            }

        } catch (EPUndeployException e) {
            return null;
        }
    }

    @Override
    public PatternResponse addNewPattern(String patternName, String query) throws EPCompileException {
        boolean patternAlreadyCompiled = this.patternOperationsRepository.isPatternCompiled(patternName);

        if (!patternAlreadyCompiled){
            EPCompiled epCompiled = this.esperEngineRepository.compile(query);
            this.patternOperationsRepository.addCompiledPattern(patternName, query,epCompiled);
            return new PatternResponse(patternName, query, "Pattern compiled successfully");
        }

        return  null;
    }

    @Override
    public PatternResponse deployPattern(String patternName,  MqttPublisherCallback callback) throws EPDeployException {
        EPCompiled epCompiled = this.patternOperationsRepository.findCompiledPattern(patternName);

        if (epCompiled != null){
            EPDeployment epDeployment = this.esperEngineRepository.deploy(epCompiled);
            String deploymentId = epDeployment.getDeploymentId();
            this.esperEngineRepository.addListener(patternName,deploymentId, callback);
            this.patternOperationsRepository.addDeployedPattern(patternName, epDeployment);
            String query = this.patternOperationsRepository.getPatternQuery(patternName);
            return new PatternResponse(patternName, query, "Pattern deployed successfully with id " + deploymentId);
        }

        return null;
    }

    @Override
    public String getDeployedIdPattern(String name){
        return this.patternOperationsRepository.getDeployedId(name);
    }

    @Override
    public void sendEventJson(String eventTypeName, String content) throws Exception {
        this.esperEngineRepository.sendEventJson(eventTypeName, content);
    }

    @Override
    public void sendEvent(Object event, String eventType){
        this.esperEngineRepository.sendEvent(event, eventType);
    }

    @Override
    public void undeployAll(){
        esperEngineRepository.undeployAll();
    }

}
