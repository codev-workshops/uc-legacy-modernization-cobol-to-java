package com.mainframe.carddemo.report.job.export;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

import java.util.List;

public class CompositeItemReader<T> implements ItemStreamReader<T> {

    private final List<ItemStreamReader<T>> delegates;
    private int currentIndex = 0;
    private ExecutionContext executionContext;

    public CompositeItemReader(List<ItemStreamReader<T>> delegates) {
        this.delegates = delegates;
    }

    @Override
    public T read() throws Exception {
        while (currentIndex < delegates.size()) {
            T item = delegates.get(currentIndex).read();
            if (item != null) {
                return item;
            }
            delegates.get(currentIndex).close();
            currentIndex++;
            if (currentIndex < delegates.size()) {
                delegates.get(currentIndex).open(executionContext);
            }
        }
        return null;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        this.executionContext = executionContext;
        if (!delegates.isEmpty()) {
            delegates.get(0).open(executionContext);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        if (currentIndex < delegates.size()) {
            delegates.get(currentIndex).update(executionContext);
        }
    }

    @Override
    public void close() throws ItemStreamException {
        if (currentIndex < delegates.size()) {
            delegates.get(currentIndex).close();
        }
    }
}
