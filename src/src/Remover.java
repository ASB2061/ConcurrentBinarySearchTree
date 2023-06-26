package src;

public class Remover extends Thread {
    private final int removalData;
    private final BST<Integer> removalTree;

    public Remover(BST<Integer> tree, int integerData) {
        removalData = integerData;
        removalTree = tree;
    }

    @Override
    public void run () {
        System.out.println("Looking to remove " + removalData + " from tree.");
        try {
            removalTree.remove(removalData);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
