package esper.api4eventprocessing.models;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.runtime.client.EPDeployment;

public class EventTypeDetails {
    private String name;
    private String schema;
    private EPCompiled epCompiled;
    private String deployId;
    private EPDeployment epDeployment;

    public EventTypeDetails(String name, String schema,EPCompiled epCompiled){
        this.name = name;
        this.schema = schema;
        this.epCompiled = epCompiled;
    }

    public String getName() {
        return name;
    }

    public EPCompiled getEpCompiled() {
        return epCompiled;
    }

    public String getSchema() {
        return schema;
    }

    public String getDeployId() {
        return deployId;
    }

    public void setEpDeployment(EPDeployment epDeployment) {
        this.epDeployment = epDeployment;
        this.deployId = epDeployment.getDeploymentId();
    }

    public void setDeployId(String deployId){
        this.deployId = deployId;
    }
}
