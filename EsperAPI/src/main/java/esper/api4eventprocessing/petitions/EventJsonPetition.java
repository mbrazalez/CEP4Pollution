package esper.api4eventprocessing.petitions;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EventJsonPetition {
    @JsonProperty("name")
    public String eventTypeName;
    @JsonProperty("content")
    public String content;
}
