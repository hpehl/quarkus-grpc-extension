package io.quarkus.grpc.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import java.util.Collection;

import javax.enterprise.context.Dependent;
import javax.inject.Singleton;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import com.google.protobuf.GeneratedMessageV3;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.ServiceStartBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.grpc.GrpcInterceptor;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.runtime.GrpcConfig;
import io.quarkus.grpc.runtime.GrpcProvider;
import io.quarkus.grpc.runtime.GrpcRecorder;

/**
 * Collects and registers all gRPC services and interceptors annotated with {@code @GrpcService} and
 * {@code @GrpcInterceptor}. Starts a gRPC server listening on port {@code quarkus.grpc.port} (defaults to 5050).
 */
public class GrpcBuildStep {

    private static final Logger log = Logger.getLogger("io.quarkus.grpc");

    private static final DotName BUILDER = DotName.createSimple(GeneratedMessageV3.Builder.class.getName());
    private static final DotName DEPENDENT = DotName.createSimple(Dependent.class.getName());
    private static final DotName GENERATED_MESSAGE_V3 = DotName.createSimple(GeneratedMessageV3.class.getName());
    private static final DotName GRPC_INTERCEPTOR = DotName.createSimple(GrpcInterceptor.class.getName());
    private static final DotName GRPC_SERVICE = DotName.createSimple(GrpcService.class.getName());
    private static final DotName SINGLETON = DotName.createSimple(Singleton.class.getName());

    @BuildStep(providesCapabilities = "io.quarkus.grpc")
    public void build(CombinedIndexBuildItem combinedIndex,
            BuildProducer<FeatureBuildItem> feature,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            BuildProducer<BeanDefiningAnnotationBuildItem> beanDefinitions,
            BuildProducer<ExtensionSslNativeSupportBuildItem> extensionSslNativeSupport) {

        // generated messages use reflection!
        Collection<ClassInfo> messages = combinedIndex.getIndex().getAllKnownSubclasses(GENERATED_MESSAGE_V3);
        for (ClassInfo message : messages) {
            log.debugf("Register %s for reflection", message.name());
            reflectiveClass.produce(new ReflectiveClassBuildItem(true, true, true, message.name().toString()));
        }
        Collection<ClassInfo> builders = combinedIndex.getIndex().getAllKnownSubclasses(BUILDER);
        for (ClassInfo builder : builders) {
            log.debugf("Register %s for reflection", builder.name());
            reflectiveClass.produce(new ReflectiveClassBuildItem(true, true, true, builder.name().toString()));
        }

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
    public void prepareServer(GrpcRecorder recorder, GrpcConfig config, LaunchModeBuildItem launchMode) {
        recorder.prepareServer(config, launchMode.getLaunchMode());
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    public ServiceStartBuildItem startServer(GrpcRecorder recorder, BeanContainerBuildItem beanContainer,
            ShutdownContextBuildItem shutdown) throws Exception {
        recorder.registerServices(beanContainer.getValue());
        recorder.registerInterceptors(beanContainer.getValue());
        recorder.startServer(shutdown);
        return new ServiceStartBuildItem("gRPC");
    }
}
