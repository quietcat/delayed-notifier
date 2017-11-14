package com.denispetrov.notifier.delayed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelayedNotifier {
    private static final Logger LOG = LoggerFactory.getLogger(DelayedNotifier.class);
    private static int threadNum = 0;
    private long delay;
    private long maxDelay;
    private Runnable runnable;
    private Thread thread;
    private final Object sync = new Object();
    private volatile long lastPing = 0L;

    private Runnable threadRunnable = new Runnable() {
        public void run() {
            LOG.trace("Delayed notifier thread started");
            long lastPingCopy = lastPing;
            long time = delay;
            long maxTime = lastPing + maxDelay;
            if (maxTime < 0L) { // overflow
                maxTime = Long.MAX_VALUE;
            }
            LOG.trace("lastPing={}, maxTime={}", lastPing, maxTime);
            for (;;) {
                try {
                    LOG.trace("Delayed notifier thread sleeping for {} ms", time);
                    Thread.sleep(time);
                    synchronized(sync) {
                        long currentTime = System.currentTimeMillis();
                        LOG.trace("lastPing={}, currentTime={}", lastPing, currentTime);
                        if (lastPing > lastPingCopy && currentTime < maxTime) {
                            // we always want to wait at least delay ms since last ping to execute runnable
                            // for example, if delay is 1000 ms, after first ping we always sleep for 1000 ms
                            // if there was a second ping in 700 ms, when 1000 ms expires we need to wait
                            // another 700 ms to add up to 1000 ms after last ping
                            long pingTime = lastPing-lastPingCopy;
                            LOG.trace("pingTime={}", pingTime);
                            long maxPingTime = maxTime-currentTime;
                            LOG.trace("maxPingTime={}", maxPingTime);
                            time = Math.min(pingTime, maxPingTime);
                            // check for anything funky going on in the system, e.g. leap second or clock adjustments
                            if (time < 0) {
                                time = 0;
                            } else if (time > delay) {
                                time = delay;
                            }
                            lastPingCopy = lastPing;
                        } else {
                            LOG.trace("Delayed notifier executing runnable");
                                thread = null;
                                runnable.run();
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    };

    public DelayedNotifier(Runnable runnable, long delay) {
        this(runnable, delay, Long.MAX_VALUE);
    }

    public DelayedNotifier(Runnable runnable, long delay, long maxDelay) {
        this.runnable = runnable;
        this.delay = delay;
        this.maxDelay = maxDelay;
    }
    public void ping() {
        synchronized(sync) {
            lastPing = System.currentTimeMillis();
            if (thread == null) {
                LOG.trace("Delayed notifier starting new thread {}", threadNum);
                thread = new Thread(threadRunnable, "dn-" + threadNum);
                thread.start();
                threadNum += 1;
            }
        }
    }

    @Override
    public void finalize() {
        if (thread != null) {
            thread.interrupt();
        }
    }
}
