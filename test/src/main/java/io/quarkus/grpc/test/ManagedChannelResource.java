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
package io.quarkus.grpc.test;

import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;

class ManagedChannelResource extends Resource<ManagedChannel> {

    ManagedChannelResource(ManagedChannel delegate) {
        super(delegate);
    }

    @Override
    void shutdown() {
        getDelegate().shutdown();
    }

    @Override
    void shutdownNow() {
        getDelegate().shutdownNow();
    }

    @Override
    boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return getDelegate().awaitTermination(timeout, unit);
    }
}
