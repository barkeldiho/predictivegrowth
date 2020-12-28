package de.tse.predictivegrowth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Predictive Growth API",
                version = "0.3.0",
                description = "This app provides the backend for the predictive growth frontend."
                )
        )
@Configuration
public class OpenApiConfig {
}
