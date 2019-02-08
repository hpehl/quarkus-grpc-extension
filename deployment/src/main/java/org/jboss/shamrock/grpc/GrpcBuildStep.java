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

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.BindableService;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.shamrock.deployment.annotations.BuildStep;
import org.jboss.shamrock.deployment.annotations.Record;
import org.jboss.shamrock.deployment.builditem.CombinedIndexBuildItem;
import org.jboss.shamrock.deployment.builditem.FeatureBuildItem;
import org.jboss.shamrock.deployment.builditem.ServiceStartBuildItem;
import org.jboss.shamrock.deployment.builditem.ShutdownContextBuildItem;
import org.jboss.shamrock.deployment.recording.RecorderContext;
import org.jboss.shamrock.runtime.RuntimeValue;
import org.jboss.shamrock.runtime.annotations.ConfigItem;
import org.jboss.shamrock.runtime.annotations.ConfigRoot;

import static org.jboss.shamrock.deployment.annotations.ExecutionTime.RUNTIME_INIT;
import static org.jboss.shamrock.deployment.annotations.ExecutionTime.STATIC_INIT;

/**
 * @author Harald Pehl
 */
public class GrpcBuildStep {

    @ConfigRoot
    static final class GrpcConfig {

        /** The port of the gRPC server. */
        @ConfigItem(defaultValue = "8888")
        int port;
    }


    private static final DotName GRPC_SERVICE = DotName.createSimple(GrpcService.class.getName());
    private static final Logger log = Logger.getLogger("org.jboss.shamrock.grpc");

    GrpcConfig config;

    @BuildStep
    @Record(STATIC_INIT)
    public void prepareServer(GrpcTemplate template) {
        template.prepareServer(config.port);
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    public ServiceStartBuildItem registerServices(CombinedIndexBuildItem indexBuildItem, RecorderContext context,
            ShutdownContextBuildItem shutdown, GrpcTemplate template) throws Exception {
        Collection<AnnotationInstance> serviceAnnotations = indexBuildItem.getIndex().getAnnotations(GRPC_SERVICE);
        for (AnnotationInstance serviceAnnotation : serviceAnnotations) {
            String className = serviceAnnotation.target().asClass().toString();
            log.log(Level.FINE, "Found gRPC service " + className);
            RuntimeValue<BindableService> serviceInstance = context.newInstance(className);
            template.registerService(serviceInstance);
        }
        template.startServer(shutdown);
        return new ServiceStartBuildItem("gRPC");
    }

    @BuildStep
    public FeatureBuildItem feature() {
        return new FeatureBuildItem("grpc");
    }
}
