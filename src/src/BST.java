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
//        inserterTreeAccess = new Semaphore(1);
//        removerTreeAccess = new Semaphore(1);
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
//        inserterTreeAccess = new Semaphore(1);
//        removerTreeAccess = new Semaphore(1);
    }

    @Override
    public void insert(Integer element) throws InterruptedException {
        //inserterTreeAccess.acquire();
        // treeStateInitialize(true, element);
        // inserterCount.incrementAndGet();
        insertRecursive(element, null, null);
    }

    private void insertRecursive(Integer element, BST<Integer> parent, BST<Integer> superParent) {
        try {
            // size++;
            if (parent != null) {
                inserterLock.acquire(1);
                parent.inserterLock.release();
                if (data == null) { // if the element is null we can place it there.
                    data = element;
                    inserterLock.release();
                    insertExit(superParent);
                } else if (element.compareTo(data) > 0 && (rightSubTree == null)) { // if the element is greater and there is no
                    rightSubTree = new BST<>(element);
                    inserterLock.release();
                    insertExit(superParent);
                } else if (element.compareTo(data) > 0) { // if the element is greater
                    rightSubTree.insertRecursive(element, (BST<Integer>) this, superParent); // we recurse until there is an empty subtree to add the element
                } else if (element.compareTo(data) <= 0 && (leftSubTree == null)) { // if the element is lesser and there is no
                    // leftSubtree
                    leftSubTree = new BST<>(element);
                    inserterLock.release();
                    insertExit(superParent);
                } else if (element.compareTo(data) <= 0) { // if the element is lesser and there is a leftSubTree
                    leftSubTree.insertRecursive(element, (BST<Integer>) this, superParent); // we recurse until there is an empty subtree to add the element
                }
            } else {
                inserterLock.acquire(1);
                if (data == null) { // if the element is null we can place it there.
                    data = element;
                    inserterLock.release();
                    insertExit((BST<Integer>) this);
                } else if (element.compareTo(data) > 0 && (rightSubTree == null)) { // if the element is greater and there is no subtree
                    rightSubTree = new BST<>(element);
                    inserterLock.release();
                    insertExit((BST<Integer>) this);
                } else if (element.compareTo(data) > 0) { // if the element is greater
                    rightSubTree.insertRecursive(element, (BST<Integer>) this, (BST<Integer>) this); // we recurse until there is an empty subtree to add the element
                } else if (element.compareTo(data) <= 0 && (leftSubTree == null)) { // if the element is lesser and there is no
                    // leftSubtree
                    leftSubTree = new BST<>(element);
                    inserterLock.release();
                    insertExit((BST<Integer>) this);
                } else if (element.compareTo(data) <= 0) { // if the element is lesser and there is a leftSubTree
                    leftSubTree.insertRecursive(element, (BST<Integer>) this, (BST<Integer>) this); // we recurse until there is an empty subtree to add the element
                }
            }
        } catch (InterruptedException j) {
        }
    }

    private void insertExit(BST<Integer> superTree) {
        superTree.inserterCount.decrementAndGet();
        if (superTree.inserterCount.compareAndSet(0, 0)) {
            // superTree.removerTreeAccess.release();
        }
    }

    @Override
    public void remove(Integer e) throws InterruptedException {
        //removerTreeAccess.acquire();
        // treeStateInitialize(false, integer);
        removerCount.incrementAndGet();
        removeRecursive(e, null, null);
    }
    private void removeRecursive(Integer e, BST<Integer> parent, BST<Integer> superParent) {
        try {
            if (data == null) { // if the root is already null then we can't do anything
                return;
            } else if (data.compareTo(e) > 0) { // if root is greater than the element we are searching for, we look in the
                // left sub tree.

                leftSubTree.removeRecursive(e, (BST<Integer>) this, (BST<Integer>) this);
            } else if (data.compareTo(e) < 0) { // if root is less than the element we are searching for we look in the right
                // sub tree

                rightSubTree.removeRecursive(e, (BST<Integer>) this, (BST<Integer>) this);
            } else if (data.compareTo(e) == 0) { // we have found the node that we were looking for (I still haven't found what I'm looking for ... U2)
                if (leftSubTree == null && rightSubTree == null) {
                    size--;
                    try {
                        data = null;
                    } catch (NullPointerException g) {
                    }
                    return;
                } else if (leftSubTree == null) { // if there is no left node.
                    size--;
                    data = rightSubTree.data;
                    leftSubTree = rightSubTree.leftSubTree;
                    rightSubTree = rightSubTree.rightSubTree;
                    return;
                } else if (rightSubTree == null) { // if there is no right node.
                    size--;
                    data = leftSubTree.data;
                    rightSubTree = leftSubTree.rightSubTree;
                    leftSubTree = leftSubTree.leftSubTree;
                    return;
                } else { // if there are two nodes
                    size--;
                    data = minValue(rightSubTree).getRootElement(); // we get the lowest node on the right subtree
                    minValue(rightSubTree).data = null; // remove the old val and turn it null
                    return;
                    // rightSubTree.data = remove(rightSubTree.data);
                }
            }
        } catch (NullPointerException g) {

        }
    }

//    private void treeStateInitialize(boolean isInserter, Integer data) throws InterruptedException {
//        if (treeLock.compareAndSet(false, true)) {
//            if (isInserter) {
//                inserterTreeAccess.acquire();
//                removerTreeAccess.acquire();
//                treeAccess.acquire();
//            } else {
//                inserterTreeAccess.acquire();
//                removerTreeAccess.acquire();
//                treeAccess.acquire();
//            }
//        } else {
//            if (isInserter) {
//                inserterTreeAccess.acquire();
//            } else {
//                removerTreeAccess.acquire();
//            }
//        }
//    }

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

//    public boolean contains(E e) {
//        return false;
//    }

    @Override
    public boolean contains(Integer integer) {
        try {
            if (data == null) {
                return false;
            } else {
                if (data.compareTo(integer) > 0 && leftSubTree != null) {
                    return leftSubTree.contains(integer);
                } else if (data.compareTo(integer) < 0 && rightSubTree != null) {
                    return rightSubTree.contains(integer);
                } else if (data.compareTo(integer) == 0) {
                    return true;
                }
            }
        } catch (NullPointerException g) {
        }
        return false;
    }

    /***
     * inOrder and inOrderRec were also developed using sample code from geeksForGeeks
     */
    void inOrder() {
        inOrderRec((BST<Integer>) this);
    }

    void inOrderRec(BST<Integer> inputBST) {
        try {
            if (inputBST.data != null) {
                inOrderRec(inputBST.leftSubTree);
                System.out.print(inputBST.data + "    ");
                inOrderRec(inputBST.rightSubTree);
            }
        } catch (NullPointerException e) {
        }
    }

    @Override
    public boolean isBST() {
        if (leftSubTree != null && rightSubTree != null) {
            return leftSubTree.isBSTGoLeft(data) && rightSubTree.isBSTGoRight(data);
        } else if (leftSubTree != null) {
            return leftSubTree.isBSTGoLeft(data);
        } else if (rightSubTree != null) {
            return rightSubTree.isBSTGoRight(data);
        }
        return data != null;
    }

    public boolean isBSTGoRight(Integer min) {
        if (data != null) {
            if (leftSubTree != null && rightSubTree != null) {
                return (data.compareTo(min) >= 0) && leftSubTree.isBSTWithBothBounds(min, data) && rightSubTree.isBSTGoRight(data) && data != null;
            } else if (leftSubTree != null) {
                return (data.compareTo(min) >= 0) && leftSubTree.isBSTWithBothBounds(min, data) && data != null;
            } else if (rightSubTree != null) {
                return (data.compareTo(min) >= 0) && rightSubTree.isBSTGoRight(data) && data != null;
            }
            return data.compareTo(min) >= 0;
        }
        return false;
    }


    public boolean isBSTGoLeft(Integer max) {
        if (data != null) {
            if (leftSubTree != null && rightSubTree != null) {
                return (data.compareTo(max) <= 0) && leftSubTree.isBSTGoLeft(data) && rightSubTree.isBSTWithBothBounds(data, max) && data != null;
            } else if (leftSubTree != null) {
                return (data.compareTo(max) <= 0) && leftSubTree.isBSTGoLeft(data) && data != null;
            } else if (rightSubTree != null) {
                return (data.compareTo(max) <= 0) && rightSubTree.isBSTWithBothBounds(data, max) && data != null;
            }
            return (data.compareTo(max) <= 0);
        }
        return false;
    }


    public boolean isBSTWithBothBounds(Integer min, Integer max) {
        if (data != null) {
            if (leftSubTree != null && rightSubTree != null) {
                return (data.compareTo(max) <= 0 && data.compareTo(min) >= 0 && leftSubTree.isBSTWithBothBounds(min, data) && rightSubTree.isBSTWithBothBounds(data, max));
            } else if (leftSubTree != null) {
                return (data.compareTo(max) <= 0 && data.compareTo(min) >= 0 && leftSubTree.isBSTWithBothBounds(min, data));
            } else if (rightSubTree != null) {
                return (data.compareTo(max) <= 0 && data.compareTo(min) >= 0 && rightSubTree.isBSTWithBothBounds(data, max));
            }
            return data.compareTo(max) <= 0 && data.compareTo(min) >= 0;
        }
        return false;
    }
}
