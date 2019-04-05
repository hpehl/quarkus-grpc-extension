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
package io.quarkus.grpc.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import javax.enterprise.context.Dependent;
import javax.inject.Singleton;

import org.jboss.jandex.DotName;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.ServiceStartBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.deployment.builditem.substrate.ReflectiveClassBuildItem;
import io.quarkus.grpc.GrpcInterceptor;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.runtime.GrpcConfig;
import io.quarkus.grpc.runtime.GrpcProvider;
import io.quarkus.grpc.runtime.GrpcTemplate;

/**
 * Collects and registers all gRPC services and interceptors annotated with {@code @GrpcService} and
 * {@code @GrpcInterceptor}. Starts a gRPC server listening on port {@code quarkus.grpc.port} (defaults to 5050).
 */
public class GrpcBuildStep {

    private static final DotName GRPC_SERVICE = DotName.createSimple(GrpcService.class.getName());
    private static final DotName GRPC_INTERCEPTOR = DotName.createSimple(GrpcInterceptor.class.getName());
    private static final DotName DEPENDENT = DotName.createSimple(Dependent.class.getName());
    private static final DotName SINGLETON = DotName.createSimple(Singleton.class.getName());

    @BuildStep(providesCapabilities = "io.quarkus.grpc")
    public void build(BuildProducer<FeatureBuildItem> feature,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            BuildProducer<BeanDefiningAnnotationBuildItem> beanDefinitions,
            BuildProducer<ExtensionSslNativeSupportBuildItem> extensionSslNativeSupport) {

        // Register gRPC feature
        feature.produce(new FeatureBuildItem("grpc"));

        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(GrpcProvider.class)
                .setDefaultScope(SINGLETON)
                .setUnremovable()
                .build());
        // gRPC services and interceptors have to be @Dependent since gRPC services cannot be proxied
        // due to final methods in the generated gRPC service code
        beanDefinitions.produce(new BeanDefiningAnnotationBuildItem(GRPC_SERVICE, DEPENDENT, false));
        beanDefinitions.produce(new BeanDefiningAnnotationBuildItem(GRPC_INTERCEPTOR, DEPENDENT, false));

        // Indicates that this extension would like the SSL support to be enabled
        extensionSslNativeSupport.produce(new ExtensionSslNativeSupportBuildItem("grpc"));
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    // runtime and not static init, because the grpc config uses io.quarkus.runtime.configuration.ssl.ServerSslConfig
    // which requires a SSL protocol converter not available at static init time.
    public void prepareServer(GrpcTemplate template, GrpcConfig config, LaunchModeBuildItem launchMode)
            throws Exception {
        template.prepareServer(config, launchMode.getLaunchMode());
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    public ServiceStartBuildItem startServer(GrpcTemplate template, BeanContainerBuildItem beanContainer,
            ShutdownContextBuildItem shutdown) throws Exception {
        template.registerServices(beanContainer.getValue());
        template.registerInterceptors(beanContainer.getValue());
        template.startServer(shutdown);
        return new ServiceStartBuildItem("gRPC");
    }
}
