package src;

import java.util.Random;

public class Main {

    public static void main(String[] args) {
        singleThreadedTesting();
    }

    public static void singleThreadedTesting() {
        Random random = new Random();
        BST<Integer> sampleIntegerTree = new BST<>();
        try {
            for (int i = 0; i < 50; i++) {
                Inserter newInserter = new Inserter(sampleIntegerTree, random.nextInt(500)); // we create a new inserter thread
                // that will try to insert a random integer between 0 & 500
                newInserter.start();
                try {
                    newInserter.join();
                } catch (Exception e) {
                    System.out.println("Just in case?");
                }
            }
        } catch (NullPointerException e) {}
        sampleIntegerTree.inOrder();
    }
}