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

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.grpc.GrpcProvider;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Template;

/**
 * Setup a gRPC server, register services and interceptors and finally start the server.
 *
 * @author Harald Pehl
 */
@Template
public class GrpcTemplate {

    private static final Logger log = Logger.getLogger("io.quarkus.grpc");
    private static ServerBuilder<?> serverBuilder;

    public void prepareServer(GrpcConfig config) {
        serverBuilder = ServerBuilder.forPort(config.port)
                .handshakeTimeout(config.handshakeTimeout, TimeUnit.MILLISECONDS)
                .maxInboundMessageSize(config.maxInboundMessageSize)
                .maxInboundMetadataSize(config.maxInboundMetadataSize);
    }

    public void registerServices(BeanContainer beanContainer) {
        GrpcProvider provider = beanContainer.instance(GrpcProvider.class);
        for (BindableService service : provider.getServices()) {
            serverBuilder.addService(service);
            log.info("Registered gRPC service " + service.getClass().getName());
        }
    }

    public void registerInterceptors(BeanContainer beanContainer) {
        GrpcProvider provider = beanContainer.instance(GrpcProvider.class);
        for (ServerInterceptor interceptor : provider.getInterceptors()) {
            serverBuilder.intercept(interceptor);
            log.info("Registered gRPC interceptor " + interceptor.getClass().getName());
        }
    }

    public void startServer(ShutdownContext shutdown) throws Exception {
        Server server = serverBuilder.build().start();
        log.info("gRPC server listening on port " + server.getPort());
        shutdown.addShutdownTask(server::shutdown);
    }
}
