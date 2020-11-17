package de.tse.predictivegrowth.config.data;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "iexcloud")
@Getter
@Setter
public class IexCloudData {

    private String baseUrl;

    private String apiVersionUrl;

    private String publishableToken;

    private String secretToken;
}
