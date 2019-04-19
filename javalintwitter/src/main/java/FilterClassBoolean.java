import java.util.List;

public class FilterClassBoolean {
    private final String Location;
    private final boolean Tags;
    private final boolean  Mentions;

    public FilterClassBoolean(){
        this.Location = "";
        this.Tags = true;
        this.Mentions = true;
    }
    public FilterClassBoolean(FilterClass filterClass) {
        super();
        this.Location = filterClass.getLocation();
        this.Tags = filterClass.getTags().isEmpty();
        this.Mentions = filterClass.getMentions().isEmpty();

    }
    public String getLocation() {
        return this.Location;
    }
    public boolean getTags() {
        return this.Tags;
    }
    public boolean  getMentions() {
        return this.Mentions;
    }
    @Override
    public String toString() {
        return Location + "_" + Tags + "_" + Mentions;
    }
    public boolean isFilterEmpty(){
        if(this.getLocation().isEmpty() && this.getTags() && this.getMentions())
            return true;
        else
            return false;
    }
}
