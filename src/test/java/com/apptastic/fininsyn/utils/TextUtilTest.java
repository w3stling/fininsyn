package com.apptastic.fininsyn.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TextUtilTest {

    @Test
    public void containsAny() {
        assertTrue(TextUtil.containsAny("aaa bbb ccc", "bbb"));
        assertFalse(TextUtil.containsAny("aaa bbb ccc", "ddd"));
    }


    @Test
    public void containsAll() {
        assertTrue(TextUtil.containsAll("aaa bbb ccc", "aaa", "bbb", "ccc"));
        assertFalse(TextUtil.containsAll("aaa bbb ccc", "aaa", "ddd"));
    }

    @Test
    public void containsAtLeast() {
        assertTrue(TextUtil.containsAtLeast("aaa bbb ccc", 2,"aaa", "ccc"));
        assertTrue(TextUtil.containsAtLeast("aaa bbb ccc", 3,"aaa", "bbb", "ccc"));
        assertTrue(TextUtil.containsAtLeast("aaa bbb ccc", 1,"aaa", "ddd"));
        assertFalse(TextUtil.containsAtLeast("aaa bbb ccc", 2,"aaa", "ddd"));
    }


    @Test
    public void endsWith() {
        assertTrue(TextUtil.endsWith("Ericsson AB","Plc", "AB"));
        assertFalse(TextUtil.endsWith("Ericsson AB","Inc", "Ltd"));
    }
}
