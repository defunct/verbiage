package com.goodworkalan.verbiage;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.goodworkalan.verbiage.Indexes;

/**
 * Test cases for the Indexes class.
 * 
 * @author Alan Gutierrez
 */
public class IndexesTest {
    /**
     * Check a null Java identifier.
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void nullJavaIdentifier() {
        Indexes.checkJavaIdentifier(null);
    }

    /** Satisfy test coverage of default constructor. */
    @Test
    public void constructor() {
        new Indexes();
    }

    /**
     * Check invalid Java identifiers.
     */
    @Test
    public void invalidJavaIdentifier() {
        assertFalse(Indexes.checkJavaIdentifier(""));
        assertFalse(Indexes.checkJavaIdentifier("1"));
        assertFalse(Indexes.checkJavaIdentifier("a!"));
    }

    /**
     * Test the integer determination method.
     */
    @Test
    public void isInteger() {
        assertTrue(Indexes.isInteger("10"));
        assertFalse(Indexes.isInteger("!"));
    }

    /**
     * Check a valid Java identifier.
     */
    @Test
    public void validJavaIdentifier() {
        assertTrue(Indexes.checkJavaIdentifier("a"));
        assertTrue(Indexes.checkJavaIdentifier("ab"));
    }
}
