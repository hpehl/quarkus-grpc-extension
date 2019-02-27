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
package io.quarkus.build.grpc.runtime.graal;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import io.netty.channel.ChannelHandlerContext;

@TargetClass(className = "io.grpc.netty.ProtocolNegotiators")
final class Target_io_grpc_netty_ProtocolNegotiators {

    @Substitute
    static void logSslEngineDetails(Level level, ChannelHandlerContext ctx, String msg, Throwable t) {
        Logger log = Logger.getLogger("io.grpc.netty.ProtocolNegotiators");
        if (log.isLoggable(level)) {
            StringBuilder builder = new StringBuilder(msg).append("\nNo SSLEngine details available!");
            log.log(level, builder.toString(), t);
        }
    }
}

@TargetClass(className = "com.google.protobuf.UnsafeUtil")
final class Target_com_google_protobuf_UnsafeUtil {

    @Substitute
    private static sun.misc.Unsafe getUnsafe() {
        // since we configured Netty with io.netty.noUnsafe=true, this should be safe
        return null;
    }
}

class GrpcNettySubstitutions {
}
