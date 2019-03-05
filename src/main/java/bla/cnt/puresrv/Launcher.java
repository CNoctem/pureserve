package bla.cnt.puresrv;

import bla.cnt.puresrv.messaging.Emcue;
import bla.cnt.puresrv.messaging.Message;
import bla.cnt.puresrv.messaging.Subscriber;
import bla.cnt.puresrv.messaging.Topic;
import bla.cnt.puresrv.service.Server;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class Launcher {

    public static void main(String[] args) {
        createPublisher();
        createSubscriber();
        createEmcueWatcher();
    }

    private static void createEmcueWatcher() {
        Server s = Server.serverAt(8002);
        StringBuilder emcueLog = new StringBuilder();
        Emcue.INSTANCE.addListener(topic -> topic.subscribe((t, msg) ->
            emcueLog
                    .append("[")
                    .append(Instant.now())
                    .append("]|")
                    .append(t.getName())
                    .append("|: ")
                    .append(msg.getContent())
                    .append("<br>").append("\n")));

        s.registerEndpoint("/watch", ep -> {
            Map<String, Object> args = new HashMap<>();
            args.put("log", emcueLog);
            ep.sendFile("EmcueWatcher.html", args);
        });
    }

    private static void createSubscriber() {
        Server s = Server.serverAt(8001);

        Subscriber sub = (t, message) ->
                System.out.printf("Server at %s received message from %s: %s\n", 8001, t, message.getContent());

        s.registerEndpoint("/subscriber", ep -> ep.sendFile("subscriber.html"));

        s.registerEndpoint("/subscribe", ep -> {
            String topicName = ep.getParam("topicname");
            Topic t = Emcue.INSTANCE.createTopicIfAbsent(topicName);
            t.subscribe(sub);

            ep.redirect("subscriber");
        });
    }

    private static void createPublisher() {
        Server s1 = Server.serverAt(8000);

        s1.registerEndpoint("/act", ep -> ep
                .setResponse("For the sake of brevity... " + Instant.now())
                .send());

        s1.registerEndpoint("/publisher", ep -> ep.sendFile("publisher.html"));
        s1.registerEndpoint("/publish", ep -> {
            String topicName = ep.getParam("topicname");
            String msg = ep.getParam("message");
            String response = "Publishing...";

            if (topicName == null) {
                ep.setResponse("Topicname is empty. Cannot publish.");
                ep.send();
                return;
            }

            Emcue.INSTANCE.getTopicByName(topicName).publish(new Message<>().setContent(msg));
            ep.redirect("publisher");
        });
    }


}
