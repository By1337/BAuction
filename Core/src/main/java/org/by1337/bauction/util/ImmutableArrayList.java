package org.by1337.bauction.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ImmutableArrayList<E> implements List<E> {
    private final List<E> handle;

    public ImmutableArrayList(List<E> handle) {
        this.handle = Collections.unmodifiableList(handle);
    }

    @Override
    public int size() {
        return handle.size();
    }

    @Override
    public boolean isEmpty() {
        return handle.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return handle.contains(o);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return handle.iterator();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        handle.forEach(action);
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return handle.toArray();
    }

    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T[] a) {
        return handle.toArray(a);
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return handle.toArray(generator);
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return new HashSet<>(handle).containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sort(Comparator<? super E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E get(int index) {
        return handle.get(index);
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        return handle.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return handle.lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator() {
        return handle.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator(int index) {
        return handle.listIterator(index);
    }

    @NotNull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return new ImmutableArrayList<>(handle.subList(fromIndex, toIndex));
    }

    @Override
    public Spliterator<E> spliterator() {
        return handle.spliterator();
    }

    @Override
    public Stream<E> stream() {
        return handle.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return handle.parallelStream();
    }
}
