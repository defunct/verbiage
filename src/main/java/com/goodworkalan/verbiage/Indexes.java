package com.goodworkalan.verbiage;

/**
 * Utility classes to check the validity of object path parts.
 *
 * @author Alan Gutierrez
 */
public class Indexes {
    /**
     * Determine whether a string represents an integer.
     * 
     * @param name
     *            The integer string.
     * @return True if the string represents an integer.
     */
    public static boolean isInteger(String name) {
        for (int i = 0, stop = name.length(); i < stop; i++) {
            if (!Character.isDigit(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determine if the given name is a valid Java identifier.
     * 
     * @param name
     *            The name to check.
     * @return True if the name is valid Java identifier.
     * @exception NullPointerException
     *                If the name is null.
     */
    public static boolean checkJavaIdentifier(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (name.length() == 0) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            return false;
        }
        for (int i = 1, stop = name.length(); i < stop; i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
