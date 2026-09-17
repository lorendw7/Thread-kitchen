# Development Guide

## Development loop

For each behavior:

1. Define the contract and invariant.
2. Add the smallest failing test.
3. Implement the behavior.
4. Run the focused test and the complete suite.
5. Review interruption, cleanup, timeouts, and possible hangs.
6. Commit only after the milestone's checks pass.

Avoid using `Thread.sleep` as the primary synchronization mechanism in tests.
Prefer `CountDownLatch`, barriers, futures with timeouts, and bounded joins.

## Source layout

- Application classes: `src/main/java`
- JUnit tests: `src/test/java`
- JMH benchmarks: `src/main/java/com/threadkitchen/bench`
- Reports and design notes: `docs`

JUnit has `test` scope in `pom.xml`. A test class placed under `src/main/java` will
therefore fail to resolve `org.junit.jupiter.api` and must be moved to
`src/test/java`.

## `SimpleBlockingQueue` sequence

1. Validate `capacity > 0`.
2. Define null-handling behavior.
3. Add FIFO and wrap-around tests.
4. Implement blocking `put` and `take` operations.
5. Add full-queue, empty-queue, and interruption tests.
6. Add SPSC and MPMC transfer tests.
7. Use `notifyAll` for MPMC operation with a shared monitor wait set.

Every wait must use a `while` guard. Queue state must be read and written while
holding the same monitor.

## `LockBlockingQueue` sequence

1. Create the same bounded ring-buffer state.
2. Add one `ReentrantLock` and two conditions.
3. Make producers wait on `notFull` and signal `notEmpty`.
4. Make consumers wait on `notEmpty` and signal `notFull`.
5. Add timed operations using the remaining value returned by `awaitNanos`.
6. Run the common queue contract suite.

Every successful `lock` call must have a corresponding `unlock` in `finally`.

## Concurrent test rules

- Start competing workers together.
- Give each produced value a unique identity.
- Verify exact totals and detect duplicates.
- Apply explicit timeouts to futures and joins.
- Cancel and join workers during cleanup, including failure paths.
- Repeat stress tests with small capacities and more workers than CPU cores.

## Benchmark rules

- Benchmark only implementations that pass the correctness suite.
- Record warmup, measurement, forks, JVM, OS, and processor information.
- Consume operation results so the JIT cannot eliminate work.
- Compare trends across capacities and thread counts, not only the best score.
