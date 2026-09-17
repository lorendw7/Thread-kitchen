package com.threadkitchen.queue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimpleBlockingQueueTest {

    @Test
    void createsQueueWithPositiveCapacity() {
        assertDoesNotThrow(() -> new SimpleBlockingQueue<Integer>(1));
    }

    @Test
    void rejectsZeroCapacity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimpleBlockingQueue<Integer>(0));
    }

    @Test
    void rejectsNegativeCapacity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimpleBlockingQueue<Integer>(-1));
    }
}
