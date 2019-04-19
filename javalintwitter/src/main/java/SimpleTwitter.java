import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.serversentevent.SseClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class SimpleTwitter {

    public static void main(String[] args) throws InterruptedException {

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy z");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));

        String bootstrapServers = "127.0.0.1:9092"; //169.254.32.251
        String topic = "test2_topic";

        int numPartitions=8;
        String x = "İzmir";
        byte[] m = x.getBytes();
        int y = Utils.toPositive(Utils.murmur2(m)) % numPartitions;
        System.out.println("Partition=" + y);

        ArrayList<TopicPartition> topicAssignment = new ArrayList<TopicPartition>();

        for(int i = 0; i<=numPartitions-1; i++){
            topicAssignment.add(new TopicPartition(topic, i));
        }

        Logger logger = LoggerFactory.getLogger(SimpleTwitter.class.getName());
        final ArrayList<User> userList = new ArrayList<User>();


        // create Producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Javalin app = Javalin.create().enableCorsForAllOrigins().start(7000);

        app.post("/users/:name", ctx -> {
            User user = new User(ctx.pathParam("name"));
            userList.add(user);
            ctx.result(ctx.pathParam("name") + " subscribed!");
        });

        app.post("/tweets", ctx -> {

            KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

            try {//checking the json data types, if they are not the same returns warning to the client

                Tweet tweet = new Gson().fromJson(ctx.body(), Tweet.class);
                Tweet twt =  new Tweet(tweet.getAuthors(),tweet.getContent(),tweet.getTimestamp(),tweet.getLocation(),tweet.getTags(),tweet.getMentions());

                // create a producer record
                ProducerRecord<String, String> record =
                        new ProducerRecord<String, String>(topic, (twt.getLocation()) +"_"+ (twt.getTags()).isEmpty() +"_"+(twt.getMentions()).isEmpty(), ctx.body());

                System.out.println("post:  " + (twt.getLocation()) +"_"+ (twt.getTags()).isEmpty() +"_"+(twt.getMentions()).isEmpty());

                // send data - asynchronous
                producer.send(record, new Callback() {
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        // executes everytime a record is successfully sent or exception is thrown
                        if (e == null) {
                            // record sent successfully
                            logger.info("Received new metadata.\n" +
                                    "Topic: " + recordMetadata.topic() + "\n" +
                                    "Partition: " + recordMetadata.partition() + "\n" +
                                    "Offset: " + recordMetadata.offset() + "\n" +
                                    "Timestamp: " + recordMetadata.timestamp() + "\n"
                            );
                        } else {
                            logger.error("Error while producing " + e);
                        }

                    }
                }).get(); //block the send to make it synchronous - not done is prod
                // flush data
                producer.flush();
                // flush and close producer
                producer.close();

            } catch(Exception e) {
                ctx.result(new Gson().toJson("Tweet format is wrong"));
            }
        });

        app.get("/tweets/*/latest", ctx -> {
            System.out.println("getteyiz");
            if (userList.size() > 0) {
                int noRecordsCount = 0;
                int giveUp = 2;
                ArrayList<Tweet> finalToSend = new ArrayList<Tweet>();
                System.out.println("filter1:  " + ctx.splat(0));

                FilterClass filt =  new Gson().fromJson(ctx.splat(0),FilterClass.class);
                FilterClassBoolean filterClassBoolean = new FilterClassBoolean(filt);
                System.out.println("filter2:  " + filterClassBoolean.toString());
                User ercan = userList.get(userList.size() - 1);
                KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(ercan.getProperties());

                if(!filterClassBoolean.isFilterEmpty()) {
                    byte[] n = filterClassBoolean.toString().getBytes();
                    int partn = Utils.toPositive(Utils.murmur2(n)) % numPartitions;

                    TopicPartition topicPartition = new TopicPartition(topic, partn);
                    List<TopicPartition> topics = Arrays.asList(topicPartition);
                    consumer.assign(topics);
                }
                else{
                    TopicPartition topicPartition0 = new TopicPartition(topic, 0);
                    TopicPartition topicPartition1 = new TopicPartition(topic, 1);
                    TopicPartition topicPartition2 = new TopicPartition(topic, 2);
                    TopicPartition topicPartition3 = new TopicPartition(topic, 3);
                    TopicPartition topicPartition4 = new TopicPartition(topic, 4);
                    TopicPartition topicPartition5 = new TopicPartition(topic, 5);
                    TopicPartition topicPartition6 = new TopicPartition(topic, 6);
                    TopicPartition topicPartition7 = new TopicPartition(topic, 7);

                    List<TopicPartition> topics = Arrays.asList(topicPartition0,topicPartition1,topicPartition2,topicPartition3,topicPartition4,topicPartition5,topicPartition6,topicPartition7);
                    consumer.assign(topics);

                }
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        consumer.wakeup();
                    }
                });
                try {
                    while (true) {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100)); // new in Kafka 2.0.0
                        System.out.println(records.count());
                        for (ConsumerRecord<String, String> record : records) {
                            logger.info("Key: " + record.key() + ", Value: " + record.value());
                            logger.info("Partition: " + record.partition() + ", Offset:" + record.offset());

                            Tweet twt = new Gson().fromJson(record.value(), Tweet.class);

                            if (twt.getLocation().equals(filt.getLocation()) || filt.getLocation().isEmpty()) {
                                if (!finalToSend.contains(twt)) {
                                    finalToSend.add(twt);
                                }
                            }
                            for (int i = 0; i < filt.getTags().size(); i++) {
                                if (twt.getTags().contains(filt.getTags().get(i))) {
                                    if (!finalToSend.contains(twt)) {
                                        finalToSend.add(twt);
                                    }
                                }
                            }
                            for (int i = 0; i < filt.getMentions().size(); i++) {
                                if (twt.getMentions().contains(filt.getMentions().get(i))) {
                                    if (!finalToSend.contains(twt)) {
                                        finalToSend.add(twt);
                                    }
                                }
                            }
                            System.out.println((twt.getLocation()).isEmpty() +"_"+ (twt.getTags()).isEmpty() +"_"+(twt.getMentions()).isEmpty());

                        }

                        if ((records.count()==0)){
                            noRecordsCount++;
                            if (noRecordsCount > giveUp) break; //while(noRecordsCount<= giveUp)?
                            else continue;
                        }
                    }
                } catch (WakeupException e) {
                    // Do Nothing
                } finally {

                    consumer.assign(topicAssignment);
                    consumer.seekToEnd(topicAssignment);

                    for(int i=0; i<topicAssignment.size();i++) {
                        if(i == 6){

                        }
                        else{
                            long current = consumer.position(topicAssignment.get(i));
                            consumer.seek(topicAssignment.get(i), current);
                        }
                    }
                    consumer.close();
                    System.out.println("console closed");
                }

                ctx.result(new Gson().toJson(finalToSend));

            }
            else{
                ctx.result(new Gson().toJson("No user subscribed"));
            }
        });

        Queue<SseClientExtended> clientsExtended = new ConcurrentLinkedQueue<>();

        app.sse("/sse/*/", client -> {
            System.out.println(client.ctx.splat(0));
            FilterClass filt =  new Gson().fromJson(client.ctx.splat(0),FilterClass.class);

            SseClientExtended clientExtended = new SseClientExtended(client.ctx,filt);
            clientsExtended.add(clientExtended);
            clientExtended.onClose(()-> clientsExtended.remove(clientExtended));
        });

        ArrayList<Tweet> finalToSend = new ArrayList<Tweet>();
        ArrayList<Tweet> listToSend = new ArrayList<Tweet>();

        while (true) {


            for (SseClientExtended client : clientsExtended) {
                FilterClass filt =  client.getFilterC();
                if (userList.size() > 0) {

                    int noRecordsCount = 0;
                    int giveUp = 2;


                    User ercan = userList.get(userList.size() - 1);
                    KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(ercan.getProperties());
                    consumer.subscribe(Arrays.asList(topic));

                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        public void run() {
                            consumer.wakeup();
                        }
                    });
                    try {
                        while (true) {
                            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100)); // new in Kafka 2.0.0
                            System.out.println(records.count());
                            for (ConsumerRecord<String, String> record : records) {
                                logger.info("Key: " + record.key() + ", Value: " + record.value());
                                logger.info("Partition: " + record.partition() + ", Offset:" + record.offset());
                                Tweet twt = new Gson().fromJson(record.value(), Tweet.class);

                                long diff = (Instant.now().getEpochSecond() - Long.valueOf(twt.getTimestamp()).longValue());
                                if (diff<90) {
                                //filt =  new Gson().fromJson(client.ctx.splat(0),FilterClass.class);
                                if (twt.getLocation().equals(filt.getLocation()) || filt.getLocation().isEmpty()) {
                                    if (!listToSend.contains(twt)) {
                                        listToSend.add(twt);
                                    }
                                }

                                for (int i = 0; i < filt.getTags().size(); i++) {
                                    if (twt.getTags().contains(filt.getTags().get(i))) {
                                        if (!listToSend.contains(twt)) {
                                            listToSend.add(twt);
                                        }
                                    }
                                }
                                for (int i = 0; i < filt.getMentions().size(); i++) {
                                    if (twt.getMentions().contains(filt.getMentions().get(i))) {
                                        if (!listToSend.contains(twt)) {
                                            listToSend.add(twt);
                                        }
                                    }
                                }
                                Collections.sort(listToSend);
                                finalToSend = (ArrayList) listToSend.clone();
                                boolean endFor;
                                for (Tweet lts : listToSend) {
                                    endFor = false;
                                    if (!lts.getLocation().equals(filt.getLocation()) && !filt.getLocation().isEmpty()) {
                                        if (finalToSend.contains(lts)) {
                                            finalToSend.remove(lts);
                                            endFor = true;
                                        }
                                    }

                                    for (int i = 0; i < filt.getTags().size(); i++) {
                                        if ((!lts.getTags().contains(filt.getTags().get(i)) && !filt.getTags().isEmpty()) && !endFor) {
                                            if (finalToSend.contains(lts)) {
                                                finalToSend.remove(lts);
                                                endFor = true;
                                            }
                                        }
                                    }
                                    for (int i = 0; i < filt.getMentions().size(); i++) {
                                        if ((!lts.getMentions().contains(filt.getMentions().get(i)) && !filt.getMentions().isEmpty()) && !endFor) {
                                            if (finalToSend.contains(lts)) {
                                                finalToSend.remove(lts);
                                            }
                                        }
                                    }
                                }
                            }
                            }

                            if ((records.count() == 0)) {
                                noRecordsCount++;
                                if (noRecordsCount > giveUp) break;
                                else continue;
                            }

                        }

                    } catch (Exception e) {
                        // Do Nothing
                    } finally {
                        consumer.close();
                        System.out.println("console closed");
                    }

                }
                if(!finalToSend.isEmpty()){
                    for (int i = finalToSend.size() -1; i >= 0; i--) {
                        long olddiff = (Instant.now().getEpochSecond() - Long.valueOf(finalToSend.get(i).getTimestamp()).longValue());
                        System.out.println(olddiff);
                        if (olddiff>90) {
                            finalToSend.remove(i);
                        }
                    }
                    String json = new Gson().toJson(finalToSend);
                    System.out.println(json);
                    client.sendEvent("hi", json);
                }
            }
            TimeUnit.SECONDS.sleep(5);
        }
    }

}