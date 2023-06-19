package src;

import java.util.concurrent.atomic.AtomicInteger;

public interface BinarySearchTree<E> {
    void insert(Integer element) throws InterruptedException; // inserts an element into the tree, maintaining the binary search tree structure.

    void remove(Integer integer) throws InterruptedException; // deletes an element in the tree, maintaining a binary search tree.

    // void insert(E element);

    // void remove(E e);

    Integer getRootElement(); // returns the root of the BST

    int size(); // returns the number of nodes in a BST

    boolean isEmpty(); // tells us whether the BST is empty

    boolean contains(E e); // checks for an element in the BST

    boolean contains(Integer integer);

    boolean isBST(); // verifies whether the invariant of the binary search tree is represented properly

}
