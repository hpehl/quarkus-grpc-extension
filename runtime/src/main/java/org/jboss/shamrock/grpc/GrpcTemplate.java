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

import java.io.IOException;
import java.util.logging.Logger;

import org.jboss.shamrock.runtime.annotations.Template;
import io.grpc.ServerBuilder;

/**
 * @author Harald Pehl
 */
@Template
public class GrpcTemplate {

    private static final Logger log = Logger.getLogger(GrpcTemplate.class.getName());

    public void startServer() throws IOException {
        ServerBuilder
                .forPort(8888)
                .build()
                .start();
        log.info("gRPC server started");
    }

    public void registerServices() {
        log.info("gRPC services registered");
    }
}
