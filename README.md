# thread-kitchen

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Build: Maven](https://img.shields.io/badge/Build-Maven-blue.svg)](pom.xml)

A Java 21 concurrency project that implements bounded blocking queues, validates
their behavior under concurrent workloads, and compares their performance with JDK
queue implementations.

## Scope

- Array-backed bounded queue using `synchronized` and monitor wait sets
- Queue using `ReentrantLock` with separate `notFull` and `notEmpty` conditions
- Timed and interruptible operations
- Deterministic unit tests and multi-producer/multi-consumer stress tests
- Comparison with JDK `ArrayBlockingQueue` and `LinkedBlockingQueue`
- JMH throughput and latency benchmarks
- Optional CAS, false-sharing, jcstress, and SPSC ring-buffer experiments

## Requirements

| Tool | Version |
|------|---------|
| JDK | 21 LTS |
| Maven | 3.9+; IntelliJ bundled Maven is supported |
| IntelliJ IDEA | 2026.1 or compatible |
| JUnit | 5.11.3 |
| JMH | 1.37 |

## Project layout

```text
thread-kitchen/
├── pom.xml
├── docs/
│   ├── PROJECT_PLAN.md
│   ├── DEVELOPMENT_GUIDE.md
│   ├── ENVIRONMENT.md
│   └── FOUNDATIONS.md
└── src/
    ├── main/java/com/threadkitchen/
    │   ├── queue/
    │   └── bench/
    └── test/java/com/threadkitchen/queue/
```

Production code belongs in `src/main/java`. JUnit test classes must be placed in
`src/test/java`; the JUnit dependency intentionally has Maven `test` scope.

## Build in IntelliJ

1. Open the repository root.
2. Set the Project SDK and language level to Java 21.
3. Open `Settings > Build, Execution, Deployment > Build Tools > Maven`.
4. Set **Maven home path** to **Bundled (Maven 3)**.
5. Reload the Maven project.
6. Run `Lifecycle > test` from the Maven tool window.

See [docs/ENVIRONMENT.md](docs/ENVIRONMENT.md) for troubleshooting and
[docs/PROJECT_PLAN.md](docs/PROJECT_PLAN.md) for implementation milestones.

## Current status

- Project builds with IntelliJ bundled Maven 3.9.11 and JDK 21.0.10.
- `SimpleBlockingQueue` contains the ring-buffer state and constructor validation.
- Constructor tests cover positive, zero, and negative capacities.
- Queue operations and concurrency tests remain to be implemented.

## License

Released under the [MIT License](LICENSE). © 2026 lorendw7
