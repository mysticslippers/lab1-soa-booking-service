package me.ifmo.booking_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class TicketServiceClientConfig {

    @Bean
    public RestClient ticketServiceRestClient(RestClient.Builder builder,
            @Value("${ticket-service.base-url}") String baseUrl,
            @Value("${ticket-service.connect-timeout}") Duration connectTimeout,
            @Value("${ticket-service.read-timeout}") Duration readTimeout
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        return builder.baseUrl(baseUrl).defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).requestFactory(factory).build();
    }
}
