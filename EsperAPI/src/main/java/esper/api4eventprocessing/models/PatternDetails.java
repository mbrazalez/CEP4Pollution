package esper.api4eventprocessing.models;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.runtime.client.EPDeployment;

public class PatternDetails {
    private String name;
    private String query;
    private String deployId;
    private EPCompiled epCompiled;
    private EPDeployment epDeployment;

    public PatternDetails(String name, String query, EPCompiled epCompiled){
        this.name = name;
        this.query = query;
        this.epCompiled = epCompiled;
    }

    public String getName() {
        return name;
    }

    public String getDeployId() {
        return deployId;
    }

    public EPCompiled getEpCompiled() {
        return epCompiled;
    }

    public String getQuery() {
        return query;
    }

    public void setEpDeployment(EPDeployment epDeployment) {
        this.epDeployment = epDeployment;
        this.deployId = epDeployment.getDeploymentId();
    }

    public void setDeployId(String deployId){
        this.deployId = deployId;
    }
}
