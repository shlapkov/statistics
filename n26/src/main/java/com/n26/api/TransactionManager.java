package com.n26.api;

import com.n26.model.Transaction;
import com.n26.model.Statistics;

/**
 * Transaction manager for hold statistic
 */
public interface TransactionManager {
    /**
     * Get current statistics
     * @return statics
     */
    Statistics getStatistic();

    /**
     * Add transactions and update statics
     * @param transaction
     * @return
     */
    boolean add(Transaction transaction);

    /**
     * Functions for evicting cache.
     * Current implementation is using strong consistency most of the time O(1).
     * However, using this functions and timer is possible to have eventual consistency all time O(1).
     * @param lastTime
     */
    void cacheEvict(long lastTime);
}
