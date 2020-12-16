package de.tse.predictivegrowth.config.data;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "alphavantage")
@Getter
@Setter
public class AlphaVantageData {

    private String baseUrl;

    private String apiKey;
}
