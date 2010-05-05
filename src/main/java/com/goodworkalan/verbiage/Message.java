package com.goodworkalan.verbiage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentMap;

public class Message {
    /**
     * Cache of resource bundles. Cached to keep from rereading from file.
     * Bundle is passed in so that it can be collected if a class library is
     * reloaded.
     */
  private final ConcurrentMap<String, ResourceBundle> bundles;
    
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
    private final Object variables;

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
     * @param bundles
     *            The message bundle cache.
     * @param context
     *            The message bundle context.
     * @param bundleName
     *            The message bundle file name.
     * @param messageKey
     *            The message key.
     * @param variables
     *            The map of variables.
     */
    public Message(ConcurrentMap<String, ResourceBundle> bundles, String context, String bundleName, String messageKey, Object variables) {
        this.bundles = bundles;
        this.context = context;
        this.bundleName = bundleName;
        this.variables = variables;
        this.messageKey = messageKey;
    }

    /**
     * Add the given positioned parameters to the given argument map.
     * 
     * @param variables
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
    public Object getVariables() {
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
                if (!Indexes.isInteger(name)) {
                    if (!Indexes.checkJavaIdentifier(name)) {
                        throw new IllegalArgumentException();
                    }
                    throw new NoSuchElementException();
                }
                int index = Integer.parseInt(name, 10);
                List<?> list = (List<?>) current;
                if (index >= list.size()) {
                    throw new NoSuchElementException();
                }
                current = list.get(index);
            } else {
                throw new NoSuchElementException();
            }
        }
        return current;
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
     * Generate the formatted message.
     */
    public String toString() {
        int dot = context.lastIndexOf('.');
        String bundlePath = "com.goodworkalan.verbiage.package";
        String key = messageKey;
        if (dot != -1) {
            String packageName = context.substring(0, dot);
            bundlePath = packageName + "." + bundleName;
        } else {
            return message("defaultPackage", context, key);
        }
        ResourceBundle bundle = bundles.get(bundlePath);
        if (bundle == null) {
            try {
                bundle = ResourceBundle.getBundle(bundlePath);
            } catch (MissingResourceException e) {
                return message("missingBundle", bundlePath, key);
            }
            bundles.put(bundlePath, bundle);
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
                Map<?, ?> map = (Map<?, ?>) variables;
                for (int i = 1; map.containsKey("$" + i); i++) {
                    positioned.add(map.get("$" + i));
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
                    argument = getValue(name);
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
            return message("missingArgument", e.getMessage(), key, bundlePath);
        }
    }
    
    private String message(String key, Object...variables) {
        return new Message(bundles, "com.goodworkalan.verbiage.Message", "missing", key, position(new HashMap<Object, Object>(), variables)).toString();
    }
}
