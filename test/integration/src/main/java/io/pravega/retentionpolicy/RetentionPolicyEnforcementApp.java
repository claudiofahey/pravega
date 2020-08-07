package io.pravega.retentionpolicy;

import io.pravega.client.ClientConfig;
import io.pravega.client.stream.impl.ControllerImpl;
import io.pravega.client.stream.impl.ControllerImplConfig;
import io.pravega.common.concurrent.Futures;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@Slf4j
public class RetentionPolicyEnforcementApp {
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(5);

    public static void main(String[] args) throws Exception {
        final ClientConfig clientConfig = ClientConfig.builder().controllerURI(Parameters.getControllerURI()).build();
        try (final ControllerImpl controller = new ControllerImpl(ControllerImplConfig.builder().clientConfig(clientConfig).build(), EXECUTOR)) {
            Futures.allOf(Parameters.getRolloverStreams().stream().map(stream -> {
                log.info("Rolling over stream {}", stream.getScopedName());
                return controller.rollOver(stream.getScope(), stream.getStreamName(), EXECUTOR)
                        .thenAccept(newSegments -> {
                            log.info("Scale operation submitted for stream {}", stream.getScopedName());
                            log.debug("newSegments={}", newSegments);
                        });
            }).collect(Collectors.toList())).get();
            log.info("Done.");
        }
        EXECUTOR.shutdown();
    }
}
