package java.de.tum.msrg.baseline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;

/**
 * Created by pxsalehi on 09.02.17.
 */
public class BatchFactor {
    private static BatchFactor instance = new BatchFactor();

    public static BatchFactor getInstance() {
        return instance;
    }

    private BatchFactor() {}

    private final int[] MSG_SIZES = new int[] {512, 1024, 2*1024, 3*1024, 4*1024, 5*1024};
    private int msgSize;
    private int maxBatchSize = 512;
    private double[] factors = new double[maxBatchSize];
    public final String BATCH_FACTOR_STAT_FILE = "batch_factors";
    private boolean useFixedBatchFactor = false;
    private double fixedBatchFactor;

    public void read(int msgSize) {
        useFixedBatchFactor = false;
        this.msgSize = msgSize;
        // find msg sizes index
        int lineIdx = -1;
        for(int i = 0; i < MSG_SIZES.length; i++)
            if (MSG_SIZES[i] == msgSize)
                lineIdx = i;
        if (lineIdx == -1)
            throw new RuntimeException("Message size " + msgSize + " does not have a batch factor!");
        try {
            BufferedReader file = new BufferedReader(new FileReader(BATCH_FACTOR_STAT_FILE));
            String line;
            while((line = file.readLine()) != null) {
                line = line.replace('(', ' ').replace(')', ' ').replace(',', ' ').trim();
                String[] toks = line.split("\\s+");
                int msg = Integer.parseInt(toks[0]);
                int batch = Integer.parseInt(toks[1]);
                double factor = Double.parseDouble(toks[2]);
                if (msg == msgSize)
                    factors[batch - 1] = factor;
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Cannot read batch factors!", e);
        }
    }

    public double getBatchFactor(int batchSize) {
        if (batchSize <= 0)
            throw new RuntimeException("Invalid batch size " + batchSize);
        if (useFixedBatchFactor)
            return fixedBatchFactor;
        if (batchSize > maxBatchSize)
            return factors[factors.length - 1];
        else
            return factors[batchSize - 1];
    }

    public double[] getFactors() {
        return factors;
    }

    public boolean isUseFixedBatchFactor() {
        return useFixedBatchFactor;
    }

    public void setUseFixedBatchFactor(boolean useFixedBatchFactor) {
        this.useFixedBatchFactor = useFixedBatchFactor;
    }

    public void setFixedBatchFactor(double fixedBatchFactor) {
        useFixedBatchFactor = true;
        this.fixedBatchFactor = fixedBatchFactor;
    }

    public static void main(String[] args) {
        BatchFactor.getInstance().read(1024);
        System.out.println(Arrays.toString(BatchFactor.getInstance().getFactors()));
        System.out.println(BatchFactor.getInstance().getBatchFactor(8));
        BatchFactor.getInstance().setFixedBatchFactor(0.4);
        System.out.println(BatchFactor.getInstance().getBatchFactor(8));
        BatchFactor.getInstance().setUseFixedBatchFactor(false);
        System.out.println(BatchFactor.getInstance().getBatchFactor(8));
        System.out.println(BatchFactor.getInstance().getBatchFactor(600));
        System.out.println(BatchFactor.getInstance().getBatchFactor(1));
        System.out.println(BatchFactor.getInstance().getBatchFactor(512));
    }
}
