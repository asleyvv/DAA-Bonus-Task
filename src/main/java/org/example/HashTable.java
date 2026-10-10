// Hash table with separate chaining (linked list in each bucket)
// int keys, int values
public class HashTable {

    private class Node {
        int key, val;
        Node next;

        Node(int k, int v, Node n) {
            key = k;
            val = v;
            next = n;
        }
    }

    private Node[] table;
    private int size;
    private boolean canResize;

    public HashTable() {
        this(16, true);
    }

    // canResize = false is only used to show the worst case (long chains)
    public HashTable(int cap, boolean canResize) {
        table = new Node[cap];
        this.canResize = canResize;
    }

    public int size() {
        return size;
    }

    private int hash(int key, int len) {
        return (key & 0x7fffffff) % len;
    }

    // add key, or update the value if it already exists
    public void put(int key, int val) {
        int i = hash(key, table.length);
        for (Node n = table[i]; n != null; n = n.next) {
            if (n.key == key) {
                n.val = val;
                return;
            }
        }
        table[i] = new Node(key, val, table[i]); // add at the front of the chain
        size++;
        if (canResize && size > table.length * 0.75) grow();
    }

    public boolean contains(int key) {
        int i = hash(key, table.length);
        for (Node n = table[i]; n != null; n = n.next) {
            if (n.key == key) return true;
        }
        return false;
    }

    public boolean remove(int key) {
        int i = hash(key, table.length);
        Node prev = null;
        Node cur = table[i];
        while (cur != null) {
            if (cur.key == key) {
                if (prev == null) table[i] = cur.next;
                else prev.next = cur.next;
                size--;
                return true;
            }
            prev = cur;
            cur = cur.next;
        }
        return false;
    }

    // double the array and move every node to its new bucket
    private void grow() {
        Node[] bigger = new Node[table.length * 2];
        for (int i = 0; i < table.length; i++) {
            Node n = table[i];
            while (n != null) {
                Node next = n.next;
                int j = hash(n.key, bigger.length);
                n.next = bigger[j];
                bigger[j] = n;
                n = next;
            }
        }
        table = bigger;
    }

    public int longestChain() {
        int best = 0;
        for (int i = 0; i < table.length; i++) {
            int len = 0;
            for (Node n = table[i]; n != null; n = n.next) len++;
            if (len > best) best = len;
        }
        return best;
    }
}