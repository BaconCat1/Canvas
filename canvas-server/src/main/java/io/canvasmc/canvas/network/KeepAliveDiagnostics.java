package io.canvasmc.canvas.network;

/**
 * Separates server-side keepalive dispatch delay from the time spent waiting for a client response.
 */
public final class KeepAliveDiagnostics {

    private KeepAliveDiagnostics() {
    }

    public static Timing measure(final long scheduledTimeNs, final long dispatchTimeNs, final long responseTimeNs) {
        final long totalTimeNs = Math.max(0L, responseTimeNs - scheduledTimeNs);
        if (dispatchTimeNs < scheduledTimeNs || dispatchTimeNs > responseTimeNs) {
            return new Timing(totalTimeNs, -1L, -1L, Cause.UNKNOWN);
        }

        final long dispatchDelayNs = dispatchTimeNs - scheduledTimeNs;
        final long networkRoundTripNs = responseTimeNs - dispatchTimeNs;
        final Cause cause;
        if (dispatchDelayNs > networkRoundTripNs) {
            cause = Cause.SERVER_DISPATCH;
        } else if (networkRoundTripNs > dispatchDelayNs) {
            cause = Cause.NETWORK;
        } else {
            cause = Cause.MIXED;
        }
        return new Timing(totalTimeNs, dispatchDelayNs, networkRoundTripNs, cause);
    }

    public enum Cause {
        SERVER_DISPATCH,
        NETWORK,
        MIXED,
        UNKNOWN
    }

    public record Timing(long totalTimeNs, long dispatchDelayNs, long networkRoundTripNs, Cause likelyCause) {

        public boolean hasDispatchTime() {
            return this.dispatchDelayNs >= 0L;
        }

        public long latencyStartTimeNs(final long scheduledTimeNs, final long dispatchTimeNs) {
            return this.hasDispatchTime() ? dispatchTimeNs : scheduledTimeNs;
        }

        public boolean exceeds(final long thresholdNs) {
            return this.totalTimeNs >= thresholdNs;
        }
    }
}
