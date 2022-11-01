package com.excludes;

import com.excludes.CoveredClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CoveredClassTest {

    @Test
    public void coveredShouldReturn0() {
        int covered = new CoveredClass().covered();
        assertEquals(0, covered);
    }

}
