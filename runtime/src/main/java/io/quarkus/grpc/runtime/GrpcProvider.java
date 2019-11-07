package io.quarkus.grpc.runtime;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.quarkus.grpc.GrpcInterceptor;
import io.quarkus.grpc.GrpcService;

/**
 * Provides access to all gRPC services and interceptors as defined by {@code @GrpcService} and {@code @GrpcInterceptor}
 */
public class GrpcProvider {

    @Inject
    @GrpcService
    Instance<BindableService> services;

    @Inject
    @GrpcInterceptor
    Instance<ServerInterceptor> interceptors;

    public Instance<BindableService> getServices() {
        return services;
    }

    public Instance<ServerInterceptor> getInterceptors() {
        return interceptors;
    }
}
