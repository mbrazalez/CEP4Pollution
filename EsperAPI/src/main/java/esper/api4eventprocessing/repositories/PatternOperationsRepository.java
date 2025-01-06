package esper.api4eventprocessing.repositories;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.runtime.client.EPDeployment;
import esper.api4eventprocessing.interfaces.IPatternOperations;
import esper.api4eventprocessing.models.PatternDetails;

import java.util.*;

public class PatternOperationsRepository implements IPatternOperations {
    private final Map<String, PatternDetails> patternDetailsMap;

    public PatternOperationsRepository() {
        this.patternDetailsMap = new HashMap<>();
    }


    @Override
    public boolean isPatternCompiled(String patternName) {
        return patternDetailsMap.containsKey(patternName);
    }

    @Override
    public void addCompiledPattern(String patternName, String query, EPCompiled compiled) {
        PatternDetails newPattern = new PatternDetails(patternName, query, compiled);
        patternDetailsMap.put(patternName, newPattern);
    }

    @Override
    public EPCompiled findCompiledPattern(String patternName) {
        PatternDetails details = patternDetailsMap.get(patternName);
        return details != null ? details.getEpCompiled() : null;    }

    @Override
    public void addDeployedPattern(String patternName, EPDeployment deployment) {
        PatternDetails details = patternDetailsMap.get(patternName);
        details.setEpDeployment(deployment);
    }

    @Override
    public String getPatternQuery(String patternName) {
        PatternDetails details = patternDetailsMap.get(patternName);
        return details.getQuery();
    }

    @Override
    public List<String> getDeployedPatternsWithName() {
        List<String> deployedPatternsWithName = new ArrayList<>();
        Collection<PatternDetails> patterns = this.patternDetailsMap.values();

        for (PatternDetails pattern :  patterns){
            if (pattern.getDeployId() != null){
                deployedPatternsWithName.add(pattern.getName() + ": " + pattern.getDeployId());
            }
        }

        return deployedPatternsWithName;
    }

    @Override
    public String removeDeployedId(String deployId) {
        Collection<PatternDetails> patterns = this.patternDetailsMap.values();

        for (PatternDetails pattern :  patterns){
            if (pattern.getDeployId().equals(deployId)){
                pattern.setDeployId(null);
                return pattern.getName();
            }
        }

        return null;
    }

    @Override
    public String getDeployedId(String patternName){
        return this.patternDetailsMap.get(patternName).getDeployId();
    }

}
