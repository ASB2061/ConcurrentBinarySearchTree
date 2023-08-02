package src;

public class Inserter extends Thread {
    private final int writingData;
    private final ConcurrentBinarySearchTree<Integer> treeToWrite;

    public Inserter(ConcurrentBinarySearchTree<Integer> writingTree, int inputData) {
        writingData = inputData;
        treeToWrite = writingTree;
    }


    @Override
    public void run() {
        System.out.println("Looking to insert " + writingData + " into tree");
        try {
            treeToWrite.insert(writingData);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

