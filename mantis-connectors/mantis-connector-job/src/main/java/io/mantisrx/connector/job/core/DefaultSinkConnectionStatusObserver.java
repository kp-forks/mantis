/*
 * Copyright 2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mantisrx.connector.job.core;

import java.util.concurrent.atomic.AtomicLong;

import io.mantisrx.client.SinkConnectionsStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultSinkConnectionStatusObserver implements SinkConnectionStatusObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSinkConnectionStatusObserver.class);
    private static final SinkConnectionStatusObserver INSTANCE = new DefaultSinkConnectionStatusObserver();

    private final AtomicLong numConnected = new AtomicLong();
    private final AtomicLong total = new AtomicLong();
    private final AtomicLong receivingData = new AtomicLong();

    public static synchronized SinkConnectionStatusObserver getInstance(boolean singleton) {
        if (singleton) {
            return INSTANCE;
        } else {
            return new DefaultSinkConnectionStatusObserver();
        }
    }

    public static SinkConnectionStatusObserver getInstance() {
        boolean singleton = true;
        return getInstance(singleton);
    }

    @Override
    public void onCompleted() {
        LOGGER.error("SinkConnectionStatusObserver completed!");

    }

    @Override
    public void onError(Throwable e) {
        LOGGER.error("Got Error", e);

    }

    @Override
    public void onNext(SinkConnectionsStatus t) {
        LOGGER.info("Got SinkConnectionStatus update " + t);
        numConnected.set(t.getNumConnected());
        total.set(t.getTotal());
        receivingData.set(t.getRecevingDataFrom());
    }

    @Override
    public long getConnectedServerCount() {
        return numConnected.get();
    }

    @Override
    public long getTotalServerCount() {
        return total.get();
    }

    @Override
    public long getReceivingDataCount() {
        return receivingData.get();
    }

    @Override
    public boolean isConnectedToAllSinks() {
        if (receivingData.get() > 0
                && numConnected.get() > 0
                && total.get() > 0
                && ((numConnected.get() == total.get()) && (total.get() == receivingData.get()))) {
            return true;
        }

        LOGGER.warn("NOT connected to all sinks "
                + " connected : " + numConnected.get()
                + " total " + total.get()
                + " receiving Data " + receivingData.get());

        return false;
    }

}
