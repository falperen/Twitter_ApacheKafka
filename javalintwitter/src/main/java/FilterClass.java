import java.util.List;

public class FilterClass {
    private final String Location;
    private final List<String> Tags;
    private final List<String>  Mentions;

    public FilterClass(String Location, List<String>  Tags, List<String>  Mentions) {
        super();
        this.Location = Location;
        this.Tags = Tags;
        this.Mentions = Mentions;

    }
    public String getLocation() {
        return this.Location;
    }
    public List<String> getTags() {
        return this.Tags;
    }
    public List<String>  getMentions() {
        return this.Mentions;
    }
    @Override
    public String toString() {
        return "Tweet [ Location:" + Location
                + ", Tags:" + Tags + ", Mentions:" + Mentions + "]";
    }
}
