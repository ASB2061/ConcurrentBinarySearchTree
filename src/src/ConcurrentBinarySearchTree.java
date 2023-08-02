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

        private void nullify() {
            this.dataOfNode = null;
            this.leftChild = null;
            this.rightChild = null;
            nodeLock = null;
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

    public void breadthFirstPrint() {
        TreePrinter.print(rootNode);
    }

    /***
     * Brief Note: Need to add case for when the rootNode is empty ... aka ... has not been initialized. I need a way
     * to detect an error for when the field hasn't been initialized and then to catch that exception and just call
     * the Node constructor.
     * @param element
     */
    public void insert(E element) throws InterruptedException {
        if (rootNode == null)
            rootNode = new Node<>(element, null, null);
        else
            insertRecursive(rootNode, null, element);
    }

    public void remove(E element) throws InterruptedException {
        if (rootNode != null)
            removeRecursive(rootNode, null, null, element);
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
            return;
        }
    }

    private void compareForRemove(Node<E> currentNode, Node<E> parentNode, Node<E> secondParentNode, E elementToRemove) throws InterruptedException {
        if (secondParentNode != null && parentNode != null && currentNode != null) {
            if (elementToRemove.compareTo(currentNode.dataOfNode) == 0) {
                removalCases(parentNode.leftChild == currentNode, ((byte) (((currentNode.leftChild != null) ? 1 : 0)
                        + ((currentNode.rightChild != null) ? 1 : 0))), currentNode, parentNode, elementToRemove);
            } else if (elementToRemove.compareTo(currentNode.dataOfNode) < 0) {
                childDetectionRemoveRecurse(currentNode.leftChild != null, true, currentNode, parentNode, secondParentNode, elementToRemove);
            } else {
                childDetectionRemoveRecurse(currentNode.rightChild != null, false, currentNode, parentNode, secondParentNode, elementToRemove);
            }
        } else if (secondParentNode == null && parentNode != null && currentNode != null) {
            if (elementToRemove.compareTo(currentNode.dataOfNode) == 0) {

            } else if (elementToRemove.compareTo(currentNode.dataOfNode) < 0) {

            } else {

            }
        } else if (secondParentNode == null && parentNode == null && currentNode != null) {
            if (elementToRemove.compareTo(currentNode.dataOfNode) == 0) {
                if (currentNode.hasBothChildren()) {
                    removeBothChildrenCase(currentNode, elementToRemove);
                } else if (currentNode.hasLeftChild()) {

                } else if (currentNode.hasRightChild()) {

                } else {

                }
            } else if (elementToRemove.compareTo(currentNode.dataOfNode) < 0) {
                if (currentNode.hasLeftChild())
                    removeRecursive(currentNode.leftChild, currentNode, null, elementToRemove);
                else
                    System.out.println(elementToRemove.toString() + " not removed");
            } else if (elementToRemove.compareTo(currentNode.dataOfNode) > 0) {
                if (currentNode.hasRightChild())
                    removeRecursive(currentNode.rightChild, currentNode, null, elementToRemove);
                else
                    System.out.println(elementToRemove.toString() + " not removed");
            }
        } else {
            return;
        }
    }

    private void removeBothChildrenCase (Node<E> currentNode, E elementToRemove) {
        // ArrayList<Semaphore> semaphoreStack = new ArrayList<>(); // ? might need this ?
        currentNode.rightChild.grab();
        Node<E> searchNode = currentNode.rightChild;
        while (searchNode.leftChild != null) {
            searchNode.leftChild.grab();
            searchNode.letGo();
            searchNode = searchNode.leftChild;
        }
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

//    private void childDetectionRemoveRecurse(boolean hasChild, boolean isLess, Node<E> currentNode, E elementToRemove) throws InterruptedException {
//        if (hasChild && isLess)
//            removeRecursive(currentNode.leftChild, currentNode, elementToRemove);
//        else if (!hasChild && isLess)
//            return;
//        else if (hasChild) // !isLess
//            removeRecursive(currentNode.rightChild, currentNode, elementToRemove);
//        else
//            return;
//    }
//
//    private void childDetectionRemoveRecurse(boolean hasChild, boolean isLess, Node<E> currentNode, Node<E> parentNode, Node<E> secondParentNode, E elementToRemove) throws InterruptedException {
//        if (hasChild && isLess)
//            removeRecursive(currentNode.leftChild, currentNode, parentNode, elementToRemove);
//        else if (!hasChild && isLess)
//            return;
//        else if (hasChild) // !isLess
//            removeRecursive(currentNode.rightChild, currentNode, parentNode, elementToRemove);
//        else
//            return;
//    }

// private void removalCases(boolean isLeftChild, byte childCount, Node<E> currentNode, Node<E> parentNode, E elementToRemove) {
//        switch (childCount) {
//            case 0 -> {
//                if (isLeftChild) {
//                    parentNode.leftChild = null;
//                    parentNode.nodeLock.release();
//                } else {
//                    parentNode.rightChild = null;
//                    parentNode.nodeLock.release();
//                }
//            }
//            case 1 -> {
//            }
//            case 2 -> {
//            }
//        }
//    }

//    private void removeRecursive(Node<E> currentNode, Node<E> parentNode, E elementToRemove) throws InterruptedException {
//        if (parentNode != null) {
//            currentNode.nodeLock.acquire();
//            // parentNode.nodeLock.release();
//            compareForRemove(currentNode, parentNode, elementToRemove);
//        } else {
//            currentNode.nodeLock.acquire();
//            compareForRemove(currentNode, elementToRemove);
//        }
//    }
