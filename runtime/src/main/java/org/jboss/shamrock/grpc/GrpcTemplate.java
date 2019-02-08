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

import java.util.logging.Logger;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.jboss.shamrock.runtime.RuntimeValue;
import org.jboss.shamrock.runtime.ShutdownContext;
import org.jboss.shamrock.runtime.annotations.Template;

/**
 * Starts a gRPC server and registers gRPC services annotated with {@code @GrpcService}.
 *
 * @author Harald Pehl
 */
@Template
public class GrpcTemplate {

    private static final Logger log = Logger.getLogger("org.jboss.shamrock.grpc");
    private static ServerBuilder<?> serverBuilder;

    public void prepareServer(int port) {
        serverBuilder = ServerBuilder.forPort(port);
    }

    public void registerService(RuntimeValue<BindableService> serviceValue) {
        BindableService service = serviceValue.getValue();
        serverBuilder.addService(service);
        log.info("Registered gRPC service " + service.bindService().getServiceDescriptor().getName());
    }

    public void startServer(ShutdownContext shutdown) throws Exception {
        Server server = serverBuilder.build().start();
        log.info("Started gRPC server");
        shutdown.addShutdownTask(server::shutdown);
    }
}
