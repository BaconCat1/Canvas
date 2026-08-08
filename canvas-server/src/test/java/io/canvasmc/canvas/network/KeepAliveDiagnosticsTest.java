package io.canvasmc.canvas.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class KeepAliveDiagnosticsTest {

    @Test
    void separatesServerDispatchDelayFromNetworkRoundTrip() {
        final long scheduled = TimeUnit.SECONDS.toNanos(10L);
        final long dispatched = scheduled + TimeUnit.SECONDS.toNanos(2L);
        final long response = dispatched + TimeUnit.MILLISECONDS.toNanos(50L);

        final KeepAliveDiagnostics.Timing timing = KeepAliveDiagnostics.measure(scheduled, dispatched, response);

        assertEquals(TimeUnit.MILLISECONDS.toNanos(2050L), timing.totalTimeNs());
        assertEquals(TimeUnit.SECONDS.toNanos(2L), timing.dispatchDelayNs());
        assertEquals(TimeUnit.MILLISECONDS.toNanos(50L), timing.networkRoundTripNs());
        assertEquals(KeepAliveDiagnostics.Cause.SERVER_DISPATCH, timing.likelyCause());
        assertEquals(dispatched, timing.latencyStartTimeNs(scheduled, dispatched));
    }

    @Test
    void identifiesNetworkDominatedLatency() {
        final long scheduled = TimeUnit.SECONDS.toNanos(10L);
        final long dispatched = scheduled + TimeUnit.MILLISECONDS.toNanos(5L);
        final long response = dispatched + TimeUnit.MILLISECONDS.toNanos(1200L);

        final KeepAliveDiagnostics.Timing timing = KeepAliveDiagnostics.measure(scheduled, dispatched, response);

        assertEquals(KeepAliveDiagnostics.Cause.NETWORK, timing.likelyCause());
        assertTrue(timing.exceeds(TimeUnit.SECONDS.toNanos(1L)));
    }

    @Test
    void fallsBackToScheduledTimeWhenDispatchWasNotObserved() {
        final long scheduled = TimeUnit.SECONDS.toNanos(10L);
        final long response = scheduled + TimeUnit.MILLISECONDS.toNanos(75L);

        final KeepAliveDiagnostics.Timing timing = KeepAliveDiagnostics.measure(scheduled, 0L, response);

        assertEquals(KeepAliveDiagnostics.Cause.UNKNOWN, timing.likelyCause());
        assertFalse(timing.hasDispatchTime());
        assertEquals(scheduled, timing.latencyStartTimeNs(scheduled, 0L));
    }

    @Test
    void thresholdIsInclusive() {
        final KeepAliveDiagnostics.Timing timing = KeepAliveDiagnostics.measure(1L, 1L, 101L);

        assertTrue(timing.exceeds(100L));
        assertFalse(timing.exceeds(101L));
    }
}
