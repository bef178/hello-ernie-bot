package helloerniebot.common;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Slf4j
public class HttpUtil {

    @SneakyThrows
    public static String httpGet(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(request);

            log.info("responseStatusLine: {}", response.getStatusLine());

            StringBuilder sb = new StringBuilder();
            Scanner scanner = new Scanner(response.getEntity().getContent());
            while (scanner.hasNext()) {
                sb.append(scanner.next());
            }
            return sb.toString();
        }
    }

    public static WebClient buildWebClient(String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 8000)
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(8000, TimeUnit.MILLISECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(8000, TimeUnit.MILLISECONDS));
                });

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> {
                            configurer.defaultCodecs().maxInMemorySize(-1);
                            configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder());
                            configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder());
                        })
                        .build())
                .build();
    }
}
