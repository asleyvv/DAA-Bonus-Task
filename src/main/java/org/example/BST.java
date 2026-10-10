// Binary Search Tree with int keys (no duplicates)
// everything is iterative so a sorted input (very deep tree) can't blow the stack
public class BST {

    private class Node {
        int key;
        Node left, right;

        Node(int k) {
            key = k;
        }
    }

    private Node root;
    private int size;

    public int size() {
        return size;
    }

    // returns false if the key is already there
    public boolean insert(int key) {
        if (root == null) {
            root = new Node(key);
            size++;
            return true;
        }
        Node cur = root;
        while (true) {
            if (key == cur.key) return false;
            if (key < cur.key) {
                if (cur.left == null) {
                    cur.left = new Node(key);
                    size++;
                    return true;
                }
                cur = cur.left;
            } else {
                if (cur.right == null) {
                    cur.right = new Node(key);
                    size++;
                    return true;
                }
                cur = cur.right;
            }
        }
    }

    public boolean search(int key) {
        Node cur = root;
        while (cur != null) {
            if (key == cur.key) return true;
            if (key < cur.key) cur = cur.left;
            else cur = cur.right;
        }
        return false;
    }

    public boolean delete(int key) {
        // find the node and remember its parent
        Node parent = null;
        Node cur = root;
        while (cur != null && cur.key != key) {
            parent = cur;
            if (key < cur.key) cur = cur.left;
            else cur = cur.right;
        }
        if (cur == null) return false; // not found

        // case 3: two children -> copy the inorder successor's key here,
        // then delete the successor node instead (it has no left child)
        if (cur.left != null && cur.right != null) {
            Node sp = cur;
            Node s = cur.right;
            while (s.left != null) {
                sp = s;
                s = s.left;
            }
            cur.key = s.key;
            parent = sp;
            cur = s;
        }

        // case 1 and 2: zero or one child
        Node child = (cur.left != null) ? cur.left : cur.right;
        if (parent == null) root = child;
        else if (parent.left == cur) parent.left = child;
        else parent.right = child;

        size--;
        return true;
    }

    // height of the tree (level by level, no recursion)
    public int height() {
        if (root == null) return 0;
        java.util.ArrayDeque<Node> q = new java.util.ArrayDeque<>();
        q.add(root);
        int h = 0;
        while (!q.isEmpty()) {
            int count = q.size();
            for (int i = 0; i < count; i++) {
                Node n = q.poll();
                if (n.left != null) q.add(n.left);
                if (n.right != null) q.add(n.right);
            }
            h++;
        }
        return h;
    }
}