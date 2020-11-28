package intro.to.akka.demo1;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import io.vavr.API;

import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;

import static io.vavr.API.println;
import static java.util.Objects.nonNull;

public class BuildingBlocks {

    private static final ActorSystem system = ActorSystem.create("BuildingBlocks");

    public static final BiConsumer<Done, Throwable> printAndTerminateSystem = (f, e) -> {
        if (nonNull(e)) {
            e.printStackTrace();
        } else {
            println("Arrêt du systeme : " + system.name());
            system.terminate();
        }
    };

    public static void main(String[] args) {

        // Le premier type de la Source représente un entier (Integer) qui va être envoyé dans le flux descendant.
        // Cela signifie que cette Source ne peut être associée qu'à un composant descendant acceptant les éléments de
        // type entier en entrée.
        // The first type on the Source represents the Integer elements that will be sent downstream. This means that
        // this Source can only be hooked into a downstream component that accepts Integer elements as its input.
        // The second type on the Source is a specialized type called NotUsed, which is basically a glorified unit.
        final Source<Integer, NotUsed> source = Source.range(1, 5);
        // The Sink that is created here is purely side effecting, performing a loop over a stream of Integer elements
        // that were sent downstream to it. The Integer type param specifies that the Sink can only be hooked into an
        // upstream component that is emitting Integer elements downstream, making it a good fit for the Source created
        // on the line above it. The second type on the Sink represents the materialized value for the Sink, which, in
        // this case, is CompletionStage<Done>. The CompletableFuture there will be completed when the Sink has finished
        // processing all of its elements, with the Done type also being a glorified unit.
        final Sink<Integer, CompletionStage<Done>> sink = Sink.foreach(API::println);

        final CompletionStage<Done> dataflow = source.runWith(sink, system);

        dataflow.whenComplete(printAndTerminateSystem);
    }
}
