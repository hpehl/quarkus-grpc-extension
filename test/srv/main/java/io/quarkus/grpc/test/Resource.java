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

/**
 * Generic wrapper for stoppable resources.
 *
 * @param <T> resource type
 */
abstract class Resource<T> {

    private final T delegate;

    Resource(T delegate) {
        this.delegate = delegate;
    }

    T getDelegate() {
        return delegate;
    }

    abstract void shutdown();

    abstract void shutdownNow();

    abstract boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;

    @Override
    public String toString() {
        return delegate.toString();
    }
}
