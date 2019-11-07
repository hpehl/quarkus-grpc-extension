package io.quarkus.grpc.runtime;

import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;

/** Setup a gRPC server, register services and interceptors and finally start the server. */
@Recorder
public class GrpcRecorder {

    private static final Logger log = Logger.getLogger("io.quarkus.grpc");
    private static ServerBuilder<?> serverBuilder;

    public void prepareServer(GrpcConfig config, LaunchMode launchMode) {
        int port = config.determinePort(launchMode);

        serverBuilder = ServerBuilder.forPort(port)
                .handshakeTimeout(config.handshakeTimeout, TimeUnit.MILLISECONDS)
                .maxInboundMessageSize(config.maxInboundMessageSize)
                .maxInboundMetadataSize(config.maxInboundMetadataSize);
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
