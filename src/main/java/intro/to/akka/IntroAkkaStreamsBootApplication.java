package intro.to.akka;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.javadsl.AsPublisher;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.databind.Module;
import io.vavr.jackson.datatype.VavrModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ReactiveAdapterRegistry;

import static org.springframework.core.ReactiveTypeDescriptor.multiValue;


@SpringBootApplication
public class IntroAkkaStreamsBootApplication {

	public static final Logger LOGGER = LoggerFactory.getLogger(IntroAkkaStreamsBootApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(IntroAkkaStreamsBootApplication.class, args);
	}

	@Bean
	ActorSystem actorSystem() {
		return ActorSystem.create();
	}

	@Bean
	Materializer materializer(ActorSystem actorSystem) {
		return Materializer.createMaterializer(actorSystem);
	}

	@Bean
	InitializingBean registerSource(Materializer materializer, ReactiveAdapterRegistry registry) {
		return () -> {
			registry.registerReactiveType(
					multiValue(akka.stream.javadsl.Source.class, akka.stream.javadsl.Source::empty),
					source -> ((akka.stream.javadsl.Source<?, ?>) source)
							.runWith(
									akka.stream.javadsl.Sink.asPublisher(AsPublisher.WITH_FANOUT), materializer),
					Source::fromPublisher);

			registry.registerReactiveType(
					multiValue(akka.stream.scaladsl.Source.class, akka.stream.scaladsl.Source::empty),
					source -> ((akka.stream.scaladsl.Source<?, ?>) source)
							.runWith(akka.stream.scaladsl.Sink.asPublisher(true), materializer),
					akka.stream.scaladsl.Source::fromPublisher);
		};
	}

	@Bean
	public Module vavrModule() {
		return new VavrModule();
	}

}
