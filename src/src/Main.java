package src;

import java.util.Random;

import static java.lang.Math.abs;

public class Main {

    public static void main(String[] args) {
        try {
            ConcurrentBinarySearchTree<Integer> sampleIntegerTree = new ConcurrentBinarySearchTree<>();
            multiThreadedTesting(sampleIntegerTree);
            tryToSleep(5, 7);
            sampleIntegerTree.inOrderPrint();
            System.out.print("------------------------------------------------------------------------------------------------");
            // testingSuite();
        } catch (Exception e) {
            System.out.println("failed");
        }
    }

    public static void multiThreadedTesting(ConcurrentBinarySearchTree inputTree) throws InterruptedException {
        Random random = new Random();
        try {
            int treeSize = 1000;
            int nRemoves = 900;
            int[] nums = new int[treeSize];
            for (int num = 0; num < treeSize; num++) {
                nums[num] = random.nextInt(500);
            }
            for (int i = 0; i < treeSize; i++) {
                Inserter newInserter = new Inserter(inputTree, nums[i]);
                newInserter.start();
//                try {
//                    newInserter.join(); // asks the thread once
//                } catch (Exception e) {
//                    System.out.println("Just in case?");
//                }
                // sampleIntegerTree.insert(nums[i]);
            }
            System.out.println("About to print...");
            inputTree.inOrderPrint();
            System.out.println("Finished printing");
            for (int j = 0; j < nRemoves; j++) {
                Remover newRemover = new Remover(inputTree, nums[random.nextInt(treeSize)]);
                newRemover.start();
                // tryToSleep(0.3, 0.7);
//                try {
//                    newRemover.join();
//                } catch (Exception e) {
//                    System.out.println("Maybe?");
//                }
//                sampleIntegerTree.remove(nums[random.nextInt(treeSize)]);
            }
            inputTree.inOrderPrint();
        } catch (NullPointerException e) {
        }
    }

    public static void testingSuite() throws InterruptedException {
        ConcurrentBinarySearchTree<Integer> testingConcurrentBST = new ConcurrentBinarySearchTree<>();
        Random random = new Random();
        int[] nums = {100, 75, 40, 100, 190, 150, 30, 45};
        for (int num : nums) {
            testingConcurrentBST.insert(num);
        }
        tryToSleep(0.5, 1.5);
        for (int num : nums) {
            System.out.println("Removing " + num);
            testingConcurrentBST.remove(num);
            tryToSleep(0.3, 0.7);
            testingConcurrentBST.breadthFirstPrint();
        }
    }

    private static final Random dice = new Random(); // random number generator, for delays mostly

    public static void tryToSleep(double secMin, double secVar) {
        try {
            java.lang.Thread.sleep(Math.round(secMin * 1000) + Math.round(dice.nextDouble() * (secVar) * 1000));
        } catch (InterruptedException e) {
            System.out.println("Not Handling interruptions yet ... just going on with the program without as much sleep as needed ... how appropriate!");
        }
    }
}