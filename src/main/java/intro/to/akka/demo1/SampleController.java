package intro.to.akka.demo1;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.stream.*;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import static akka.stream.javadsl.Keep.right;
import static io.vavr.API.List;
import static io.vavr.API.println;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;

@RestController
public class SampleController {

    private final Graph<FlowShape<Integer, Integer>, UniqueKillSwitch> killSwitchFlow = KillSwitches.single();

    private final Source<Integer, NotUsed> counter = Source.from(Stream.from(1)).throttle(1, ofMillis(3_000)).log("counter");

    private UniqueKillSwitch killSwitch;

    private final ActorSystem actorSystem;
    private final Materializer materializer;

    public SampleController(ActorSystem actorSystem, Materializer materializer) {
        this.actorSystem = actorSystem;
        this.materializer = materializer;
    }

    @RequestMapping("/")
    public Source<String, NotUsed> index() {
        // Ce code retourne 5 fois "Hello world!" entremélé avec 5 saut de lignes, soit 10 élèments
        return Source.repeat("Hello world!")
                        .intersperse("\n")
                        .take(10);
    }

    @RequestMapping("/list")
    public Source<String, NotUsed> list() {
        return Source.from(List("1", "2", "3")).intersperse(",");
    }

    /**
     * Retourne la String "tick" toutes les secondes.
     * @return
     */
    @RequestMapping("/tick")
    public Source<String, Cancellable> tick() {
        return Source.tick(
                ofSeconds(1), // delay of first tick
                ofSeconds(1), // delay of subsequent ticks
                "tick" // element emitted each tick
        );
    }

    @RequestMapping("/schedule")
    public void schedule() {
        println("==> schedule");
        final Sink<Integer, CompletionStage<Done>> sink = Sink.ignore();
        killSwitch = counter.viaMat(killSwitchFlow, right()).to(sink).run(materializer);
    }

    @RequestMapping("/stop")
    public void stop() {
        println("==> stop");
        killSwitch.shutdown();
    }
}
