package org.by1337.bauction.network;


import org.jetbrains.annotations.Nullable;

public interface CallBack<T> {
    void back(@Nullable T value);
}
