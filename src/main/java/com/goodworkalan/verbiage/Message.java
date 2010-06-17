package com.goodworkalan.verbiage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

/**
 * A sprintf formatted internationalized message. The message uses an message
 * that contains the sprintf format strings. Internationalization is obtained by
 * creating new message bundles for new locales. If a language requires that the
 * message display its parameters in a different order, sprintf formatting can
 * handle it.
 * <p>
 * The message accepts a concurrent hash map that is used to cache the message
 * bundles so they do not have to be loaded for each message. The cache is
 * provided as an argument so that derived classes can keep a static reference
 * to a single instance of the bundle map. In this way, if the derived class is
 * reloaded within an application container, but the message class is not, the
 * old bundle will become unreachable when the class becomes unreachable and the
 * resource bundle will be collected with the derived class. If a class loader
 * loads the derived classes that reference the same resource bundle path, the
 * resource bundle will be reloaded since the old cache is unreachable.
 * <p>
 * The context string is used to determine the package to used to find the
 * resource bundle. The context string is assumed to be a canonical class name.
 * A string is used instead of the class itself, because sometimes the class
 * name could be obtained from an existing logging configuration, such as from a
 * Log4J logger. No attempt is made to validate the class name other than to
 * attempt to fetch a bundle with it.
 * <p>
 * A the name of a class in the default package cannot be used as a context
 * string.
 * <p>
 * The bundle name can be specified so that the message can be used by different
 * aspects of a program pulling different bundles from the same package. That
 * is, you might have <code>com.yoyodyne.utility.exceptions.properties</code>
 * and <code>com.yoyodyne.utility.stderr.properties</code>, one bundle for
 * exception messages and one bundle for messages written to standard error.
 * <p>
 * The variables reference a map of variables. The variables within the map can
 * be used in the sprintf messages or they can be ignored. The variables are
 * selected from the map using a comma separated list of dotted object paths.
 * <p>
 * <code>1101: threadId,duration~The launch sequence in thread %d lasted %10.3f seconds.</code>
 * <p>
 * The above entry in a resource bundle would select the keys
 * <code>threadId</code> and <code>duration</code> from the map of variables and
 * pass them to sprintf in that order.
 * <p>
 * As noted, you can select items using a dotted object path that will
 * dereference items in a nested graph of maps and lists where objects are
 * leaves. The dotted path language will not evaluate public fields or bean
 * property getters.
 * <p>
 * <code>1102: manager.lastName,employee.lastName~The manager %s does not manage the employee %s.</code>
 * <p>
 * In the above example, <code>manager</code> and <code>employee</code> are maps
 * in the variables map. Both have a value mapped to the <code>lastName</code>
 * key.
 * <p>
 * Lists and arrays can be dereferenced using an array or list index for the
 * path part.
 * <p>
 * <code>1103: sort.0,sort.1~Unable to compare %s to %s.</code>
 * <p>
 * If no arguments are selected for use in a message, then the message format is
 * not run through sprintf and is returned as is.
 * 
 * @author Alan Gutierrez
 */
public class Message {
    /** The resource bundle from which to read the message format. */
    private final ResourceBundle bundle;
    
    /**
     * The notice context, which is a class name, but it is not a class, because
     * the class name is sometimes obtained from a SLF4J logger, which converts
     * a class name into a string for use as its own name.
     */
    private final String context;
    
    /** The bundle name to be appended to the context package. */
    private final String bundleName;
    
    /** The key of the message in the resource bundle. */
    private final String messageKey;

    /** The map of object variables. */
    private final Map<?, ?> variables;

    /**
     * <p>
     * The context must always be qualified, it must reference a package other
     * than the default package.
     * <p>
     * Positioned arguments are translated into the named parameters named after
     * their position in the variable portion of the argument list. The first
     * parameter is named <code>$1</code>, the second is named <code>$2</code>,
     * the third is named <code>$3</code>, and so on. The parameters are
     * inserted into the given object map at the root level.
     * <p>
     * You reference the positioned arguments using the translated name as you
     * would reference any named parameter.
     * 
     * <code><pre>
     * 1011: $1,$2~File %s not found while running module %s.
     * </pre></code>
     * 
     * <p>
     * You can simply specify that the format should use the list of positioned
     * arguments in order by using the special named parameter <code>$@</code>.
     * The <code>$@</code> parameter is expanded into <code>$1</code>,
     * <code>$2</code>, <code>$3</code>, etc. stopping at the first parameter
     * for which <code>containsKey</code> returns <code>false</code>.
     * 
     * <code><pre>
     * 1011: $@~File %s not found while running module %s.
     * </pre></code>
     * 
     * You can add named parameters before or after <code>$@</code>.
     * 
     * <code><pre>
     * 1011: $@,module~File %s not found in ~%s/foo while running module %s.
     * </pre></code>
     * 
     * @param context
     *            The message bundle context.
     * @param bundleName
     *            The message bundle file name.
     * @param messageKey
     *            The message key.
     * @param variables
     *            The map of variables.
     */
    public Message(String context, String bundleName, String messageKey, Map<?,?> variables) {
        this.context = context;
        this.bundleName = bundleName;
        this.variables = variables;
        this.messageKey = messageKey;
        this.bundle = getResourceBundle(context, bundleName); 
    }

	/**
	 * Get the resource bundle for the given context and bundle name.
	 * 
	 * @param context
	 *            The context.
	 * @param bundleName
	 *            The bundle name.
	 * @return The resource bundle or null if the resource bundle cannot be
	 *         loaded.
	 */
    private static ResourceBundle getResourceBundle(String context, String bundleName) {
    	try {
    		return ResourceBundle.getBundle(getBundlePath(context, bundleName), Locale.getDefault(), Thread.currentThread().getContextClassLoader());
    	} catch (MissingResourceException e) {
    		return null;
    	}
    }

    /**
     * Add the given positioned parameters to the given argument map.
     * 
     * @param arguments
     *            The map of variables.
     * @param positioned
     *            The array of positioned arguments.
     * @return The given arguments map .
     */
    public static Map<Object, Object> position(Map<Object, Object> arguments, Object...positioned) {
        for (int i = 0, stop = positioned.length; i < stop; i++) {
            arguments.put("$" + (i + 1), positioned[i]);
        }
        return arguments;
    }

    /**
     * Get the name of the resource bundle.
     * 
     * @return The resource bundle name.
     */
    public String getBundleName() {
        return bundleName;
    }

    /**
     * Get the key of the message in the resource bundle.
     * 
     * @return The key of the message in the resource bundle.
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * Get the package in which to look for the resource bundle.
     * 
     * @return The resource bundle package.
     */
    public String getContext() {
        return context;
    }

    /**
     * Get the primitive argument tree.
     * 
     * @return The primitive argument tree.
     */
    public Map<?, ?> getVariables() {
        return variables;
    }

    /**
     * Evaluate the given path against the argument structure.
     * 
     * @param path
     *            The path.
     * @return The value found by navigating the path or null if the path does
     *         not exist.
     * @exception IllegalArgumentException
     *                If any part of the given path is not a valid Java
     *                identifier or list index.
     */
    private Object getValue(String path) {
        int start = -1, end, stop = path.length();
        Object current = variables;
        while (start != stop) {
            start++;
            end = path.indexOf('.', start);
            if (end == -1) {
                end = stop;
            }
            String name = path.substring(start, end);
            start = end;
            if (current instanceof Map<?, ?>) {
                if (!Indexes.checkJavaIdentifier(name)) {
                    throw new IllegalArgumentException();
                }
                current = ((Map<?, ?>) current).get(name);
            } else if (current instanceof List<?>) {
                int index = asIndex(name);
                List<?> list = (List<?>) current;
                if (index >= list.size()) {
                    throw new NoSuchElementException();
                }
                current = list.get(index);
            } else if (current != null && current.getClass().isArray()) {
                int index = asIndex(name);
                Object[] array = (Object[]) current;
                if (index >= array.length) {
                    throw new NoSuchElementException();
                }
                current = array[index];
            } else {
                throw new NoSuchElementException();
            }
        }
        return current;
    }

    /**
     * Convert the path part to an integer index and raise an exception if it is
     * not a valid integer index.
     * 
     * @param name
     *            The path part.
     * @return The integer value.
     * @exception NoSuchElementException
     *                If the name is not an integer index but is a valid Java
     *                identifier.
     * @exception If
     *                the name is neither an integer index nor a valid Java
     *                identifier.
     */
    private int asIndex(String name) {
        if (!Indexes.isInteger(name)) {
            if (!Indexes.checkJavaIdentifier(name)) {
                throw new IllegalArgumentException();
            }
            throw new NoSuchElementException();
        }
        return Integer.parseInt(name, 10);
    }

    /**
     * Get the value in the report structure at the given path.
     * 
     * @param path
     *            The path.
     * @return The value found by navigating the path or null if the path does
     *         not exist.
     * @exception IllegalArgumentException
     *                If any part of the given path is not a valid Java
     *                identifier or list index.
     */
    public Object get(String path) {
        try {
            return getValue(path);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * Convert a class to its class name for format output. I don't like
     * the prefix of "class" or "interface."
     * 
     * @param value
     *            The object to convert.
     * @return The object or the class name if the object is class.
     */
    private Object convertClasses(Object value) {
        // My personal preference.
        if (value instanceof Class<?>) {
            return ((Class<?>) value).getName();
        }
        return value;
    }

	/**
	 * Generate a bundle path from the given context and bundle name. The
	 * context is treated as fully qualified class name of a class that is not
	 * nested, so that the bundle path is created by combining the package name
	 * of the class with the bundle name. With a context of
	 * <code>com.acme.Account</code> and a bundle name of
	 * <code>exceptions</code> the generated bundle name would be
	 * <code>com.acme.exceptions</code>.
	 * 
	 * @param context
	 *            The context.
	 * @param bundleName
	 *            The bundle name.
	 * @return The generated bundle name.
	 */
    private static String getBundlePath(String context, String bundleName) {
        String bundlePath = "com.goodworkalan.verbiage.package";
        int dot = context.lastIndexOf('.');
        if (dot != -1) {
            String packageName = context.substring(0, dot);
            bundlePath = packageName + "." + bundleName;
        }
        return bundlePath;
    }

    /**
     * Generate the formatted message.
     */
    public String toString() {
        String key = messageKey;
        String bundlePath = getBundlePath(context, bundleName);
        if (bundlePath.equals("com.goodworkalan.verbiage.package")) {
            return message("defaultPackage", context, key);
        }
        if (bundle == null) {
            return message("missingBundle", bundlePath, key);
        }
        String format;
        try {
            format = bundle.getString(key);
        } catch (MissingResourceException e) {
            return message("missingKey", key, bundlePath);
        }
        format = format.trim();
        if (format.length() == 0) {
            return message("blankMessage", key, bundlePath);
        }
        int tilde = format.indexOf("~");
        if (tilde == -1) {
            return format;
        }
        String paths = format.substring(0, tilde);
        format = format.substring(tilde + 1);
        int length = 0, index = 0;
        for (;;) {
            length++;
            index = paths.indexOf(',', index);
            if (index == -1) {
                break;
            }
            index += 1;
        }
        Object[] arguments = new Object[length];
        int start = -1, end, stop = paths.length(), position = 0;
        while (start != stop) {
            start++;
            end = paths.indexOf(',', start);
            if (end == -1) {
                end = stop;
            }
            String name = paths.substring(start, end);
            start = end;
            if (name.equals("$@")) {
                List<Object> positioned = new ArrayList<Object>();
                Map<?, ?> map = variables;
                for (int i = 1; map.containsKey("$" + i); i++) {
                    positioned.add(convertClasses(map.get("$" + i)));
                }
                Object[] resized = new Object[arguments.length + positioned.size() - 1];
                System.arraycopy(arguments, 0, resized, 0, position);
                arguments = resized;
                for (int i = 0; i < positioned.size(); i++) {
                    arguments[position++] = positioned.get(i);
                }
            } else {
                Object argument = "";
                try {
                    argument = convertClasses(getValue(name));
                } catch (IllegalArgumentException e) {
                    return message("badFormatArgument", name, key, bundlePath);
                } catch (NoSuchElementException e) {
                    return message("missingArgument", name, key, bundlePath);
                }
                arguments[position++] = argument;
            }
        }
        try {
            return String.format(format, arguments);
        } catch (RuntimeException e) {
            return message("formatException", e.getMessage(), key, bundlePath);
        }
    }

	/**
	 * Generate a meta error message.
	 * 
	 * @param key
	 *            The meta error message key.
	 * @param variables
	 *            The message format arguments.
	 * @return The meta error message.
	 */
    private String message(String key, Object...variables) {
        return new Message("com.goodworkalan.verbiage.Message", "missing", key, position(new HashMap<Object, Object>(), variables)).toString();
    }
}
