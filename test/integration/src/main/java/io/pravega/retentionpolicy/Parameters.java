package io.pravega.retentionpolicy;

import io.pravega.client.stream.Stream;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * All parameters will come from environment variables.
 */
@Slf4j
class Parameters {
    public static URI getControllerURI() {
        return URI.create(getEnvVar("PRAVEGA_CONTROLLER", "tcp://localhost:9090"));
    }

    /**
     * Environment variable ROLLOVER_STREAMS should be a comma-separated list of scope/stream.
     */
    public static List<Stream> getRolloverStreams() {
        final String s = getEnvVar("ROLLOVER_STREAMS", "");
        if (s.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(s.split(",")).map(Stream::of).collect(Collectors.toList());
    }

    private static String getEnvVar(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }
}
