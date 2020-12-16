package de.tse.predictivegrowth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Extend this service to build your REST API services.
 *
 * @param <T> the Retrofit interface for this service
 */
@Slf4j
public abstract class AbstractRestService<T> {

    private static final int TIMEOUT_DURATION = 60;

    private final JacksonConverterFactory jacksonConverterFactory;

    private final Class<T> type;

    private String baseUrl;

    private T restInterface;

    public AbstractRestService(final Class<T> type, final ObjectMapper objectMapper) {
        this.type = type;
        this.jacksonConverterFactory = JacksonConverterFactory.create(objectMapper);
    }

    /**
     * Gets the REST interface for the current context. The base URL is handed over to ensure
     * the REST interface is rerouted in case the URL changes. Needed to make the URL hot
     * swappable or get custom interfaces for multiple clients.
     *
     * @param baseUrl               The most current base URL of this REST service
     *
     * @return The REST interface.
     */
    public T getRestInterface(final String baseUrl) {
        synchronized (this) {
            if ((this.baseUrl == null) || !this.baseUrl.equalsIgnoreCase(baseUrl) || this.restInterface == null) {
                this.baseUrl = baseUrl;

                final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

                clientBuilder
                        .connectTimeout(TIMEOUT_DURATION, TimeUnit.SECONDS)
                        .readTimeout(TIMEOUT_DURATION, TimeUnit.SECONDS)
                        .addInterceptor(chain -> {
                            final Request originalRequest = chain.request();
                            final Request.Builder builder = originalRequest.newBuilder();
                            final Request newRequest = builder.build();
                            return chain.proceed(newRequest);
                        });

                final Retrofit gateway = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(this.jacksonConverterFactory)
                        .client(clientBuilder.build())
                        .build();

                this.restInterface = gateway.create(this.type);
            }
        }
        return this.restInterface;
    }
}
