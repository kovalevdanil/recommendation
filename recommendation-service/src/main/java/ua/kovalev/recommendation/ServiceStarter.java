package ua.kovalev.recommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ua.kovalev.recommendation.config.properties.ModelInitializerProperties;
import ua.kovalev.recommendation.config.properties.ModelProperties;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableConfigurationProperties({ModelInitializerProperties.class, ModelProperties.class})
public class ServiceStarter {

	public static void main(String[] args) {
		SpringApplication.run(ServiceStarter.class, args);
	}

}
