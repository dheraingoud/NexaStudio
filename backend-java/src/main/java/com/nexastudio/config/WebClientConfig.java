package com.nexastudio.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * WebClient configuration for all external API clients.
 * Uses a single NVIDIA API key for all NIM models.
 */
@Configuration
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    @Value("${gemini.api.base-url}")
    private String geminiBaseUrl;

    @Value("${gemini.api.timeout:120000}")
    private int geminiTimeout;

    @Value("${nvidia.api.base-url:https://integrate.api.nvidia.com/v1}")
    private String nvidiaBaseUrl;

    @Value("${nvidia.api.key:}")
    private String nvidiaApiKey;

    @Value("${nvidia.step.timeout:60000}")
    private int stepTimeout;

    @Value("${nvidia.minimax.timeout:90000}")
    private int minimaxTimeout;

    @Value("${nvidia.kimi.timeout:190000}")
    private int kimiTimeout;

    /**
     * Gemini API WebClient (fallback provider)
     */
    @Bean(name = "geminiWebClient")
    public WebClient geminiWebClient() {
        return buildWebClient(geminiBaseUrl, null, geminiTimeout);
    }

    /**
     * Step-3.5-Flash WebClient (primary — fast)
     */
    @Bean(name = "stepWebClient")
    public WebClient stepWebClient() {
        return buildNvidiaWebClient(stepTimeout);
    }

    /**
     * MiniMax M2.5 WebClient (secondary — powerful reasoning)
     */
    @Bean(name = "minimaxWebClient")
    public WebClient minimaxWebClient() {
        return buildNvidiaWebClient(minimaxTimeout);
    }

    /**
     * Kimi K2.5 WebClient (tertiary — thorough)
     */
    @Bean(name = "kimiWebClient")
    public WebClient kimiWebClient() {
        return buildNvidiaWebClient(kimiTimeout);
    }

    /**
     * General purpose WebClient
     */
    @Bean(name = "defaultWebClient")
    public WebClient defaultWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(30));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private WebClient buildNvidiaWebClient(int timeoutMs) {
        return buildWebClient(nvidiaBaseUrl, nvidiaApiKey, timeoutMs);
    }

    private WebClient buildWebClient(String baseUrl, String bearerToken, int timeoutMs) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(timeoutMs))
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000);

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024))
                .build();

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        if (bearerToken != null && !bearerToken.isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
            log.debug("WebClient configured with Bearer token");
        }

        return builder.build();
    }
}
