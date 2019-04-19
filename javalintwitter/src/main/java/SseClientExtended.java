import io.javalin.Context;
import io.javalin.serversentevent.SseClient;

public class SseClientExtended extends SseClient {
private FilterClass filterC;
    public SseClientExtended(Context ctx, FilterClass filterClass){
        super(ctx);
        this.filterC = filterClass;
    }
    public void setFilterC(FilterClass newFilter){
        this.filterC = newFilter;
    }
    public FilterClass getFilterC(){
        return this.filterC;
    }
}
