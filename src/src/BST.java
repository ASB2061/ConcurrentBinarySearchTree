package src;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.lang.Integer;

/***
 * Concurrent BST Plan:
 * Rep. Invariant:
 *  The parent is greater or equal to left child and less than the right child.
 * Features:
 *  Insertion Function
 *  Removal Function
 *  Contains Function
 * Enabling Concurrency (i.e. allowing multiple threads to insert safely, but serializing removal)
 *  Also need to prevent deadlock & starvation.
 *  If k threads are working on an object w/ n items, for n much larger than k, can all k threads run simultaneously
 *  when doing so would not create a problem?
 * Locking Rules
 *  When using insert, we must have some lock between nodes in BST so that we don't have two threads inserting at the
 *  same place at the same time.
 *  Need a way to switch between insert & remove.
 *      Could have a semaphore labeled "tree access" that determines who controls the tree. {How do we decide when to
 *      switch? Should we have some way to detect potential removal or inserter threads? Some kind of gate?}
 *  One way of thinking about this is looking at number of threads operating on tree at once. Say the max number of
 *  threads to operate on tree at once is size of tree. Then must switch if we see a thread looking to remove; can also
 *  verify that there are no other threads looking to insert prior to locking the tree.
 * @param <E>
 */
public class BST<E extends Comparable<E>> implements BinarySearchTree<E> {
    private Integer data; // the root's element
    private BST<Integer> leftSubTree; // reference to left subtree
    private BST<Integer> rightSubTree; // reference to right subtree
    private AtomicInteger size; // track the tree's size
    private AtomicInteger inserterCount; // track the number of inserters
    private AtomicInteger removerCount; // track the number of removers
    private AtomicBoolean treeLock;
    private Semaphore treeAccess;
    private Semaphore inserterLock; // used as our locks between nodes in the tree when inserting to prevent multiple
    // insertions in the same place at the same time.
    private Semaphore inserterTreeAccess; // this will determine who gets access to the tree.
    private Semaphore removerTreeAccess;

    public BST() {
        data = null;
        leftSubTree = null;
        rightSubTree = null;
        size = new AtomicInteger(0);
        inserterCount = new AtomicInteger(0);
        removerCount = new AtomicInteger(0);
        treeLock = new AtomicBoolean(false);
        treeAccess = new Semaphore(1);
        inserterLock = new Semaphore(1);
        inserterTreeAccess = new Semaphore(1);
        removerTreeAccess = new Semaphore(1);
    }
    public BST(int rootElement) {
        data = rootElement;
        leftSubTree = null;
        rightSubTree = null;
        size = new AtomicInteger(1);
        inserterCount = new AtomicInteger(0);
        removerCount = new AtomicInteger(0);
        treeLock = new AtomicBoolean(false);
        treeAccess = new Semaphore(1);
        inserterLock = new Semaphore(1);
        inserterTreeAccess = new Semaphore(1);
        removerTreeAccess = new Semaphore(1);
    }

    @Override
    public void insert(Integer element) throws InterruptedException {
        //inserterTreeAccess.acquire();
        treeStateInitialize(true, element);
        inserterCount.incrementAndGet();
        insertRecursive(element, null, null);
    }

    private void insertRecursive(Integer element, BST<Integer> parent, BST<Integer> superParent) {
        
    }

    @Override
    public void remove(Integer integer) throws InterruptedException {
        //removerTreeAccess.acquire();
        treeStateInitialize(false, integer);
        removerCount.incrementAndGet();
    }

    private void treeStateInitialize(boolean isInserter, Integer data) throws InterruptedException {
        if (treeLock.compareAndSet(false, true)) {
            if (isInserter) {
                inserterTreeAccess.acquire();
                removerTreeAccess.acquire();
                treeAccess.acquire();
            } else {
                inserterTreeAccess.acquire();
                removerTreeAccess.acquire();
                treeAccess.acquire();
            }
        } else {
            if (isInserter) {
                inserterTreeAccess.acquire();
            } else {
                removerTreeAccess.acquire();
            }
        }
    }
    @Override
    public Integer getRootElement() {
        return data;
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(E e) {
        return false;
    }

    @Override
    public boolean contains(Integer integer) {
        return false;
    }

    @Override
    public boolean isBST() {
        return false;
    }
}
