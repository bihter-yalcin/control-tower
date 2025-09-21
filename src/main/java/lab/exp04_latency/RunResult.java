package lab.exp04_latency;

public class RunResult {
    final int passengers, lanes;
    final long wallMs;
    final long min, max;
    final double p50, p95, p99, avg, throughput;

    RunResult(int passengers, int lanes, long wallMs, long min, long max, double p50, double p95, double p99, double avg, double throughput) {
        this.passengers = passengers;
        this.lanes = lanes;
        this.wallMs = wallMs;
        this.min = min;
        this.max = max;
        this.p50 = p50;
        this.p95 = p95;
        this.p99 = p99;
        this.avg = avg;
        this.throughput = throughput;
    }

    void print() {
        System.out.println(
                "Lanes=" + lanes +
                        " | Wall=" + wallMs + " ms" +
                        " | Thrpt=" + String.format("%.2f", throughput) + " passenger/s" +
                        " | Lat(ms) min=" + min +
                        " p50=" + (long) p50 +
                        " p95=" + (long) p95 +
                        " p99=" + (long) p99 +
                        " max=" + max +
                        " avg=" + String.format("%.1f", avg)
        );
    }
}