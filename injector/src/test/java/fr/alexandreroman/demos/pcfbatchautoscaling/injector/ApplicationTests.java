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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
class ApplicationTests {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private Source source;
    @Autowired
    private MessageCollector messageCollector;

    @Test
    void contextLoads() {
    }

    @Test
    void testInjector() throws InterruptedException {
        final var expectedUpdates = 10;
        final var resp = restTemplate.getForEntity("/inject?n=10", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        final var messageQueue = messageCollector.forChannel(source.output());
        for (int i = 0; i < expectedUpdates; ++i) {
            final Message<?> msg = messageQueue.poll(1, TimeUnit.SECONDS);
            log.info("Received stock update: {}", msg.getPayload());
        }
        assertThat(messageQueue.size()).isZero();
    }

    @SpringBootApplication
    @EnableBinding(Source.class)
    public static class MyStreams {
        @Autowired
        private Source source;
    }
}
