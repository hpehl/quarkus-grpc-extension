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
package io.quarkus.grpc.runtime;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Template;
import org.jboss.logging.Logger;

/** Setup a gRPC server, register services and interceptors and finally start the server. */
@Template
public class GrpcTemplate {

    private static final Logger log = Logger.getLogger("io.quarkus.grpc");
    private static ServerBuilder<?> serverBuilder;

    public void prepareServer(GrpcConfig config, LaunchMode launchMode) throws Exception {
        int port = config.determinePort(launchMode);
        int sslPort = config.determineSslPort(launchMode);
        SSLContext context = config.ssl.toSSLContext();

        if (context != null) {
            log.warn("SSL not yet implemented!");
            // NYI
        } else {
            serverBuilder = ServerBuilder.forPort(port)
                    .handshakeTimeout(config.handshakeTimeout, TimeUnit.MILLISECONDS)
                    .maxInboundMessageSize(config.maxInboundMessageSize)
                    .maxInboundMetadataSize(config.maxInboundMetadataSize);
        }
    }

    public void registerServices(BeanContainer beanContainer) {
        GrpcProvider provider = beanContainer.instance(GrpcProvider.class);
        for (BindableService service : provider.getServices()) {
            serverBuilder.addService(service);
            log.infof("Registered gRPC service %s", service.getClass().getName());
        }
    }

    public void registerInterceptors(BeanContainer beanContainer) {
        GrpcProvider provider = beanContainer.instance(GrpcProvider.class);
        for (ServerInterceptor interceptor : provider.getInterceptors()) {
            serverBuilder.intercept(interceptor);
            log.infof("Registered gRPC interceptor %s", interceptor.getClass().getName());
        }
    }

    public void startServer(ShutdownContext shutdown) throws Exception {
        Server server = serverBuilder.build().start();
        log.infof("gRPC server listening on port %d", server.getPort());
        shutdown.addShutdownTask(server::shutdown);
    }
}
