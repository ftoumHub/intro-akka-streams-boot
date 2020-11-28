package intro.to.akka.demo1;

import akka.NotUsed;
import akka.actor.Cancellable;
import akka.stream.javadsl.Source;
import io.vavr.collection.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

import static io.vavr.API.List;

@RestController
public class SampleController {

    @RequestMapping("/")
    public Source<String, NotUsed> index() {
        // Ce code retourne 5 fois "Hello world!" entremélé avec 5 saut de lignes, soit 10 élèments
        return Source.repeat("Hello world!")
                        .intersperse("\n")
                        .take(10);
    }

    @RequestMapping("/list")
    public Source<String, NotUsed> list() {
        final List<String> list = List("1", "2", "3");
        return Source.from(list)
                .intersperse(",");
    }

    @RequestMapping("/tick")
    public Source<String, Cancellable> tick() {
        return Source.tick(
                Duration.ofSeconds(1), // delay of first tick
                Duration.ofSeconds(1), // delay of subsequent ticks
                "tick" // element emitted each tick
        );
    }

}
