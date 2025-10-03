package org.example.transaction.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomThreadFactory implements ThreadFactory {

    private final String prefix;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    @Override
    public Thread newThread(final Runnable r) {
        return new Thread(r, prefix + "-" + threadNumber.getAndIncrement());
    }
}
