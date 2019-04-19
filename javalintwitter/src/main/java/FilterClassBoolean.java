import java.util.List;

public class FilterClassBoolean {
    private final boolean Location;
    private final boolean Tags;
    private final boolean  Mentions;

    public FilterClassBoolean(){
        this.Location = true;
        this.Tags = true;
        this.Mentions = true;
    }
    public FilterClassBoolean(FilterClass filterClass) {
        super();
        this.Location = filterClass.getLocation().isEmpty();
        this.Tags = filterClass.getTags().isEmpty();
        this.Mentions = filterClass.getMentions().isEmpty();

    }
    public boolean getLocation() {
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
        if(this.getLocation() && this.getTags() && this.getMentions())
            return true;
        else
            return false;
    }
}
