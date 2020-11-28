package intro.to.akka.demo2;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import io.vavr.collection.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;

import static akka.stream.javadsl.FramingTruncation.ALLOW;
import static io.vavr.API.Set;
import static io.vavr.API.println;
import static java.nio.file.StandardOpenOption.*;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;

public class BlueprintExample {

    private static final Logger logger = LoggerFactory.getLogger(BlueprintExample.class);

    private static final ActorSystem system = ActorSystem.create("BlueprintExample");

    static final Set<StandardOpenOption> outputOpenOptions = Set(CREATE, WRITE, APPEND).toJavaSet();

    public static final BiConsumer<IOResult, Throwable> printAndTerminateSystem = (f, e) -> {
        if (nonNull(e)) {
            e.printStackTrace();
        } else {
            logger.debug("Valeur matérialisée : " + f);
        }

        logger.debug("Arrêt du systeme : " + system.name());
        system.terminate();
    };

    public static void main(String[] args) throws FileNotFoundException {

        final Path inPath = ResourceUtils.getFile("classpath:current_inventory.csv").toPath();
        final Path outPath = Paths.get("src/main/resources/no_inventory.csv");

        final Source<ByteString, CompletionStage<IOResult>> fileSource = FileIO.fromPath(inPath);
        final Sink<ByteString, CompletionStage<IOResult>> fileSink = FileIO.toPath(outPath, outputOpenOptions);

        final Flow<String, List<String>, NotUsed> csvHandler = Flow.fromFunction(__ -> List.ofAll(asList(__.split(","))));

        final RunnableGraph<CompletionStage<IOResult>> lowInventoryFlow = fileSource
                .via(Framing.delimiter(ByteString.fromString("\n"), 128, ALLOW))
                .drop(1)
                .map(ByteString::utf8String)
                .via(csvHandler)
                .filter(list -> Integer.parseInt(list.get(1)) == 0)
                .map(list -> ByteString.fromString(list.mkString(",") + "\n"))
                .toMat(fileSink, Keep.right());

        final CompletionStage<IOResult> stage = lowInventoryFlow.run(Materializer.matFromSystem(system));
        stage.whenComplete(printAndTerminateSystem);
    }
}
