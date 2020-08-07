package io.pravega.retentionpolicy;

import io.pravega.client.ClientConfig;
import io.pravega.client.stream.impl.ControllerImpl;
import io.pravega.client.stream.impl.ControllerImplConfig;
import io.pravega.common.concurrent.Futures;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * This application is intended to run periodically to enforce certain Pravega stream retention policies.
 * It performs the following functions:
 *   1. All streams listed in the environment variable ROLLOVER_STREAMS are rolled over.
 *      This means that all active segments are sealed and they are replaced with
 *      new segments, using the same key ranges.
 *      If the streams have been truncated, either manually or due to the stream's retention policy,
 *      any unused segments will be deleted from the long-term storage, thus reclaiming disk space.
 */
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
