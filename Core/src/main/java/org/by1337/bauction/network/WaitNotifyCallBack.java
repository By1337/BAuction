package org.by1337.bauction.network;

import org.jetbrains.annotations.Nullable;

public abstract class WaitNotifyCallBack<T> implements CallBack<T> {
    @Override
    public void back(@Nullable T value) {
        synchronized (this) {
            notifyAll();
            back0(value);
        }
    }

    protected abstract void back0(@Nullable T value);

    public void wait_(long ms) throws InterruptedException {
        synchronized (this) {
            wait(ms);
        }
    }

    public void wait_() throws InterruptedException {
        wait_(0L);
    }
}
