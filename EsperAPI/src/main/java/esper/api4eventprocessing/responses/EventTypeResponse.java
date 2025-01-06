package esper.api4eventprocessing.responses;

public class EventTypeResponse {
    private String name;
    private String schema;
    private String message;

    public EventTypeResponse(String eventTypeName,String schema,String message){
        this.name = eventTypeName;
        this.schema = schema;
        this.message = message;
    }


    public String getName() {
        return this.name;
    }

    public String getSchema() {
        return this.schema;
    }

    public String getMessage() {
        return this.message;
    }
}
