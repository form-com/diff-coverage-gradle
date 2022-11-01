package com.excludes;

public class CoveredClass {

    public int covered() {
        return 0;
    }

    public static class UncoveredNestedClass {
        public void uncoveredMethod() {
            System.out.println("uncovered method");
        }
    }

}
