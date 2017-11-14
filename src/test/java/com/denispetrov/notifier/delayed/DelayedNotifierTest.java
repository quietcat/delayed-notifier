package com.denispetrov.notifier.delayed;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelayedNotifierTest {
    private static final Logger LOG = LoggerFactory.getLogger(DelayedNotifierTest.class);

    private volatile boolean notified;

    private DelayedNotifier delayedNotifier;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void singleNotifyAfterMultiplePings() {
        delayedNotifier = new DelayedNotifier(new Runnable() {
            public void run() {
                LOG.info("Update");
                notified = true;
            }
        }, 1000);
        notified = false;
        LOG.info("Pinging notifier {}", System.currentTimeMillis());
        delayedNotifier.ping();
        sleep(700);
        LOG.info("Pinging notifier {}", System.currentTimeMillis());
        delayedNotifier.ping();
        sleep(400);
        LOG.info("Pinging notifier {}", System.currentTimeMillis());
        delayedNotifier.ping();
        sleep(900);
        LOG.info("Current time: {}, notified: {}", System.currentTimeMillis(), notified);
        assertFalse(notified);
        sleep(200);
        LOG.info("Current time: {}, notified: {}", System.currentTimeMillis(), notified);
        assertTrue(notified);
    }

    @Test
    public void singleNotifyAfterMultipleShortPings() {
        delayedNotifier = new DelayedNotifier(new Runnable() {
            public void run() {
                LOG.info("Update");
                notified = true;
            }
        }, 150);
        notified = false;
        LOG.info("Pinging notifier {}", System.currentTimeMillis());
        delayedNotifier.ping();
        sleep(40);
        LOG.info("Pinging notifier {}", System.currentTimeMillis());
        delayedNotifier.ping();
        sleep(40);
        LOG.info("Pinging notifier {}", System.currentTimeMillis());
        delayedNotifier.ping();
        sleep(40);
        LOG.info("Pinging notifier {}", System.currentTimeMillis());
        delayedNotifier.ping();
        sleep(40);
        LOG.info("Pinging notifier {}", System.currentTimeMillis());
        delayedNotifier.ping();
        sleep(40);
        LOG.info("Pinging notifier {}", System.currentTimeMillis());
        delayedNotifier.ping();
        sleep(120);
        LOG.info("Current time: {}, notified: {}", System.currentTimeMillis(), notified);
        assertFalse(notified);
        sleep(50);
        LOG.info("Current time: {}, notified: {}", System.currentTimeMillis(), notified);
        assertTrue(notified);
    }

    @Test
    public void consecutiveNotify() {
        delayedNotifier = new DelayedNotifier(new Runnable() {
            public void run() {
                LOG.info("Update");
                notified = true;
            }
        }, 200);
        notified = false;
        delayedNotifier.ping();
        sleep(100);
        assertFalse(notified);
        sleep(200);
        assertTrue(notified);
        notified = false;
        delayedNotifier.ping();
        sleep(100);
        assertFalse(notified);
        sleep(200);
        assertTrue(notified);
    }

    @Test
    public void maxNotifyDelay() {
        delayedNotifier = new DelayedNotifier(new Runnable() {
            public void run() {
                LOG.info("Update");
                notified = true;
            }
        }, 150, 350);
        notified = false;
        delayedNotifier.ping();
        sleep(100);
        assertFalse(notified);
        delayedNotifier.ping();
        sleep(100);
        assertFalse(notified);
        delayedNotifier.ping();
        sleep(100);
        assertFalse(notified);
        delayedNotifier.ping();
        sleep(100);
        assertTrue(notified);
    }

    @Test
    public void notifyBeforeMaxNotifyDelay() {
        delayedNotifier = new DelayedNotifier(new Runnable() {
            public void run() {
                LOG.info("Update");
                notified = true;
            }
        }, 150, 450);
        notified = false;
        delayedNotifier.ping();
        sleep(100);
        assertFalse(notified);
        delayedNotifier.ping();
        sleep(100);
        assertFalse(notified);
        delayedNotifier.ping();
        sleep(200);
        assertTrue(notified);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            fail("Interrupted while sleeping");
        }
    }
}
