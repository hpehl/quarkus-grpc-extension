package org.jboss.shamrock.grpc;

import org.jboss.shamrock.runtime.annotations.ConfigItem;
import org.jboss.shamrock.runtime.annotations.ConfigRoot;

@ConfigRoot
class GrpcConfig {

    /**
     * The port of the gRPC server
     */
    @ConfigItem(defaultValue = "8080")
    public int port;

    /**
     * The host of the gRPC server
     */
    @ConfigItem(defaultValue = "localhost")
    public String host;
}
