package com.threadkitchen.queue;

/**
 * A bounded blocking queue backed by an array (ring buffer).
 *
 * <p>Producers call {@code put}; consumers call {@code take}. When the buffer is
 * full, {@code put} blocks; when it is empty, {@code take} blocks. This first
 * implementation initially targets the single-producer / single-consumer (SPSC)
 * case using {@code synchronized} + {@code wait} / {@code notify} for coordination.
 *
 * <p>Invariants (must always hold while no thread is mid-operation):
 * <ul>
 *   <li>{@code 0 <= count <= capacity}</li>
 *   <li>empty  ⇔ {@code count == 0}</li>
 *   <li>full   ⇔ {@code count == capacity}</li>
 *   <li>{@code putIndex} / {@code takeIndex} wrap around: next index is
 *       {@code (index + 1) % capacity}</li>
 * </ul>
 */
public class SimpleBlockingQueue<E> {

    /** Backing storage; a fixed-size ring buffer. Length equals {@code capacity}. */
    private final E[] array;

    /** Maximum number of elements the queue can hold. Never changes. */
    private final int capacity;

    /** How many elements are currently stored. Mutated by put/take. */
    private int count;

    /** Index where the next {@code put} will write. */
    private int putIndex;

    /** Index where the next {@code take} will read. */
    private int takeIndex;

    /**
     * Creates an empty queue that can hold up to {@code capacity} elements.
     *
     * @param capacity the fixed capacity of the ring buffer
     */
    @SuppressWarnings("unchecked")
    public SimpleBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be greater than 0");
        }

        this.capacity = capacity;
        // Generic arrays can't be created directly (new E[capacity] won't compile
        // due to type erasure), so we create an Object[] and cast. This produces
        // an "unchecked" warning, which is safe here because only E values are stored.
        this.array = (E[]) new Object[capacity];
    }
}
