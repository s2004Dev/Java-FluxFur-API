/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JFA contributors
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

package lonter.jfa.api.utils;

import com.neovisionaries.ws.client.OpeningHandshakeException;
import lonter.jfa.api.JFA;
import lonter.jfa.api.exceptions.InvalidTokenException;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.RestRateLimiter;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.utils.JFALogger;
import org.slf4j.Logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

/**
 * Simple implementation of {@link SessionController} without supporting concurrency.
 *
 * @see ConcurrentSessionController
 */
public class SessionControllerAdapter implements SessionController {
    protected static final Logger log = JFALogger.getLog(SessionControllerAdapter.class);
    protected final Object lock = new Object();
    protected Queue<SessionConnectNode> connectQueue;
    protected RestRateLimiter.GlobalRateLimit globalRatelimit;
    protected Thread workerHandle;
    protected long lastConnect = 0;

    public SessionControllerAdapter() {
        connectQueue = new ConcurrentLinkedQueue<>();
        globalRatelimit = RestRateLimiter.GlobalRateLimit.create();
    }

    @Override
    public void appendSession(@NotNull SessionConnectNode node) {
        removeSession(node);
        connectQueue.add(node);
        runWorker();
    }

    @Override
    public void removeSession(@NotNull SessionConnectNode node) {
        connectQueue.remove(node);
    }

    @NotNull
    @Override
    public RestRateLimiter.GlobalRateLimit getRateLimitHandle() {
        return globalRatelimit;
    }

    @NotNull
    @Override
    public String getGateway(@NotNull JFA api) {
        Route.CompiledRoute route = Route.Misc.GATEWAY_BOT.compile();
        return new RestActionImpl<String>(
                        api, route, (response, request) -> response.getObject().getString("url"))
                .priority()
                .complete();
    }

    @NotNull
    @Override
    public ShardedGateway getShardedGateway(@NotNull JFA api) {
        return new RestActionImpl<ShardedGateway>(api, Route.Misc.GATEWAY_BOT.compile()) {
            @Override
            public void handleResponse(@NotNull Response response, @NotNull Request<ShardedGateway> request) {
                if (response.isOk()) {
                    DataObject object = response.getObject();

                    String url = object.getString("url");
                    int shards = object.getInt("shards");
                    int concurrency = object.getObject("session_start_limit").getInt("max_concurrency", 1);

                    request.onSuccess(new ShardedGateway(url, shards, concurrency));
                } else if (response.code == 401) {
                    api.shutdownNow();
                    request.onFailure(new InvalidTokenException());
                } else {
                    request.onFailure(response);
                }
            }
        }.priority().complete();
    }

    protected void runWorker() {
        synchronized (lock) {
            if (workerHandle == null) {
                workerHandle = new QueueWorker();
                workerHandle.start();
            }
        }
    }

    protected class QueueWorker extends Thread {
        /** Delay (in milliseconds) to sleep between connecting sessions */
        protected final long delay;

        public QueueWorker() {
            this(IDENTIFY_DELAY);
        }

        /**
         * Creates a QueueWorker
         *
         * @param delay
         *        delay (in seconds) to wait between starting sessions
         */
        public QueueWorker(int delay) {
            this(TimeUnit.SECONDS.toMillis(delay));
        }

        /**
         * Creates a QueueWorker
         *
         * @param delay
         *        delay (in milliseconds) to wait between starting sessions
         */
        public QueueWorker(long delay) {
            super("SessionControllerAdapter-Worker");
            this.delay = delay;
            super.setUncaughtExceptionHandler(this::handleFailure);
        }

        protected void handleFailure(Thread thread, Throwable exception) {
            log.error("Worker has failed with throwable!", exception);
        }

        @Override
        public void run() {
            try {
                if (this.delay > 0) {
                    long interval = System.currentTimeMillis() - lastConnect;
                    if (interval < this.delay) {
                        Thread.sleep(this.delay - interval);
                    }
                }
            } catch (InterruptedException ex) {
                log.error("Unable to backoff", ex);
            }
            processQueue();
            synchronized (lock) {
                workerHandle = null;
                if (!connectQueue.isEmpty()) {
                    runWorker();
                }
            }
        }

        protected void processQueue() {
            boolean isMultiple = connectQueue.size() > 1;
            while (!connectQueue.isEmpty()) {
                SessionConnectNode node = connectQueue.poll();
                try {
                    node.run(isMultiple && connectQueue.isEmpty());
                    isMultiple = true;
                    lastConnect = System.currentTimeMillis();
                    if (connectQueue.isEmpty()) {
                        break;
                    }
                    if (this.delay > 0) {
                        Thread.sleep(this.delay);
                    }
                } catch (IllegalStateException e) {
                    Throwable t = e.getCause();
                    if (t instanceof OpeningHandshakeException) {
                        log.error("Failed opening handshake, appending to queue. Message: {}", e.getMessage());
                    } else if (t != null && !JFA.Status.RECONNECT_QUEUED.name().equals(t.getMessage())) {
                        log.error("Failed to establish connection for a node, appending to queue", e);
                    } else {
                        log.error("Unexpected exception when running connect node", e);
                    }
                    appendSession(node);
                } catch (InterruptedException e) {
                    log.error("Failed to run node", e);
                    appendSession(node);
                    return; // caller should start a new thread
                }
            }
        }
    }
}
