package org.by1337.bauction.db.event;

import junit.framework.TestCase;
import org.by1337.bauction.common.db.event.EventPipeline;

import static org.junit.jupiter.api.Assertions.*;

public class EventPipelineTest extends TestCase {
    private EventPipeline<Event> pipeline;

    public void setUp() {
        pipeline = new EventPipeline<>();
    }

    public void testAddLastAndRun() {
        UserEvent userEvent = new UserEvent("John Doe");
        StringBuilder result = new StringBuilder();

        pipeline.addLast("userHandler", UserEvent.class, user -> result.append(user.getName()));

        pipeline.run(userEvent);

        assertEquals("John Doe", result.toString());
    }

    public void testAddFirst() {
        OrderEvent orderEvent = new OrderEvent(123);
        StringBuilder result = new StringBuilder();

        pipeline.addFirst("orderHandler", OrderEvent.class, order -> result.append(order.getOrderId()));

        pipeline.run(orderEvent);

        assertEquals("123", result.toString());
    }

    public void testAddBefore() {
        UserEvent userEvent = new UserEvent("Jane Doe");
        StringBuilder result = new StringBuilder();

        pipeline.addLast("userHandler", UserEvent.class, user -> result.append(user.getName()));

        pipeline.addBefore("userHandler", "beforeUserHandler", UserEvent.class, user -> result.insert(0, user.getName() + " "));

        pipeline.run(userEvent);

        assertEquals("Jane Doe Jane Doe", result.toString());
    }

    public void testAddAfter() {
        UserEvent userEvent = new UserEvent("Doe John");
        StringBuilder result = new StringBuilder();

        pipeline.addFirst("userHandler", UserEvent.class, user -> result.append(user.getName()));

        pipeline.addAfter("userHandler", "afterUserHandler", UserEvent.class, user -> result.append(" " + user.getName()));

        pipeline.run(userEvent);

        assertEquals("Doe John Doe John", result.toString());
    }

    public void testRemove() {
        UserEvent userEvent = new UserEvent("John Smith");
        StringBuilder result = new StringBuilder();

        pipeline.addLast("handler1", UserEvent.class, user -> result.append("Handler1"));
        pipeline.addLast("handler2", UserEvent.class, user -> result.append("Handler2"));

        pipeline.remove("handler1");

        pipeline.run(userEvent);

        assertEquals("Handler2", result.toString());
    }

    public void testAddDuplicateHandler() {
        pipeline.addLast("uniqueHandler", UserEvent.class, user -> { /* no-op */ });

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                pipeline.addLast("uniqueHandler", UserEvent.class, user -> { /* no-op */ })
        );

        assertEquals("Handler with the name 'uniqueHandler' already exists!", thrown.getMessage());
    }

    static class Event {
    }

    static class UserEvent extends Event {
        private final String name;

        UserEvent(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }
    }

    static class OrderEvent extends Event {
        private final int orderId;

        OrderEvent(int orderId) {
            this.orderId = orderId;
        }

        int getOrderId() {
            return orderId;
        }
    }
}