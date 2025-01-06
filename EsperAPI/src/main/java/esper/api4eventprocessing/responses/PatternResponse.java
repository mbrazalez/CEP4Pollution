package esper.api4eventprocessing.responses;

public class PatternResponse {
    private String name;
    private String query;
    private String message;

    public PatternResponse(String eventTypeName,String query,String message){
        this.name = eventTypeName;
        this.query = query;
        this.message = message;
    }


    public String getName() {
        return this.name;
    }

    public String getSchema() {
        return this.query;
    }

    public String getMessage() {
        return this.message;
    }

}

