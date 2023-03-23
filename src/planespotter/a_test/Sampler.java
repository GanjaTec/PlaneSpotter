package planespotter.a_test;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public abstract class Sampler implements Runnable {

    private final int maxSamples;

    public record Sample(int value, long timestamp) {
        @Override
        public String toString() {
            return "Sample{value=" + value + ", timestamp=" + timestamp + '}';
        }
    }

    private ArrayList<Sample> samples, history;
    private final int period;
    private int index;

    protected Sampler(int period, int maxSamples) {
        this.samples = new ArrayList<>();
        this.maxSamples = maxSamples;
        this.history = null;
        this.period = period;
    }

    public abstract void sample();

    @Override
    public final void run() {
        sample();
        draw();
    }

    // override
    public void draw() {
    }

    public final synchronized void addSample(@NotNull Sample sample) {
        if (index++ >= maxSamples - 1) {
            history = samples;
            samples = new ArrayList<>();
            index = 0;
        }
        samples.add(sample);
    }

    // TODO: 17.12.2022 test and debug
    public final ArrayList<Sample> getSamples() {
        return samples;
        /*Sample[] samps = new Sample[maxSamples];
        System.arraycopy(samples, 0, samps, 0, index + 1);
        if (history != null && (index < 100)) {
            System.arraycopy(history, index, samps, index, maxSamples);
        }
        return samps;*/
    }

    public final int getSampleCount() {
        return samples.size();
    }

    public final int getMaxSamples() {
        return maxSamples;
    }

    public int getPeriod() {
        return period;
    }


}
