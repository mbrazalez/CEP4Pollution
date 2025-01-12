package esper.api4eventprocessing.interfaces;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.runtime.client.EPDeployment;
import java.util.List;

public interface IEventTypeOperations {
     boolean isEventTypeCompiled(String eventTypeName);
     void addCompiledEventType(String eventTypeName, String schema,EPCompiled compiled);
     EPCompiled findCompiledEventType(String eventTypeName);
     void addDeployedEventType(String eventTypeName, EPDeployment deployment);
     String getEventTypeSchema(String eventTypeName);
     List<String> getDeployedEventTypesWithName();
     String removeDeployedId(String deployId);
     String getDeployedId(String eventTypeName);
}
