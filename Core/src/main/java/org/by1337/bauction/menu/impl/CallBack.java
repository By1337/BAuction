package org.by1337.bauction.menu.impl;

import java.util.Optional;

public interface CallBack<T extends Optional<?>> {
    void result(T result);
}
