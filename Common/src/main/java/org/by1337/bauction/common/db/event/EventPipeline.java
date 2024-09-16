package org.by1337.bauction.common.db.event;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class EventPipeline<T> {
    private final List<PipelineHandler<? extends T>> handlers = new ArrayList<>();
    private final Set<String> names = new HashSet<>();

    public void run(T t) {
        for (PipelineHandler<? extends T> handler : handlers) {
            if (handler.type == t.getClass()) {
                @SuppressWarnings("unchecked")
                Consumer<T> consumer = (Consumer<T>) handler.consumer;
                consumer.accept(t);
            }
        }
    }
    @CanIgnoreReturnValue
    public <E extends T> EventPipeline<T> addLast(String name, Class<E> type, Consumer<? super E> consumer) {
        if (names.contains(name))
            throw new IllegalStateException("Handler with the name '" + name + "' already exists!");
        names.add(name);
        handlers.add(new PipelineHandler<>(name, type, consumer));
        return this;
    }
    @CanIgnoreReturnValue
    public <E extends T> EventPipeline<T> addFirst(String name, Class<E> type, Consumer<? super E> consumer) {
        if (names.contains(name))
            throw new IllegalStateException("Handler with the name '" + name + "' already exists!");
        names.add(name);
        handlers.add(0, new PipelineHandler<>(name, type, consumer));
        return this;
    }
    @CanIgnoreReturnValue
    public <E extends T> EventPipeline<T> addBefore(String baseName, String name, Class<E> type, Consumer<? super E> consumer) {
        if (names.contains(name))
            throw new IllegalStateException("Handler with the name '" + name + "' already exists!");
        names.add(name);
        int index = findHandlerIndex(baseName);
        if (index != -1) {
            handlers.add(index, new PipelineHandler<>(name, type, consumer));
        }
        return this;
    }
    @CanIgnoreReturnValue
    public <E extends T> EventPipeline<T> addAfter(String baseName, String name, Class<E> type, Consumer<? super E> consumer) {
        if (names.contains(name))
            throw new IllegalStateException("Handler with the name '" + name + "' already exists!");
        names.add(name);
        int index = findHandlerIndex(baseName);
        if (index != -1) {
            handlers.add(index + 1, new PipelineHandler<>(name, type, consumer));
        }
        return this;
    }

    public EventPipeline<T> remove(String baseName) {
        int index = findHandlerIndex(baseName);
        if (index != -1) {
            handlers.remove(index);
            names.remove(baseName);
        }
        return this;
    }

    private int findHandlerIndex(String name) {
        for (int i = 0; i < handlers.size(); i++) {
            if (handlers.get(i).name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private record PipelineHandler<T>(String name, Class<T> type, Consumer<? super T> consumer) {
    }
}
