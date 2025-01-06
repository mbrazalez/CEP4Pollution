package esper.api4eventprocessing.interfaces;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.runtime.client.EPDeployment;

import java.util.List;

public interface IPatternOperations {
     boolean isPatternCompiled(String patternName);
     void addCompiledPattern(String patternName, String query, EPCompiled compiled);
     EPCompiled findCompiledPattern(String patterName);
     void addDeployedPattern(String patternName, EPDeployment deployment);
     String getPatternQuery(String patternName);
     List<String> getDeployedPatternsWithName();
     String removeDeployedId(String deployId);
     String getDeployedId(String patternName);

}
