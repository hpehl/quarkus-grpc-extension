package org.jboss.shamrock.grpc;

import javax.enterprise.context.Dependent;

import org.jboss.jandex.DotName;
import org.jboss.shamrock.arc.deployment.AdditionalBeanBuildItem;
import org.jboss.shamrock.arc.deployment.BeanContainerBuildItem;
import org.jboss.shamrock.arc.deployment.BeanDefiningAnnotationBuildItem;
import org.jboss.shamrock.deployment.annotations.BuildProducer;
import org.jboss.shamrock.deployment.annotations.BuildStep;
import org.jboss.shamrock.deployment.annotations.Record;
import org.jboss.shamrock.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import org.jboss.shamrock.deployment.builditem.FeatureBuildItem;
import org.jboss.shamrock.deployment.builditem.ServiceStartBuildItem;
import org.jboss.shamrock.deployment.builditem.ShutdownContextBuildItem;
import org.jboss.shamrock.deployment.builditem.substrate.ReflectiveClassBuildItem;
import org.jboss.shamrock.deployment.builditem.substrate.SubstrateConfigBuildItem;

import static org.jboss.shamrock.deployment.annotations.ExecutionTime.RUNTIME_INIT;
import static org.jboss.shamrock.deployment.annotations.ExecutionTime.STATIC_INIT;

public class GrpcBuildStep {

    private static final DotName GRPC_SERVICE = DotName.createSimple(GrpcService.class.getName());
    private static final DotName GRPC_INTERCEPTOR = DotName.createSimple(GrpcInterceptor.class.getName());
    private static final DotName DEPENDENT = DotName.createSimple(Dependent.class.getName());

    GrpcConfig config;

    @BuildStep
    public void build(BuildProducer<FeatureBuildItem> feature,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            BuildProducer<SubstrateConfigBuildItem> substrateConfig,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            BuildProducer<BeanDefiningAnnotationBuildItem> beanDefinitions,
            BuildProducer<ExtensionSslNativeSupportBuildItem> extensionSslNativeSupport) {

        // Register gRPC feature
        feature.produce(new FeatureBuildItem("grpc"));

        // Setup reflections
        reflectiveClass.produce(new ReflectiveClassBuildItem(false, false,
                "io.netty.channel.socket.nio.NioServerSocketChannel"));

        // Configure Substrate
        substrateConfig.produce(SubstrateConfigBuildItem.builder()
                .addNativeImageSystemProperty("io.netty.noUnsafe", "true")
                .addNativeImageSystemProperty("io.netty.leakDetection.level", "DISABLED")
                .addRuntimeReinitializedClass("io.netty.handler.codec.http2.Http2CodecUtil")
                .addRuntimeInitializedClass("io.netty.handler.codec.http.HttpObjectEncoder")
                .addRuntimeInitializedClass("io.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder")
                .addRuntimeInitializedClass("io.netty.handler.codec.http2.DefaultHttp2FrameWriter")
                .addRuntimeInitializedClass("io.netty.handler.ssl.JdkNpnApplicationProtocolNegotiator")
                .addRuntimeInitializedClass("io.netty.handler.ssl.ReferenceCountedOpenSslEngine")
                .addRuntimeInitializedClass("io.netty.handler.ssl.util.ThreadLocalInsecureRandom")
                .build());

        // gRPC services and interceptors are @Dependent beans, because the gRPC services cannot be
        // proxied due to a a final method in the generated gRPC service code
        additionalBeans.produce(new AdditionalBeanBuildItem(false, GrpcProvider.class));
        beanDefinitions.produce(new BeanDefiningAnnotationBuildItem(GRPC_SERVICE, DEPENDENT, false));
        beanDefinitions.produce(new BeanDefiningAnnotationBuildItem(GRPC_INTERCEPTOR, DEPENDENT, false));

        // Indicates that this extension would like the SSL support to be enabled
        extensionSslNativeSupport.produce(new ExtensionSslNativeSupportBuildItem("grpc"));
    }

    @BuildStep
    @Record(STATIC_INIT)
    public void prepareServer(GrpcTemplate template) {
        template.prepareServer(config);
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

