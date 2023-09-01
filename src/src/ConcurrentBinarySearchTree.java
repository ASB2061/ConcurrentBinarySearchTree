package src;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class ConcurrentBinarySearchTree<E extends Comparable<E>> {
    private Node<E> rootNode;

    private class Node<E extends Comparable<E>> implements TreePrinter.PrintableNode {
        private E dataOfNode;
        private Node<E> leftChild;
        private Node<E> rightChild;

        private Semaphore nodeLock;

        private Node(E dataOfNode, Node<E> leftChild, Node<E> rightChild) {
            this.dataOfNode = dataOfNode;
            this.leftChild = leftChild;
            this.rightChild = rightChild;
            nodeLock = new Semaphore(1);
        }

        private void grab() {
            try {
                nodeLock.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private void letGo() {
            nodeLock.release();
        }

        private boolean hasLeftChild() {
            return leftChild != null;
        }

        private boolean hasRightChild() {
            return rightChild != null;
        }

        private boolean hasBothChildren() {
            return hasLeftChild() && hasRightChild();
        }

        private void setDataOfNode(E data) {
            this.dataOfNode = data;
        }

        private void setLeftChild(Node<E> node) {
            this.leftChild = node;
        }

        private void setRightChild(Node<E> node) {
            this.rightChild = node;
        }

        @Override
        public TreePrinter.PrintableNode getLeft() {
            return leftChild;
        }

        @Override
        public TreePrinter.PrintableNode getRight() {
            return rightChild;
        }

        @Override
        public String getText() {
            return dataOfNode.toString();
        }
    }

    public ConcurrentBinarySearchTree() {
    }

    public ConcurrentBinarySearchTree(E element) {
        rootNode = new Node<>(element, null, null);
    }

    public boolean isBST() {
        return true;
    }

    public void breadthFirstPrint() {
        TreePrinter.print(rootNode);
    }

    /***
     * @param element
     */
    public void insert(E element) throws InterruptedException {
        if (rootNode == null)
            rootNode = new Node<>(element, null, null);
        else
            insertRecursive(rootNode, null, element);
        System.out.println(element.toString() + " inserted into the tree.");
    }

    public void remove(E element) throws InterruptedException {
        if (rootNode != null)
            removeRecursive(rootNode, null, null, element);
        else
            System.out.println("Tree is empty.");
    }

    private void removeRecursive(Node<E> currentNode, Node<E> parentNode, Node<E> secondParentNode, E elementToRemove) throws InterruptedException {
        if (parentNode != null && secondParentNode != null && currentNode != null) {
            currentNode.grab();
            secondParentNode.nodeLock.release();
            compareForRemove(currentNode, parentNode, secondParentNode, elementToRemove);
        } else if (parentNode != null && secondParentNode == null && currentNode != null) {
            currentNode.grab();
            compareForRemove(currentNode, parentNode, null, elementToRemove);
        } else if (currentNode != null) {
            currentNode.grab();
            compareForRemove(currentNode, null, null, elementToRemove);
        } else {
            System.out.println("Node was null...");
            return;
        }
    }
    // a recursive method that locks on the way down, finds its target ... "extractAndReturnMinimum" Use that on the right
    // child. it will return the successor. As it returns upward, it will unlock and then return the value it got. You will
    // have a number in hand, and you will have removed what was down there.
    private void compareForRemove(Node<E> currentNode, Node<E> parentNode, Node<E> secondParentNode, E elementToRemove) throws InterruptedException {
        if (parentNode != null && currentNode != null) {
            if (elementToRemove.compareTo(currentNode.dataOfNode) == 0) {
                if (currentNode.hasBothChildren()) {
                    parentNode.letGo();
                    removeBothChildrenCaseRoot(currentNode, elementToRemove);
                    System.out.println(elementToRemove.toString() + " removed from the tree.");
                } else if (currentNode.hasLeftChild()) {
                    parentNode.letGo();
                    removeLeftChildCaseRoot(currentNode, elementToRemove);
                    System.out.println(elementToRemove.toString() + " removed from the tree.");
                } else if (currentNode.hasRightChild()) {
                    parentNode.letGo();
                    removeBothChildrenCaseRoot(currentNode, elementToRemove);
                    System.out.println(elementToRemove.toString() + " removed from the tree.");
                } else { // in this case we need to remove in place... so we disconnect it from the parent
                    removeWithNoChildren(parentNode.leftChild == currentNode, currentNode, parentNode);
                    System.out.println(elementToRemove.toString() + " removed from the tree.");
                }
            } else if (elementToRemove.compareTo(currentNode.dataOfNode) < 0) {
                if (currentNode.hasLeftChild())
                    removeRecursive(currentNode.leftChild, currentNode, parentNode, elementToRemove);
                else {
                    System.out.println(elementToRemove.toString() + " not removed");
                    currentNode.letGo();
                    parentNode.letGo();
                }
            } else {
                if (currentNode.hasRightChild())
                    removeRecursive(currentNode.rightChild, currentNode, parentNode, elementToRemove);
                else {
                    System.out.println(elementToRemove.toString() + " not removed");
                    currentNode.letGo();
                    parentNode.letGo();
                }
            }
        } else if (secondParentNode == null && parentNode == null && currentNode != null) {
            if (elementToRemove.compareTo(currentNode.dataOfNode) == 0) {
                if (currentNode.hasBothChildren() || currentNode.hasRightChild()) {
                    removeBothChildrenCaseRoot(currentNode, elementToRemove);
                    System.out.println(elementToRemove.toString() + " removed from the tree.");
                } else if (currentNode.hasLeftChild()) {
                    removeLeftChildCaseRoot(currentNode, elementToRemove);
                    System.out.println(elementToRemove.toString() + " removed from the tree.");
                } else {
                    if (this.rootNode.dataOfNode.compareTo(currentNode.dataOfNode) == 0 && !this.rootNode.hasRightChild() && !this.rootNode.hasLeftChild()) {
                        rootNode = null;
                    } else {
                        System.out.println("Current node has no parents, yet is unequal to the root node, unable to remove.");
                    }
                }
            } else if (elementToRemove.compareTo(currentNode.dataOfNode) < 0) {
                if (currentNode.hasLeftChild())
                    removeRecursive(currentNode.leftChild, currentNode, null, elementToRemove);
                else {
                    System.out.println(elementToRemove.toString() + " not removed");
                    currentNode.letGo();
                }
            } else if (elementToRemove.compareTo(currentNode.dataOfNode) > 0) {
                if (currentNode.hasRightChild())
                    removeRecursive(currentNode.rightChild, currentNode, null, elementToRemove);
                else {
                    System.out.println(elementToRemove.toString() + " not removed");
                    currentNode.letGo();
                }
            }
        } else {
            return;
        }
    }

    public void inOrderPrint() throws InterruptedException {
        if (rootNode != null) {
            System.out.print("[ ");
            inOrderPrintFrom(rootNode);
            System.out.println(" ]");
        }
    }

    void inOrderPrintFrom(Node<E> fromAnyNode) throws InterruptedException {
        if (fromAnyNode != null) {
            fromAnyNode.nodeLock.acquire();
            fromAnyNode.nodeLock.release();
            inOrderPrintFrom(fromAnyNode.leftChild);
            System.out.print(fromAnyNode.dataOfNode + ", ");
            inOrderPrintFrom(fromAnyNode.rightChild);
        }
    }

    /***
     * In this specific case, we to find the inOrder Successor of the node we want to remove, its right child, and its
     * parent.
     * @param nodeToRemove explanatory
     * @param elementToRemove   explanatory
     */
    private void removeBothChildrenCaseRoot(Node<E> nodeToRemove, E elementToRemove) {
        /* First Section: Create an array of semaphores holding the node that we are modifying its data in place, its
         * right child's semaphore. */
        ArrayList<Semaphore> semaphores = new ArrayList<>();
        semaphores.add(nodeToRemove.nodeLock); // get the root Semaphore
        nodeToRemove.rightChild.grab();
        semaphores.add(nodeToRemove.rightChild.nodeLock);
        /* Second Section: We create two 'pointers' at the right child of the node that we are modifying. */
        Node<E> inOrderSuccessor = nodeToRemove.rightChild;
        Node<E> iOSParent = nodeToRemove.rightChild;
        /* GOAL: Find the inOrderSuccessor of the nodeToRemove, and the successor's parent. Need to be able to hold each
         * semaphore we lock along the way? */
        if (inOrderSuccessor.hasLeftChild()) {
            /* If there is a left child, we grab its semaphore lock and acquire it. then add it to the semaphore list.
             * We make one of our pointers point to that Node. */
            inOrderSuccessor.leftChild.grab();
            semaphores.add(inOrderSuccessor.leftChild.nodeLock);
            inOrderSuccessor = inOrderSuccessor.leftChild;
            /* If there are more left children... we proceed with the same process, except that we set the parent to be
             * pointing to the parent node of the lower node. */
            while (inOrderSuccessor.hasLeftChild()) {
                inOrderSuccessor.leftChild.grab();
                semaphores.add(inOrderSuccessor.leftChild.nodeLock);
                inOrderSuccessor = inOrderSuccessor.leftChild;
                iOSParent = iOSParent.leftChild;
            }
            /* The last step. we change the parent node's left child to the right child of its left child meaning that
             * that node will no longer be referenced in the tree. We change the data of the node that we want to 'remove'
             * to the data of its in order successor. We then set the in order successor node's children to null, so it's
             * not still referencing anything in the tree. Then we set its element to the element that we wanted to
             * remove. Finally, we release any semaphores that were locked during the time we descended to find an in order
             * successor. */
            iOSParent.setLeftChild(inOrderSuccessor.rightChild);
            nodeToRemove.setDataOfNode(inOrderSuccessor.dataOfNode);
            inOrderSuccessor.setRightChild(null);
            inOrderSuccessor.setDataOfNode(elementToRemove);
            for (int j = semaphores.size() - 1; j >= 0; j--) {
                semaphores.get(j).release();
            }
        } else { /* If there was no left child of the right child of the node to remove, we can just make the swap,
        since that must be the in order successor of the node to remove. We set the node to remove's right child to
        the in order successor's right child, and we change its element to the element of the in order successor. Finally,
        we remove its references to any nodes in the tree. */
            nodeToRemove.setRightChild(inOrderSuccessor.rightChild);
            nodeToRemove.setDataOfNode(inOrderSuccessor.dataOfNode);

            inOrderSuccessor.setRightChild(null);
            inOrderSuccessor.setDataOfNode(elementToRemove);
            for (int j = semaphores.size() - 1; j >= 0; j--) {
                semaphores.get(j).release();
            }
        }
    }

    /* In this case we need to find the in order predecessor to restructure the tree.
     * @param nodeToRemove */
    private void removeLeftChildCaseRoot(Node<E> nodeToRemove, E elementToRemove) {
        /* Semaphore ArrayList: Grab the left child of the node that we are 'removing' but really modifying, we remove a
         * node lower on the tree... */
        ArrayList<Semaphore> semaphores = semaphoreSetupRemove(nodeToRemove, nodeToRemove.leftChild);

        Node<E> inOrderPredecessor, iOPParent;
        inOrderPredecessor = iOPParent = nodeToRemove.leftChild;
        /* GOAL: Find the inOrderPredecessor of the nodeToRemove, and its parent. Need to be able to hold each semaphore
         we lock along the way? */
        if (inOrderPredecessor.hasRightChild()) {
            inOrderPredecessor.rightChild.grab();
            semaphores.add(inOrderPredecessor.rightChild.nodeLock);
            inOrderPredecessor = inOrderPredecessor.rightChild;
            while (inOrderPredecessor.hasRightChild()) {
                inOrderPredecessor.rightChild.grab();
                semaphores.add(inOrderPredecessor.rightChild.nodeLock);
                inOrderPredecessor = inOrderPredecessor.rightChild;
                iOPParent = iOPParent.rightChild;
            }
            iOPParent.setRightChild(inOrderPredecessor.leftChild);
            nodeToRemove.setDataOfNode(inOrderPredecessor.dataOfNode);

            inOrderPredecessor.setLeftChild(null);
            inOrderPredecessor.setRightChild(null);
            inOrderPredecessor.setDataOfNode(elementToRemove);

            for (int j = semaphores.size() - 1; j >= 0; j--) {
                semaphores.get(j).release();
            }
        } else {
            /* When the initial left child is the in order predecessor, we can set its left child to the left child's left child.*/
            nodeToRemove.setLeftChild(inOrderPredecessor.leftChild); // point to the node to remove's child
            nodeToRemove.setDataOfNode(inOrderPredecessor.dataOfNode); // set the node to the in order predecessor data

            inOrderPredecessor.setLeftChild(null); // disconnect this node from pointing to anything in the tree.
            inOrderPredecessor.setDataOfNode(elementToRemove); // set it to the element that we were looking to remove
            nodeToRemove.letGo();
        }
    }

    /***
     * Case where we are removing a node with no children.
     * @param isLeftChild is the node we are removing the left nodeToRemove of the parent?
     * @param nodeToRemove the node we are removing
     * @param parent of the node to remove
     */
    private void removeWithNoChildren(boolean isLeftChild, Node<E> nodeToRemove, Node<E> parent) {
        if (isLeftChild) {
            parent.setLeftChild(null);
        } else {
            parent.setRightChild(null);
        }
        nodeToRemove.letGo();
        parent.letGo();
    }

    private boolean equals(Node<E> searchNode, E elementToRemove) {
        return searchNode.dataOfNode == elementToRemove;
    }

    private ArrayList<Semaphore> semaphoreSetupRemove(Node<E> nodeToRemove, Node<E> childNode) {
        ArrayList<Semaphore> semaphoreArrayList = new ArrayList<>();
        semaphoreArrayList.add(nodeToRemove.nodeLock); // get the root Semaphore
        nodeToRemove.leftChild.grab();
        semaphoreArrayList.add(nodeToRemove.leftChild.nodeLock);
        return semaphoreArrayList;
    }

    // insert support sub-methods
    private void insertRecursive(Node<E> currentNode, Node<E> parentNode, E elementToInsert) throws InterruptedException {
        if (parentNode == null) {
            currentNode.nodeLock.acquire();
            compareForInsert(currentNode, elementToInsert);
        } else {
            currentNode.nodeLock.acquire();
            parentNode.nodeLock.release();
            compareForInsert(currentNode, elementToInsert);
        }
    }

    private void compareForInsert(Node<E> currentNode, E elementToInsert) throws InterruptedException {
        if (elementToInsert.compareTo(currentNode.dataOfNode) <= 0) {
            childDetectionInsert(currentNode.leftChild != null, true, currentNode, elementToInsert);
        } else {
            childDetectionInsert(currentNode.rightChild != null, false, currentNode, elementToInsert);
        }
    }

    private void childDetectionInsert(boolean hasChild, boolean isLessOrEqual, Node<E> currentNode, E elementToInsert) throws InterruptedException {
        if (hasChild && isLessOrEqual) {
            insertRecursive(currentNode.leftChild, currentNode, elementToInsert);
        } else if (!hasChild && isLessOrEqual) {
            currentNode.leftChild = new Node<>(elementToInsert, null, null);
            currentNode.nodeLock.release();
        } else if (hasChild) {
            insertRecursive(currentNode.rightChild, currentNode, elementToInsert);

        } else {
            currentNode.rightChild = new Node<>(elementToInsert, null, null);
            currentNode.nodeLock.release();
        }
    }
}