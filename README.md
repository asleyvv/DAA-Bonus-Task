# BST vs Hash Table (Java)

DAA assignment: implement a Binary Search Tree and a Hash Table, analyze insert / search / delete,
and compare them experimentally with 100, 1,000 and 10,000 elements.

## Files

- `src/BST.java` - binary search tree (iterative insert, search, delete)
- `src/HashTable.java` - hash table with separate chaining, resizes when load factor > 0.75
- `src/Main.java` - runs the experiments, prints a table and writes `results.csv`
- `results.csv` - raw results of my run

## How to run

```
javac -d out src/*.java
java -cp out Main
```

## Implementation notes

**BST**
- Keys are ints, duplicates are ignored.
- Delete has 3 cases: leaf, one child, two children. For two children I copy the key of the
  inorder successor (smallest key in the right subtree) into the node and then remove the successor.
- I used loops instead of recursion so a sorted input (tree height = n) does not cause a stack overflow.

**Hash table**
- Array of buckets, every bucket is a linked list (separate chaining).
- `hash(key) = key % capacity`, starts with 16 buckets.
- When `size > 0.75 * capacity` the array is doubled and all nodes are moved (rehash).

## Complexity analysis

n = number of elements, h = height of the BST.

### BST

| Operation | Best case | Average case | Worst case |
|-----------|-----------|--------------|------------|
| Insert    | O(1)      | O(log n)     | O(n)       |
| Search    | O(1)      | O(log n)     | O(n)       |
| Delete    | O(1)      | O(log n)     | O(n)       |

- Every operation walks down one path from the root, so the cost is O(h).
- Best case: the key is at the root (or the tree is empty for insert).
- Average case: with random keys the tree is fairly balanced, h is about log n.
- Worst case: keys inserted in sorted order make the tree a linked list, h = n.
  Then search, insert and delete are all O(n).
- Delete with two children also needs to find the successor, but that is still one path down, so still O(h).

### Hash table (separate chaining)

| Operation | Best case | Average case | Worst case |
|-----------|-----------|--------------|------------|
| Insert    | O(1)      | O(1) amortized | O(n)     |
| Search    | O(1)      | O(1)         | O(n)       |
| Delete    | O(1)      | O(1)         | O(n)       |

- Cost = compute the hash + walk the chain in that bucket.
- Best case: the bucket is empty or the key is the first node in the chain.
- Average case: the load factor is kept below 0.75, so chains are short (constant expected length).
- Worst case: all keys land in the same bucket (bad hash function, or no resizing), the table
  becomes one linked list and every operation is O(n).
- Insert is O(1) amortized: one resize costs O(n), but it happens rarely (capacity doubles each time).

## Experiments

Machine: *(write your CPU / RAM / Java version here)*

Method: for each size n, n unique random ints (fixed seed 42). Insert all, search all, delete all.
3 warm-up runs (not counted, JIT), then the average of 10 runs.
Times below are **total microseconds for all n operations**.

### 1. Random keys (normal case)

| n      | Structure | Insert (us) | Search (us) | Delete (us) |
|--------|-----------|-------------|-------------|-------------|
| 100    | BST       | 16.8        | 11.8        | 10.4        |
| 100    | HashTable | 23.9        | 4.2         | 4.4         |
| 1,000  | BST       | 549.4       | 242.0       | 114.9       |
| 1,000  | HashTable | 667.1       | 44.0        | 64.5        |
| 10,000 | BST       | 2082.1      | 1608.1      | 1871.9      |
| 10,000 | HashTable | 2088.7      | 153.2       | 163.7       |

### 2. Worst case for the BST (keys inserted in sorted order)

| n      | Structure | Insert (us) | Search (us) | Delete (us) |
|--------|-----------|-------------|-------------|-------------|
| 100    | BST       | 16.1        | 9.5         | 0.4         |
| 1,000  | BST       | 995.7       | 935.4       | 3.7         |
| 10,000 | BST       | 108216.2    | 100946.2    | 38.4        |
| 10,000 | HashTable | 322.8       | 68.1        | 50.2        |

BST height after inserting: 100 for n=100, 1000 for n=1000, 10000 for n=10000 (it is a linked list).
For random keys the height was only 14, 22 and 30.

The delete time for the sorted BST is tiny only because I delete in the same sorted order, so I always
remove the root and it has no left child (O(1) each time). That is a special case, not a general result.

### 3. Worst case for the hash table (fixed 16 buckets, no resizing)

| n      | Insert (us) | Search (us) | Delete (us) |
|--------|-------------|-------------|-------------|
| 100    | 31.2        | 6.5         | 7.0         |
| 1,000  | 180.9       | 614.4       | 94.9        |
| 10,000 | 17617.8     | 15635.6     | 13672.3     |

Longest chain: 10 / 77 / 669 for n = 100 / 1,000 / 10,000 (with resizing it was only 2 / 3 / 4).

## Comparison of results

- For random keys the hash table is clearly faster at searching and deleting (about 10x faster at n = 10,000).
  Insert is about the same, because the hash table also pays for the resizes.
- BST times grow faster than hash table times when n grows. From n = 1,000 to 10,000 (10x more elements),
  BST search total went up about 6.6x, the hash table about 3.5x. Per operation, the BST gets slower
  (deeper tree) and the hash table stays almost constant.
- Sorted input destroys the BST: 10,000 inserts took about 108 ms compared to about 2 ms for random keys.
  The hash table did not care about the order.
- The hash table is only fast if the table is resized. With 16 fixed buckets it became slower than the BST.
- At n = 100 all times are a few microseconds, so the difference is small and mostly noise.

## Why performance differs in practice

- **Memory / cache:** a BST follows pointers from node to node, nodes are spread around the heap, so there
  are many cache misses. A hash table jumps straight to one bucket and the chain is usually 1-2 nodes.
- **Number of steps:** a BST does about log2(n) comparisons (about 13 for 10,000), a hash table does one hash
  computation and about one comparison.
- **Balance:** this BST is not self-balancing, so its speed depends on the input order. A balanced tree
  (AVL, red-black) would guarantee O(log n).
- **Hash function and load factor:** the hash table is only O(1) if keys are spread evenly and the load
  factor is kept small. Resizing costs O(n) sometimes, which shows up in the insert times.
- **Order:** a BST keeps keys sorted, so it supports min, max, in-order traversal and range queries.
  A hash table cannot do these efficiently.
- **Measurement noise:** JIT compilation, garbage collection and the OS affect small timings,
  that is why I used warm-up runs and an average.

## Conclusion

If we only need insert / search / delete by key, the hash table is the better choice (O(1) average).
The BST is better when we need sorted order or range queries, but a plain BST can degrade to O(n)
on sorted input, so a balanced tree is safer.