package com.java.test;

import org.junit.Test;

import static org.junit.Assert.*;

public class Class1Test {

    private Class1 class1 = new Class1();

    @Test
    public void coveredShouldReturn1() {
        int covered = class1.covered(true);
        assertEquals(1, covered);
    }

    @Test
    public void coveredShouldReturn0() {
        int covered = class1.covered(false);
        assertEquals(0, covered);
    }

    @Test
    public void partialCoveredShouldReturn1() {
        int covered = class1.partialCovered(true);
        assertEquals(1, covered);
    }
}
