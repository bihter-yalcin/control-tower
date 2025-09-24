# Control Tower

<img width="1384" height="514" alt="Ekran Resmi 2025-09-20 15 20 05" src="https://github.com/user-attachments/assets/1964dd3f-5d50-47d7-8752-f239528faa06" />

## Experiment 1 — Creating Threads (Passenger Arrivals)

In this first experiment, I explore the basics of Java threading:

- **Creating threads** using `Runnable` and `Thread`
- **Naming threads** for clearer debugging
- **Starting threads** with `start()`
- **Joining threads** so the main thread waits until workers finish

**Scenario:**  
Passengers (threads) arrive at the airport and are processed concurrently.  
The control tower (main thread) waits until all passengers are finished before closing the process.

---

## Experiment 2 — Thread Termination (Gate Shutdown)

In this experiment, I explore **how to stop threads cleanly**:

### 🅰 Graceful Shutdown
- Uses a `volatile running` flag and `interrupt()` together.
- Worker thread continuously processes passengers until the control tower signals closure.
- Demonstrates that:
    - `running=false` → prevents further loop iterations,
    - `interrupt()` → wakes the thread if it is blocked (e.g., during `sleep`).
- **Outcome:** worker finishes ongoing work and exits in a controlled manner.

### 🅱 Executor Shutdown (GateTask + ExecutorService)
- Multiple short-lived `GateTask` jobs are submitted to a fixed thread pool.
- `shutdown()` → no new tasks, but existing ones finish.
- `awaitTermination()` → waits for tasks to complete.
- `shutdownNow()` → interrupts running tasks and cancels queued ones.
- **Outcome:** shows the difference between **graceful** and **forceful** termination.

---

## Experiment 3 — Joint Threads (Pushback Checklist)

In this experiment, I use `Thread.join()` to coordinate multiple tasks and observe the effect of parallelism.

### 🅰 Catering (1 vs 2 attendants)
We process N catering trolleys in two modes:

- **One attendant (serial):**  
  Start each thread and immediately `join()` → no overlap.

- **Two attendants (parallel):**  
  Start all threads, then `join()` them all → overlap reduces total time.

**Outcome:** Parallel execution lowers total time (not strictly half, but clearly improved).

### 🅱 Pushback Checklist (3 parallel tasks)
Start three tasks in parallel:
- Catering (~1500 ms)
- Fueling (~3000 ms)
- Boarding (~2000 ms)

Main thread calls `join()` on all three before allowing pushback.

**Outcome:** Total preparation time ≈ the longest task (Fueling). This demonstrates the *critical path* idea.

---

## Experiment 4 — Latency (Security Screening)

In this experiment, I simulate passengers going through airport security.  
Each passenger takes a random amount of time: most are fast (120–220 ms), but a few take much longer (800–1500 ms).  
This models **tail latency** — the rare but very slow cases.

We measure:
- **Per-passenger latency** (min, p50, p95, p99, max, avg)
- **Wall time** (total time until all passengers finish)
- **Throughput** (passengers per second)

### Results (300 passengers)

| Lanes | Wall (ms) | Throughput (passenger/s) | p50 (ms) | p95 (ms) | p99 (ms) | Max (ms) | Avg (ms) |
|-------|-----------|--------------------------|----------|----------|----------|----------|----------|
| 1     | 82638     | 3.63                     | 187      | 1239     | 1571     | 1682     | 275.1    |
| 4     | 20824     | 14.41                    | 187      | 1239     | 1574     | 1685     | 275.1    |
| 8     | 10481     | 28.62                    | 187      | 1163     | 1574     | 1683     | 264.4    |

### Key takeaways
- **More lanes → lower wall time, higher throughput.**  
  (From 82s at 1 lane → ~10s at 8 lanes)
- **Median latency (p50)** stayed ~187 ms: fast passengers are always fast.
- **Tail latencies (p95/p99)** improved slightly with more lanes (less queuing),  
  but never fully disappeared — a slow passenger is still slow.
- **Average latency** dropped a bit when queues were shorter.

In short: adding lanes improves system capacity and reduces the waiting effect,  
but **it cannot eliminate the inherent slowness of tail cases**.

---

## Experiment 5 — Producer–Consumer (Baggage Belt System)

In this experiment, I implemented the classic **Producer–Consumer** pattern using Java’s `BlockingQueue`.

### Scenario
- I modeled **passengers** as producers dropping luggage onto a **baggage belt** (a shared queue).
- The belt had a fixed capacity (10). If it was full, passengers had to wait before dropping more bags.
- I modeled **handlers** as consumers who continuously took bags from the belt and loaded them onto the plane.
- With more passengers than handlers, the belt filled up and producers blocked.
- With more handlers than passengers, handlers sometimes waited for bags.

### What I learned
- How to coordinate multiple threads safely using `BlockingQueue`.
- That **`put()`** blocks when the queue is full (backpressure on producers).
- That **`take()`** blocks when the queue is empty (consumers wait for work).
- How multiple producers and consumers can work together without writing `synchronized` or `wait/notify`.
- How this pattern applies to real-world systems like request queues, message brokers (Kafka, RabbitMQ), or baggage belts at airports.

### Example run
I simulated 20 passengers (producers) with 5 check-in counters (threads) and 2 baggage handlers (consumers).  
Passengers dropped their bags, and handlers loaded them in parallel.  
When all passengers were done, I interrupted the handlers to stop them gracefully.

**Outcome:**  
I observed the baggage belt fill and drain dynamically.  
By changing the number of passengers, handlers, or belt capacity, I can see different bottlenecks and backpressure effects.

---

## Experiment 6 — Locking Strategies (Runway & Gates)

In this experiment, I explored different **locking strategies** in Java to coordinate multiple threads and avoid problems like race conditions or deadlocks.

### Single Runway (`synchronized`)
- **Scenario:** Only one aircraft can use the runway at a time.
- I used `synchronized` to make the runway a **critical section**.
- Multiple threads tried to access it, but only one was allowed at once.
- **Outcome:** I learned that `synchronized` is the simplest way to enforce mutual exclusion. It guarantees safety but does not guarantee fairness (which thread goes first).

### Deadlock (wrong lock ordering)
- **Scenario:** Aircraft A acquires `TRACTOR` first, then wants `RUNWAY`.  
  Aircraft B acquires `RUNWAY` first, then wants `TRACTOR`.
- Both threads hold one lock and wait forever for the other → program hangs.
- **Outcome:** I learned how deadlock happens when multiple threads acquire locks in inconsistent order and never release them.

### Deadlock Solution #1: Consistent Ordering
- **Scenario:** All flights acquire locks in the **same order** (first `TRACTOR`, then `RUNWAY`).
- This simple rule prevents circular waiting, so no deadlock occurs.
- **Outcome:** I learned that enforcing a **global lock ordering** is an easy and effective way to prevent deadlocks.

### Deadlock Solution #2: ReentrantLock with `tryLock(timeout)`
- **Scenario:** Flights still need both `TRACTOR` and `RUNWAY`.  
  This time I used `ReentrantLock` (fair mode) and `tryLock(timeout)`.
- Each thread tries to get the first lock, then the second.
    - If it fails within the timeout, it releases what it has and retries.
- This prevents deadlock by ensuring no one waits forever.
- **Outcome:** I learned that `ReentrantLock` provides advanced control: fairness, timed locking, and retry strategies. It’s more flexible and powerful than `synchronized`.

###  ReentrantReadWriteLock (Runway Metaphor)
- **Scenario:** Multiple planes (readers) come to check runway lights, while one plane (writer) needs to take off.
- Implemented `ReentrantReadWriteLock` with separate **read** and **write** locks.
- **Readers:** Can proceed concurrently if no writer holds the lock.
- **Writer:** Requires exclusive access and blocks all other readers/writers until done.
- This pattern improves efficiency in **read-heavy workloads**, since reads don’t block each other.
- **Outcome:** I learned that `ReentrantReadWriteLock` allows fine-grained concurrency: many parallel reads with exclusive writes, making it more powerful than a single lock.
