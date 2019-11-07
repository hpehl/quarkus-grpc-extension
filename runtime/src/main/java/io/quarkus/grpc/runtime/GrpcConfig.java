package io.quarkus.grpc.runtime;

import static io.quarkus.runtime.annotations.ConfigPhase.RUN_TIME;

import javax.net.ssl.SSLContext;

import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

/** gRPC configuration. */
@ConfigRoot(phase = RUN_TIME)
public final class GrpcConfig {

    /** The port */
    @ConfigItem(defaultValue = "5050")
    public int port;

    /** The secure port */
    @ConfigItem(defaultValue = "5443")
    public int sslPort;

    /** The port used to run tests */
    @ConfigItem(defaultValue = "5051")
    public int testPort;

    /** The secure port used to run tests */
    @ConfigItem(defaultValue = "5444")
    public int testSslPort;

    /**
     * The permitted time (in ms) for new connections to complete negotiation handshakes before being killed.
     * If not set, defaults to 120000 (12s).
     */
    @ConfigItem(defaultValue = "120000")
    public int handshakeTimeout;

    /** The maximum message size allowed to be received on the server. If not set, defaults to 4 MiB. */
    @ConfigItem(defaultValue = "4194304")
    public int maxInboundMessageSize;

    /**
     * The maximum size of metadata allowed to be received. {@code Integer.MAX_VALUE} disables
     * the enforcement. If not set, defaults to 8 KiB.
     */
    @ConfigItem(defaultValue = "8192")
    public int maxInboundMetadataSize;

    public int determinePort(LaunchMode launchMode) {
        return launchMode == LaunchMode.TEST ? testPort : port;
    }

    public int determineSslPort(LaunchMode launchMode) {
        return launchMode == LaunchMode.TEST ? testSslPort : sslPort;
    }

    public SSLContext sslContext() {
        // not implemented
        return null;
    }
}
