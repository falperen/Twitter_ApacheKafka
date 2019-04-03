import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


//    "Authors": "ercan",
//    "Content": "gay alperen",
//    "Timestamp": {{$timestamp}},
//    "Location": "Milano",
//    "Tags": [ "Mal", "Meme", "Got" ],
//    "Mentions": [ "Bir", "Bu", "Eksikti" ]

public class Tweet implements Comparable<Tweet>{

    private final String Authors;
    private final String Content;
    private final String Time;
    private final String Location;
    private final List<String> Tags;
    private final List<String>  Mentions;

    public Tweet(String Authors, String Content, String Time, String Location, List<String>  Tags, List<String>  Mentions) {
        this.Authors = Authors;
        this.Content = Content;
        this.Time = Time;
        this.Location = Location;
        this.Tags = Tags;
        this.Mentions = Mentions;

    }
    public String getAuthors() {
        return this.Authors;
    }
    public String getContent() {
        return this.Content;
    }
    public String getTimestamp() {
        return this.Time;
    }
    public long getTimestampLong(){return Long.valueOf(this.Time).longValue();}
    public String getTimestampDate() {
        long unx = Long.valueOf(this.Time).longValue();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy z");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+1"));
        Date date = new Date(unx*1000L);
        return sdf.format(date);}
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
    public int compareTo(Tweet tweet){
        if(tweet.getTimestampLong()<= this.getTimestampLong())
            return -1;
        else
            return 1;
    }

    @Override
    public String toString() {
        return "{Authors:" + Authors + ", Content:" + Content + ", Timestamp:" + Time + ", Location:" + Location
                + ", Tags:" + Tags + ", Mentions:" + Mentions + "}";
    }
}