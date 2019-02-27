/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkus.build.grpc.runtime;

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
