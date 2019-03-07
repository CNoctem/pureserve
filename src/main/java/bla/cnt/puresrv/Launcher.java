package bla.cnt.puresrv;

import bla.cnt.puresrv.emcue.Emcue;
import bla.cnt.puresrv.emcue.Message;
import bla.cnt.puresrv.emcue.Subscriber;
import bla.cnt.puresrv.emcue.Topic;
import bla.cnt.puresrv.service.Server;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class Launcher {

    private static StringBuilder emcueLog = new StringBuilder();

    public static void main(String[] args) {
        createPublisher();
        createSubscriber();
        createEmcueWatcher();
    }

    private static void createEmcueWatcher() {
        Server s = Server.serverAt(8002);
        Emcue.EMCUE_ONE.subscribe((t, m) -> {
            log(t, m);
            if (m.getType() == Message.Type.TOPIC_CREATED) {
                Topic createdTopic = Emcue.INSTANCE.byName(m.getPayload().toString());
                createdTopic.subscribe(Launcher::log);
            }

        });

        s.registerEndpoint("/watch", ep -> {
            Map<String, Object> args = new HashMap<>();
            args.put("log", emcueLog);
            ep.sendFile("EmcueWatcher.html", args);
        });
    }

    private static void createSubscriber() {
        Server s = Server.serverAt(8001);

        Subscriber sub = (t, message) ->
                System.out.printf("Server at %s received message from %s: %s\n", 8001, t, message.getPayload());

        s.registerEndpoint("/subscriber", ep -> ep.sendFile("subscriber.html"));

        s.registerEndpoint("/subscribe", ep -> {
            String topicName = ep.getParam("topicname");
            Topic t = Emcue.INSTANCE.createOrGet(topicName);
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

            Emcue.INSTANCE.createOrGet(topicName).publish(Message.Type.SIMPLE.create(msg));
            ep.redirect("publisher");
        });
    }

    private static void log(Topic t, Message m) {
        emcueLog
                .append("[")
                .append(Instant.now())
                .append("]|")
                .append(t.getName())
                .append("|: ")
                .append(m.getPayload())
                .append("<br>").append("\n");
    }

}
