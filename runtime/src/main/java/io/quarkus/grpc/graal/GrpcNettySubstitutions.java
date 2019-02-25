package io.quarkus.grpc.graal;

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
