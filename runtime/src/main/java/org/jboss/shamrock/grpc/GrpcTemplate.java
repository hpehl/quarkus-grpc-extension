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
package org.jboss.shamrock.grpc;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import org.jboss.shamrock.runtime.RuntimeValue;
import org.jboss.shamrock.runtime.ShutdownContext;
import org.jboss.shamrock.runtime.annotations.Template;

/**
 * Setup and start a gRPC server.
 *
 * @author Harald Pehl
 */
@Template
public class GrpcTemplate {

    private static final Logger log = Logger.getLogger("org.jboss.shamrock.grpc");
    private static ServerBuilder<?> serverBuilder;

    public void prepareServer(GrpcConfig config) {
        serverBuilder = ServerBuilder.forPort(config.port)
                .handshakeTimeout(config.handshakeTimeout, TimeUnit.MILLISECONDS)
                .maxInboundMessageSize(config.maxInboundMessageSize)
                .maxInboundMetadataSize(config.maxInboundMetadataSize);
    }

    public void registerService(RuntimeValue<BindableService> serviceValue) {
        BindableService service = serviceValue.getValue();
        serverBuilder.addService(service);
        log.info("Registered gRPC service " + service.bindService().getServiceDescriptor().getName());
    }

    public void registerInterceptor(RuntimeValue<ServerInterceptor> interceptorValue) {
        ServerInterceptor interceptor = interceptorValue.getValue();
        serverBuilder.intercept(interceptor);
        log.info("Registered gRPC interceptor");
    }

    public void startServer(ShutdownContext shutdown) throws Exception {
        Server server = serverBuilder.build().start();
        log.info("gRPC server runnning on port " + server.getPort());
        shutdown.addShutdownTask(server::shutdown);
    }
}
