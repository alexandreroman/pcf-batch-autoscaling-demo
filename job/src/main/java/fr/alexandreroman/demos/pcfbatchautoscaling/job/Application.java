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

package fr.alexandreroman.demos.pcfbatchautoscaling.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

import java.math.BigDecimal;

import static java.lang.Thread.sleep;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@EnableBinding(Sink.class)
@Slf4j
class BatchJob {
    @StreamListener(Sink.INPUT)
    void onStockUpdate(StockUpdateRequest req) throws Exception {
        log.info("Updating stock: {}={}", req.getStock(), req.getValue());
        // Simulate lengthy operation.
        sleep(50);
    }
}

@Data
@AllArgsConstructor
class StockUpdateRequest {
    private String stock;
    private BigDecimal value;
}
