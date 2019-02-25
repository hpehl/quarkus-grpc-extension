package io.quarkus.grpc;

import static io.quarkus.runtime.annotations.ConfigPhase.BUILD_AND_RUN_TIME_FIXED;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(phase = BUILD_AND_RUN_TIME_FIXED)
public final class GrpcConfig {

    /** The port of the gRPC server. */
    @ConfigItem(defaultValue = "8888")
    int port;

    /**
     * The permitted time (in ms) for new connections to complete negotiation handshakes before being killed.
     * If not set, defaults to 120000 (12s).
     */
    @ConfigItem(defaultValue = "120000")
    int handshakeTimeout;

    /** The maximum message size allowed to be received on the server. If not set, defaults to 4 MiB. */
    @ConfigItem(defaultValue = "4194304")
    int maxInboundMessageSize;

    /**
     * The maximum size of metadata allowed to be received. {@code Integer.MAX_VALUE} disables
     * the enforcement. If not set, defaults to 8 KiB.
     */
    @ConfigItem(defaultValue = "8192")
    int maxInboundMetadataSize;
}
