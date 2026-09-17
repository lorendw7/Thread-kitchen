# Java Concurrency Foundations

Reference notes for the memory-model and coordination rules used by the queue
implementations in this repository.

---

## 0.1 Threads and shared memory

A **thread** is an independent path of execution. All threads in one JVM share the
same **heap** (objects, static fields), but each has its own **stack** (local
variables, call frames) and its own **program counter**.

The danger lives entirely in **shared, mutable state on the heap**. Two threads
reading/writing the same field, with no coordination, is where bugs are born.

```
Heap (shared)                Thread A stack        Thread B stack
┌───────────────┐            ┌──────────┐          ┌──────────┐
│ count = 0     │ <───────── │ local i  │          │ local j  │
│ buffer[...]   │ ─────────> │ ...      │          │ ...      │
└───────────────┘            └──────────┘          └──────────┘
```

---

## 0.2 Race condition, critical section, mutual exclusion

- **Race condition**: the result depends on the *timing/interleaving* of threads.
- **Critical section**: a block of code that touches shared state and must not be
  run by two threads at once.
- **Mutual exclusion (mutex)**: the guarantee that only one thread is inside a
  given critical section at a time.

The whole game of concurrency is: *find the critical sections, and protect them.*

---

## 0.3 Atomicity — "all or nothing"

An operation is **atomic** if it happens completely or not at all — no other thread
can observe a half-done state.

The classic trap: **`count++` is NOT atomic.** It is three steps:

```
1. read  count        (load from memory into a register)
2. add 1              (compute newValue)
3. write count        (store back to memory)
```

Two threads can both read `0`, both compute `1`, both write `1` — and you "lost" an
increment. With 2 threads × 10,000 increments you can end up well below 20,000.

> Atomicity question to always ask: *"If two threads run this line at the same time,
> can one see the other's half-finished work?"*

---

## 0.4 Visibility — "did the other thread even see my write?"

Modern CPUs don't read/write main memory directly; each core has **caches**. A write
by Thread A may sit in A's cache (or a store buffer) and **never become visible** to
Thread B, possibly forever.

```
Core A cache: count=1   ──(not flushed)──>   Main memory: count=0   <── Core B reads 0
```

The infamous symptom: a `while (!stop) {}` loop that **never exits** even after
another thread sets `stop = true`, because B never sees A's write.

> Atomicity and visibility are **independent** problems. You can have one without the
> other. `count++` needs both: atomic read-modify-write *and* the new value made
> visible.

---

## 0.5 Ordering / reordering — "it didn't run in the order I wrote it"

To go faster, **the compiler, the JIT, and the CPU are all allowed to reorder
instructions**, as long as the result looks the same *to a single thread*. Across
threads, that "as-if-serial" promise does **not** hold, and you can observe
impossible-looking orderings.

Classic example (both start at 0):

```
Thread A:  a = 1;  r1 = b;
Thread B:  b = 1;  r2 = a;
```

You might intuit `r1 == 0 && r2 == 0` is impossible — but with reordering it can
happen. This is why we need a *memory model* to define what's actually guaranteed.

---

## 0.6 The Java Memory Model (JMM) and happens-before

The **JMM** (Java Language Spec, Chapter 17) is the contract that says which writes a
read is guaranteed to see. Its key relation is **happens-before**:

> If action X *happens-before* action Y, then X's effects (including all its memory
> writes) are **visible** to Y, and X is **ordered** before Y.

You don't memorize the cache rules — you establish **happens-before edges** using
the language's tools. The ones that matter for us:

- **Program order**: within one thread, earlier statements happen-before later ones.
- **Monitor lock**: unlocking a monitor happens-before any later lock of the *same*
  monitor. (This is why `synchronized` gives visibility.)
- **volatile**: a write to a volatile field happens-before every later read of that
  same field.
- **Thread start/join**: `t.start()` happens-before everything the thread runs;
  everything the thread runs happens-before another thread's return from `t.join()`.

> Mental model: locks and volatiles are **memory barriers** that "publish" your
> writes and "subscribe" to others'.

---

## 0.7 `volatile` — visibility + ordering, but NOT atomicity

`volatile` on a field guarantees:
- **Visibility**: a write is immediately visible to other threads' reads.
- **Ordering**: it acts as a barrier; reads/writes around it can't be reordered
  across it.

It does **NOT** give atomicity for compound actions. `volatile int count; count++;`
is still broken — the `++` is still read-modify-write. Use `volatile` for **flags**
(`volatile boolean stop`), not for counters.

---

## 0.8 `synchronized` — the Swiss-army tool

`synchronized` on a block/method acquires an object's **monitor** (intrinsic lock).
It gives you **three** things at once:

1. **Mutual exclusion** — one thread in the critical section at a time.
2. **Visibility** — via the monitor happens-before rule (unlock → lock).
3. **Coordination** — through `wait()` / `notify()` / `notifyAll()` on the same
   monitor.

The `wait/notify` trio (only callable while holding the monitor):
- `wait()` — **releases** the monitor and parks the thread in the monitor's *wait
  set* until notified (or interrupted). On wake it **re-acquires** the monitor.
- `notify()` — wakes **one** arbitrary thread from the wait set.
- `notifyAll()` — wakes **all** threads in the wait set.

> **The golden rule:** always call `wait()` inside a `while` loop that re-checks the
> condition — never an `if`. Reasons: (a) **spurious wakeups** (the JVM may wake you
> with no notify), and (b) by the time you re-acquire the lock, another thread may
> have already changed the condition back.

```java
// Correct condition-waiting shape
synchronized (lock) {
    while (!conditionHolds()) {
        lock.wait();
    }
    // safe to proceed: condition holds AND we hold the lock
}
```

---

## 0.9 The bounded-buffer (producer–consumer) problem

A **bounded buffer** is a fixed-capacity queue between **producers** (add items) and
**consumers** (remove items). The two blocking rules:

- Buffer **full** → a producer calling `put` must **block** (wait) until space frees.
- Buffer **empty** → a consumer calling `take` must **block** (wait) until an item
  arrives.

This is the coordination problem implemented by the queue classes in this project:
mutual exclusion protects the buffer, condition waiting blocks on full/empty, and
signaling wakes threads that may now proceed.

---

## 0.10 Liveness hazards (bugs where threads make no progress)

Even with correct mutual exclusion, you can stall:

- **Deadlock** — two+ threads each hold a lock the other needs; nobody proceeds.
  (Classic cause: acquiring multiple locks in inconsistent order.)
- **Livelock** — threads keep reacting to each other and retrying, busy but stuck.
- **Starvation** — a thread never gets the CPU/lock it needs (e.g. always lower
  priority, or unfair locking). **Fairness** policies address this.

A controlled stress test can reproduce this wakeup stall and verify the corrected
signaling strategy.

---

## Design review checklist

1. Why can `count++` lose updates even on a single-core machine? (atomicity)
2. Why can a `while(!stop){}` loop never terminate? (visibility)
3. What two guarantees does `synchronized` give that `volatile` does not?
4. Why must `wait()` live inside a `while`, not an `if`? Give both reasons.
5. In a bounded buffer, name the two conditions on which threads block.

These questions should be answerable from the implementation and its tests.
