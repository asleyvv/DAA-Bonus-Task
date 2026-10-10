import java.io.*;
import java.util.*;

// runs the experiments and prints / saves the times
public class Main {

    static final int RUNS = 10;       // average over this many runs
    static final int WARMUP = 3;      // runs that are thrown away (JIT warm up)
    static final int[] SIZES = {100, 1000, 10000};

    // n different random keys (same seed -> same keys every time)
    static int[] randomKeys(int n) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < n * 5; i++) list.add(i);
        Collections.shuffle(list, new Random(42));
        int[] keys = new int[n];
        for (int i = 0; i < n; i++) keys[i] = list.get(i);
        return keys;
    }

    static int[] sortedKeys(int n) {
        int[] keys = new int[n];
        for (int i = 0; i < n; i++) keys[i] = i;
        return keys;
    }

    // returns {insert, search, delete} time in nanoseconds for ALL n operations
    static long[] timeBST(int[] keys) {
        BST t = new BST();
        long a = System.nanoTime();
        for (int k : keys) t.insert(k);
        long b = System.nanoTime();
        int found = 0;
        for (int k : keys) if (t.search(k)) found++;
        long c = System.nanoTime();
        for (int k : keys) t.delete(k);
        long d = System.nanoTime();
        if (found != keys.length || t.size() != 0) throw new RuntimeException("BST bug");
        return new long[]{b - a, c - b, d - c};
    }

    static long[] timeHash(int[] keys, int cap, boolean canResize) {
        HashTable h = new HashTable(cap, canResize);
        long a = System.nanoTime();
        for (int k : keys) h.put(k, k);
        long b = System.nanoTime();
        int found = 0;
        for (int k : keys) if (h.contains(k)) found++;
        long c = System.nanoTime();
        for (int k : keys) h.remove(k);
        long d = System.nanoTime();
        if (found != keys.length || h.size() != 0) throw new RuntimeException("Hash bug");
        return new long[]{b - a, c - b, d - c};
    }

    interface Test {
        long[] run();
    }

    // warm up, then average the runs. result in microseconds
    static double[] average(Test t) {
        for (int i = 0; i < WARMUP; i++) t.run();
        long[] sum = new long[3];
        for (int i = 0; i < RUNS; i++) {
            long[] r = t.run();
            for (int j = 0; j < 3; j++) sum[j] += r[j];
        }
        double[] avg = new double[3];
        for (int j = 0; j < 3; j++) avg[j] = sum[j] / (double) RUNS / 1000.0;
        return avg;
    }

    static void row(PrintWriter csv, String test, String ds, int n, double[] t) {
        System.out.printf("%-22s %-10s %6d %12.1f %12.1f %12.1f%n", test, ds, n, t[0], t[1], t[2]);
        csv.printf(Locale.US, "%s,%s,%d,%.1f,%.1f,%.1f%n", test, ds, n, t[0], t[1], t[2]);
    }

    public static void main(String[] args) throws Exception {
        PrintWriter csv = new PrintWriter(new FileWriter("results.csv"));
        csv.println("test,structure,n,insert_us,search_us,delete_us");

        System.out.println("Times are TOTAL microseconds for n operations (avg of " + RUNS + " runs)");
        System.out.printf("%-22s %-10s %6s %12s %12s %12s%n", "test", "structure", "n", "insert", "search", "delete");

        // 1. random keys (the normal / average case)
        for (int n : SIZES) {
            final int[] keys = randomKeys(n);
            row(csv, "random keys", "BST", n, average(() -> timeBST(keys)));
            row(csv, "random keys", "HashTable", n, average(() -> timeHash(keys, 16, true)));
        }

        // 2. worst case for the BST: keys come in sorted order
        System.out.println();
        for (int n : SIZES) {
            final int[] keys = sortedKeys(n);
            row(csv, "sorted keys", "BST", n, average(() -> timeBST(keys)));
            row(csv, "sorted keys", "HashTable", n, average(() -> timeHash(keys, 16, true)));
        }

        // 3. worst case for the hash table: fixed 16 buckets, never resizes
        System.out.println();
        for (int n : SIZES) {
            final int[] keys = randomKeys(n);
            row(csv, "no resize (16 buckets)", "HashTable", n, average(() -> timeHash(keys, 16, false)));
        }

        // shape of the structures after inserting
        System.out.println();
        for (int n : SIZES) {
            BST t1 = new BST();
            for (int k : randomKeys(n)) t1.insert(k);
            BST t2 = new BST();
            for (int k : sortedKeys(n)) t2.insert(k);
            HashTable h1 = new HashTable();
            for (int k : randomKeys(n)) h1.put(k, k);
            HashTable h2 = new HashTable(16, false);
            for (int k : randomKeys(n)) h2.put(k, k);
            System.out.println("n=" + n + "  BST height random=" + t1.height() + " sorted=" + t2.height()
                    + "  | longest chain resizing=" + h1.longestChain() + " no-resize=" + h2.longestChain());
        }

        csv.close();
    }
}