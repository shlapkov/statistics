package com.n26.model;

import com.n26.api.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Class implementing transaction manager interface
 */
public class TransactionManagerImpl implements TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);
    private static final String EVICTION_TIMEOUT = "60";
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    private LinkedList<Transaction> transactions = new LinkedList<>();
    private Statistics currentStatistics = new Statistics();
    private double max = Double.MIN_VALUE;
    private double min = Double.MAX_VALUE;
    private int evictionTimeOut;

    public TransactionManagerImpl() {
        evictionTimeOut = Integer.parseInt(System.getProperty("n26.evictionTimeOut", EVICTION_TIMEOUT));
    }

    @Override
    public Statistics getStatistic() {
        log.debug("get statistics");
        long lastTime = ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(evictionTimeOut).toInstant().toEpochMilli();
        cacheEvict(lastTime);
        readLock.lock();
        try {
            return currentStatistics;
        }
        finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean add(Transaction transaction) {
        log.debug("add transaction");
        if(!isValid(transaction)) {
            return false;
        }
        addTransaction(transaction);
        return true;
    }

    private boolean isValid(Transaction transaction) {
        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(evictionTimeOut);
        if(transaction.getTimestamp() > utc.toInstant().toEpochMilli())
        {
            return true;
        }
        return false;
    }

    private void addTransaction(Transaction transaction) {
        writeLock.lock();
        try {
            currentStatistics.upStatics(transaction);
            transactions.add(transaction);
            transactions.sort(Comparator.comparing(Transaction::getTimestamp));
            if (transaction.getAmount() < min) {
                min = transaction.getAmount();
            }
            if (transaction.getAmount() > max) {
                max = transaction.getAmount();
            }
            currentStatistics.setMax(max);
            currentStatistics.setMin(min);
            log.debug("new stat after add transaction" + currentStatistics.toString());
        }
        finally {
            writeLock.unlock();
        }
        long lastTime = ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(evictionTimeOut).toInstant().toEpochMilli();
        cacheEvict(lastTime);
    }

    public void cacheEvict(long lastTime) {
        if(transactions.size() == 0) {
            return;
        }
        writeLock.lock();
        try {
            log.debug("run transactions cache eviction " + lastTime);
            boolean needResetMaxMin = false;
            while (transactions.size() > 0 && transactions.getFirst().getTimestamp() < lastTime) {
                Transaction tr = transactions.removeFirst();
                log.debug("remove transaction " + tr.toString());
                currentStatistics.downStatics(tr);
                log.debug("new stat after remove" + currentStatistics.toString());
                if (tr.getAmount() == this.min || tr.getAmount() == this.max) {
                    log.debug("need to reset max min");
                    needResetMaxMin = true;
                }
            }
            if (transactions.size() > 0 && needResetMaxMin) {
                resetMaxMin();
            } else if (transactions.size() == 0) {
                max = Double.MIN_VALUE;
                min = Double.MAX_VALUE;
            }
        }
        finally {
            writeLock.unlock();
        }
    }
    private void resetMaxMin() {
        log.debug("run reset min/max ");
        DoubleSummaryStatistics st = transactions.stream().parallel()
                .collect(Collectors.summarizingDouble((Transaction::getAmount)));
        this.max = st.getMax();
        this.min = st.getMin();
        currentStatistics.setMin(this.min);
        currentStatistics.setMax(this.max);
        log.debug("new stat after reset min/max" + currentStatistics.toString());
    }
}
