/*
 * Copyright (c) 2019 Pivotal Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.alexandreroman.demos.pcfbatchautoscaling.injector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.MediaType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Random;

@SpringBootApplication
@EnableBinding(Source.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
class IndexController {
    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    String index() {
        final var injectUri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/inject").queryParam("n", 100).build().toUriString();
        return "Go to " + injectUri + " to start injection.";
    }
}

@RestController
@RequiredArgsConstructor
@Slf4j
class InjectorController {
    private static final String[] STOCKS = {
            "AAPL", "GOOGL", "AMZN", "MSFT", "FB", "VMW",
    };
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private final Random random = new Random();
    private final Source source;

    @GetMapping("/inject")
    String inject(@RequestParam(name = "n", required = false, defaultValue = "100") @Positive int numberOfUpdates) {
        log.info("Starting injection: {} updates", numberOfUpdates);
        try {
            for (int i = 0; i < numberOfUpdates; ++i) {
                final var stockCode = STOCKS[random.nextInt(STOCKS.length)];
                final var stockValue = BigDecimal.valueOf(random.nextInt(100000)).divide(ONE_HUNDRED);
                log.info("Injecting stock update: {}={}", stockCode, stockValue);
                source.output().send(MessageBuilder.withPayload(new StockUpdateRequest(stockCode, stockValue)).build());
            }
        } finally {
            log.info("Injection done");
        }
        return "Injection done";
    }
}

@Data
@AllArgsConstructor
class StockUpdateRequest {
    private String stock;
    private BigDecimal value;
}
