package com.strands.interview.task4;

public class MySingleton {
    private MySingleton instance;

    private MySingleton() {
        // Private constructor to prevent instantiation from outside
    }

    public MySingleton getInstance() {
        if (instance == null) {
            // First time, create the singleton instance
            synchronized(MySingleton.class) {
                if (instance == null) {
                    instance = new MySingleton();
                }
            }
        }
        return instance;
    }
}
