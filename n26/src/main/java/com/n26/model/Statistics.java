package com.n26.model;

/**
 * Class holding statistics about transactions.
 */
public class Statistics {
    private double sum;
    private double avg;
    private double max;
    private double min;
    private long count;

    public Statistics() {
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "Statistics{" +
                "sum=" + sum +
                ", avg=" + avg +
                ", max=" + max +
                ", min=" + min +
                ", count=" + count +
                '}';
    }

    void upStatics(Transaction transaction) {
        this.count++;
        this.sum += transaction.getAmount();
        this.avg = this.sum/this.count;
    }

    void downStatics(Transaction transaction) {
        this.count--;
        if(this.count <= 0) {
            this.count =0;
            this.max = 0;
            this.min = 0;
            this.sum = 0;
            this.avg = 0;
        }
        else {
            this.sum -= transaction.getAmount();
            this.avg = this.sum / this.count;
        }
    }
}
