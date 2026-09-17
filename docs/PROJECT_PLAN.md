# Project Plan

## Objective

Implement and evaluate bounded blocking queues on Java 21. The project prioritizes
correctness, explicit concurrency contracts, deterministic failure behavior, and
reproducible performance results.

## Current status (2026-09-17)

| Area | Status | Completion check |
|------|--------|------------------|
| Build environment | Ready | IntelliJ Maven build succeeds on JDK 21 |
| Queue state and constructor | In progress | Capacity validation tests pass |
| Monitor-based queue | Not started | FIFO, wrap-around, blocking, and SPSC tests pass |
| MPMC verification | Not started | No missing/duplicate values; bounded timeout |
| Lock-based queue | Not started | Shared contract suite passes |
| Timed operations | Not started | Timeout and interruption tests pass |
| JDK comparison | Not started | `ABQ_NOTES.md` completed |
| Benchmarks | Not started | Reproducible JMH results documented |
| Advanced experiments | Optional | CAS, false sharing, jcstress, or SPSC ring buffer |

## Milestones

### 1. Monitor-based bounded queue

- Complete `put` and `take` with `synchronized`, `wait`, and signaling.
- Preserve `0 <= count <= capacity` and ring-buffer index invariants.
- Clear removed array slots to avoid retaining references.
- Validate capacity, FIFO order, wrap-around, blocking, and interruption.

### 2. Multi-producer/multi-consumer correctness

- Assign unique IDs to produced values.
- Detect missing and duplicate values.
- Start workers with a latch or barrier.
- Bound every concurrent test with a timeout and clean up worker threads.
- Compare `notify` behavior with the corrected `notifyAll` implementation.

### 3. Lock-based implementation

- Implement one `ReentrantLock` and two conditions: `notFull` and `notEmpty`.
- Guarantee `unlock` through `finally`.
- Add timed `offer` and `poll` using remaining nanoseconds.
- Run the same behavioral contract suite against both implementations.

### 4. JDK comparison

Document locking, condition signaling, ring-buffer indexing, fairness, null handling,
iterators, and bulk operations in `docs/ABQ_NOTES.md`.

### 5. Benchmarks

- Compare both custom queues with JDK `ArrayBlockingQueue` and
  `LinkedBlockingQueue`.
- Test capacities 1, 16, 256, and 4096 across multiple producer/consumer counts.
- Record JVM, CPU, warmup, measurement, throughput, and latency details.
- Store results and interpretation in `docs/RESULTS.md`.

### 6. Optional advanced work

- CAS counter and ABA experiment
- False-sharing benchmark
- jcstress memory-ordering experiment
- Lock-free SPSC ring buffer

## Estimate

| Work | Focused days |
|------|--------------|
| Core queue implementations and tests | 4–5 |
| JDK comparison and benchmarks | 3–4 |
| Advanced experiments and final report | 5–8 |

Expected total: 7–9 focused days for the core project or 12–17 days including the
advanced experiments.

## Definition of done

- Public operations have documented contracts.
- Unit and repeated concurrency tests pass without flaky timing assumptions.
- Blocking tests cannot hang the build indefinitely.
- Benchmarks are reproducible and include environment details.
- Documentation matches the implemented behavior.
- A clean checkout builds successfully using `mvn clean test` or IntelliJ Maven.
