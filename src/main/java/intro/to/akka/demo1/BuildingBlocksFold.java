package intro.to.akka.demo1;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;

import static io.vavr.API.println;
import static java.util.Objects.nonNull;

public class BuildingBlocksFold {

    private static final ActorSystem system = ActorSystem.create("BuildingBlocksFold");

    public static final BiConsumer<Integer, Throwable> printAndTerminateSystem = (f, e) -> {
        if (nonNull(e)) {
            e.printStackTrace();
        } else {
            println("Valeur matérialisée : " + f);
            println("Arrêt du systeme : " + system.name());
            system.terminate();
        }
    };

    public static void main(String[] args) {

        final Source<Integer, NotUsed> source = Source.range(1, 5);
        //final Sink<Integer, CompletionStage<Done>> sink = Sink.foreach(API::println);

        final Sink<Integer, CompletionStage<Integer>> sink = Sink.fold(0, Integer::sum);

        final RunnableGraph<CompletionStage<Integer>> dataflow2 = source.toMat(sink, Keep.right());

        dataflow2.run(system).whenComplete(printAndTerminateSystem);
    }
}
