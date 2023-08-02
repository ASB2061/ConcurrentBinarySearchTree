package src;

import java.util.Random;

public class Main {

    public static void main(String[] args) {
        singleThreadedTesting();
        try {
        testingSuite();
        } catch (InterruptedException e) {
            System.out.println("failed");
        }
    }

    public static void singleThreadedTesting() {
        Random random = new Random();
        ConcurrentBinarySearchTree<Integer> sampleIntegerTree = new ConcurrentBinarySearchTree<>();
        try {
            int treeSize = 50;
            int[] nums = new int[treeSize];
            for (int num = 0; num < treeSize; num++) {
                nums[num] = random.nextInt(50000);
            }
            for (int i = 0; i < treeSize; i++) {
                Inserter newInserter = new Inserter(sampleIntegerTree, nums[i]);
                newInserter.start();
                try {
                    newInserter.join();
                } catch (Exception e) {
                    System.out.println("Just in case?");
                }
            }
            // sampleIntegerTree.inOrder();

//            for (int j = 0; j < 1; j++) {
//                Remover newRemover = new Remover(sampleIntegerTree, nums[random.nextInt(treeSize)]);
//                newRemover.start();
//                try {
//                    newRemover.join();
//                } catch (Exception e) {
//                    System.out.println("Maybe?");
//                }
//            }
            sampleIntegerTree.breadthFirstPrint();
        } catch (NullPointerException e) {
        }

        // sampleIntegerTree.inOrder();
    }

    public static void testingSuite () throws InterruptedException {
        ConcurrentBinarySearchTree<Integer> testingConcurrentBST = new ConcurrentBinarySearchTree<>();
        Random random = new Random();
        int treeSize = 10;
        int[] nums = new int[treeSize];
        for (int num = 0; num < treeSize; num++) {
            nums[num] = random.nextInt(500);
        }
        for (int i = 0; i < treeSize; i++) {
            testingConcurrentBST.insert(nums[i]);
        }
        testingConcurrentBST.breadthFirstPrint();
    }
}