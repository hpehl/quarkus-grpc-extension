# Quarkus gRPC Extension

Extension to use [gRPC](https://grpc.io/) services and interceptors in your [Quarkus](https://quarkus.io) application. The extension picks up all services annotated with `@GrpcService` and interceptors marked with `@GrpcInterceptor`. The services and interceptors are registered against a gRPC server running on port `quarkus.grpc.port` (defaults to 5050).

## Getting Started

The gRPC extension is not available in Maven Central. For now you have to clone the repository and install the extension in your local maven repository. Then follow these steps to write and deploy a simple hello world gRPC service:

### Setup Project

Create a new project using the Quarkus [archetype](https://quarkus.io/guides/getting-started-guide#bootstrapping-the-project):

```bash
mvn io.quarkus:quarkus-maven-plugin:0.11.0:create \
    -DprojectGroupId=io.grpc.helloworld \
    -DprojectArtifactId=helloworld
``` 

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-grpc</artifactId>
    <version>${quarkus.grpc.version}</version>
    <scope>provided</scope>
</dependency>
```

You can remove the `quarkus-arc` and `quarkus-resteasy` dependencies from the generated `pom.xml` since they are not used in this demo.

### Setup gRPC

To setup the gRPC code generation, add the following settings to your `pom.xml`:

```xml
<build>
    <extensions>
        <extension>
            <groupId>kr.motd.maven</groupId>
            <artifactId>os-maven-plugin</artifactId>
            <version>1.5.0.Final</version>
        </extension>
    </extensions>
    <plugins>
        <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.5.1</version>
            <configuration>
                <protocArtifact>com.google.protobuf:protoc:3.5.1-1:exe:${os.detected.classifier}
                </protocArtifact>
                <pluginId>grpc-java</pluginId>
                <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.18.0:exe:${os.detected.classifier}
                </pluginArtifact>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>compile-custom</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    <plugins>
</build>
```

### Define gRPC Service

Create a service definition in `src/main/proto/helloworld.proto`

```proto
syntax = "proto3";

option java_multiple_files = true;
option java_package = "io.grpc.helloworld";
option java_outer_classname = "HelloWorldProto";

package helloworld;

service Greeter {
  rpc SayHello (HelloRequest) returns (HelloReply) {}
}

message HelloRequest {
  string name = 1;
}

message HelloReply {
  string message = 1;
}
```

### Implement Service

Write a service implementation in `src/main/java/io/grpc/helloworld/GreeterService.java` (you might want to execute `mvn compile` first to generate the gRPC code):

```java
package io.grpc.helloworld;

import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;

@GrpcService
public class GreeterService extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> response) {
        HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + request.getName()).build();
        response.onNext(reply);
        response.onCompleted();
    }
}
```

### Run

#### JVM Mode  

```bash
mvn package
java -jar target/hello-world-1.0-SNAPSHOT-runner.jar
```

#### Native Mode

```bash
mvn package -P native
./target/hello-world-1.0-SNAPSHOT-runner
```

## Quickstart

If you want to see a more complex example, checkout the [gRPC quickstart](https://github.com/hpehl/quarkus-grpc-quickstart). It uses both the [gRPC](https://github.com/hpehl/quarkus-grpc-extension) and the [gRPC client](https://github.com/hpehl/quarkus-grpc-client-extension) extension to implement the [route guide example](https://github.com/grpc/grpc-java/tree/v1.18.0/examples#grpc-examples) provided by [gRPC Java](https://github.com/grpc/grpc-java). 

## What's Missing

- TLS
- Better devmode support
- More configuration options

See also https://github.com/quarkusio/quarkus/issues/820

Have fun!
