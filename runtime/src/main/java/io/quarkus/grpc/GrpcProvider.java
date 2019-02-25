package io.quarkus.grpc;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.grpc.BindableService;
import io.grpc.ServerInterceptor;

/**
 * Provides access to all gRPC services and interceptors as defined by {@code @GrpcService} and {@code @GrpcInterceptor}
 *
 * @author Harald Pehl
 */
@ApplicationScoped
public class GrpcProvider {

    private final Instance<BindableService> services;
    private final Instance<ServerInterceptor> interceptors;

    @Inject
    public GrpcProvider(@GrpcService Instance<BindableService> services,
            @GrpcInterceptor Instance<ServerInterceptor> interceptors) {
        this.services = services;
        this.interceptors = interceptors;
    }

    public Instance<BindableService> getServices() {
        return services;
    }

    public Instance<ServerInterceptor> getInterceptors() {
        return interceptors;
    }
}
