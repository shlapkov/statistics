package com.n26;

import com.n26.model.Statistics;
import com.n26.model.Transaction;
import com.n26.model.TransactionManagerImpl;
import org.junit.Before;
import org.junit.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.assertEquals;

public class TransactionManagerImplTest {

    private TransactionManagerImpl transactionManager;

    @Before
    public void setup() {
        System.setProperty("n26.evictionTimeOut", "6");
    }

    @Test
    public void calculateStatisticsTest() {
        transactionManager = new TransactionManagerImpl();
        double offset = 12.23;
        for (int i =0; i<10; i++) {
            ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
            Transaction tr = new Transaction(utc.toInstant().toEpochMilli(), (offset + (double) i));
            transactionManager.add(tr);
        }
        Statistics statistic = transactionManager.getStatistic();
        assertEquals(10, statistic.getCount());
        assertEquals(12.23, statistic.getMin(), 0.0);
        assertEquals(21.23, statistic.getMax(), 0.0);
        assertEquals(167.3, statistic.getSum(), 0.0);
        assertEquals(16.73, statistic.getAvg(), 0.0);
    }

    @Test
    public void transactionsCacheEvictionTest() throws InterruptedException {
        transactionManager = new TransactionManagerImpl();

        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
        transactionManager.add(new Transaction(utc.toInstant().toEpochMilli(), 10.00));
        Thread.sleep(500);
        utc = ZonedDateTime.now(ZoneOffset.UTC);
        transactionManager.add(new Transaction(utc.toInstant().toEpochMilli(), 5.00));
        Thread.sleep(500);
        Statistics statistic = transactionManager.getStatistic();
        assertEquals(2, statistic.getCount());
        assertEquals(5.00, statistic.getMin(), 0.0);
        assertEquals(10.00, statistic.getMax(), 0.0);
        assertEquals(15.00, statistic.getSum(), 0.0);
        assertEquals(7.50, statistic.getAvg(), 0.0);
        utc = ZonedDateTime.now(ZoneOffset.UTC);
        transactionManager.add(new Transaction(utc.toInstant().toEpochMilli(), 20.00));
        Thread.sleep(500);
        utc = ZonedDateTime.now(ZoneOffset.UTC);
        transactionManager.add(new Transaction(utc.toInstant().toEpochMilli(), 1.01));
        Thread.sleep(500);
        utc = ZonedDateTime.now(ZoneOffset.UTC);
        transactionManager.add(new Transaction(utc.toInstant().toEpochMilli(), 15.00));
        Thread.sleep(500);
        utc = ZonedDateTime.now(ZoneOffset.UTC);
        transactionManager.add(new Transaction(utc.toInstant().toEpochMilli(), 60.00));
        Thread.sleep(500);
        utc = ZonedDateTime.now(ZoneOffset.UTC);
        transactionManager.add(new Transaction(utc.toInstant().toEpochMilli(), 50.10));
        Thread.sleep(500);
        utc = ZonedDateTime.now(ZoneOffset.UTC);
        transactionManager.add(new Transaction(utc.toInstant().toEpochMilli(), 11.11));
        Thread.sleep(500);
        utc = ZonedDateTime.now(ZoneOffset.UTC);
        transactionManager.add(new Transaction(utc.toInstant().toEpochMilli(), 2.02));
        Thread.sleep(500);
        utc = ZonedDateTime.now(ZoneOffset.UTC);
        transactionManager.add(new Transaction(utc.toInstant().toEpochMilli(), 45.50));
        Thread.sleep(500);

        statistic = transactionManager.getStatistic();
        assertEquals(10, statistic.getCount());
        assertEquals(1.01, statistic.getMin(), 0.0);
        assertEquals(60.00, statistic.getMax(), 0.0);
        assertEquals(219.74, statistic.getSum(), 0.01);
        assertEquals(21.974, statistic.getAvg(), 0.001);
        Thread.sleep(1000);
        statistic = transactionManager.getStatistic();
        assertEquals(9, statistic.getCount());
        assertEquals(1.01, statistic.getMin(), 0.0);
        assertEquals(60.00, statistic.getMax(), 0.0);
        assertEquals(209.74, statistic.getSum(), 0.01);
        assertEquals(23.304, statistic.getAvg(), 0.001);
        Thread.sleep(1000);
        statistic = transactionManager.getStatistic();
        assertEquals(7, statistic.getCount());
        assertEquals(1.01, statistic.getMin(), 0.0);
        assertEquals(60.00, statistic.getMax(), 0.0);
        assertEquals(184.74, statistic.getSum(), 0.01);
        assertEquals(26.391, statistic.getAvg(), 0.001);
        Thread.sleep(500);
        //should update min
        statistic = transactionManager.getStatistic();
        assertEquals(6, statistic.getCount());
        assertEquals(2.02, statistic.getMin(), 0.0);
        assertEquals(60.00, statistic.getMax(), 0.0);
        assertEquals(183.73, statistic.getSum(), 0.01);
        assertEquals(30.621, statistic.getAvg(), 0.001);
        Thread.sleep(1000);
        //should update max
        statistic = transactionManager.getStatistic();
        assertEquals(4, statistic.getCount());
        assertEquals(2.02, statistic.getMin(), 0.0);
        assertEquals(50.10, statistic.getMax(), 0.0);
        assertEquals(108.729, statistic.getSum(), 0.01);
        assertEquals(27.182, statistic.getAvg(), 0.001);
        Thread.sleep(2000);
        //empty list
        statistic = transactionManager.getStatistic();
        assertEquals(0, statistic.getCount());
        assertEquals(0, statistic.getMin(), 0.0);
        assertEquals(0, statistic.getMax(), 0.0);
        assertEquals(0, statistic.getSum(), 0.0);
        assertEquals(0, statistic.getAvg(), 0.0);
    }

    @Test(timeout = 60000)
    public void transactionsThreadsTest() throws ExecutionException, InterruptedException {
        transactionManager = new TransactionManagerImpl();
        List<FutureTask> randomReaders = new ArrayList<>();
        List<FutureTask> randomWriters = new ArrayList<>();
        for (int i = 0; i < 10; i++)
        {
            Callable<String> callable = () -> {
                Random generator = new Random();
                for (int i1 = 0; i1 < 10; i1++) {
                    ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
                    transactionManager.add(new Transaction(utc.toInstant().toEpochMilli(), (100 * generator.nextDouble() - 50 )));
                    Thread.sleep(generator.nextInt(100));
                }
                return "";
            };
            randomWriters.add(new FutureTask(callable));
        }
        for (int i = 0; i < 10; i++)
        {
            Callable<String> callable = () -> {
                Random generator = new Random();
                for (int i12 = 0; i12 < 10; i12++) {
                    transactionManager.getStatistic();
                    Thread.sleep(generator.nextInt(100));
                }
                return "";
            };
            randomReaders.add(new FutureTask(callable));
        }
        for(int i = 0; i < 10; i++) {
            Thread w = new Thread(randomWriters.get(i));
            Thread r = new Thread(randomReaders.get(i));
            w.start();
            r.start();
        }
        for(int i = 0; i < 10; i++) {
            Thread w = new Thread(randomWriters.get(i));
            Thread r = new Thread(randomReaders.get(i));
            w.start();
            r.start();
        }
        for(int i = 0; i < 10; i++) {
            randomWriters.get(i).get();
            randomReaders.get(i).get();
        }
    }
}
