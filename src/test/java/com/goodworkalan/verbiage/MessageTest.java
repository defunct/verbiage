package com.goodworkalan.verbiage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.testng.annotations.Test;

import com.goodworkalan.verbiage.Message;

/**
 * Test for the CassandraException class.
 * 
 * @author Alan Gutierrez
 */
public class MessageTest {
    /** Create a message with the given variables. */
    private Message makeMessage(String key, Object variables) {
        return new Message(new ConcurrentHashMap<String, ResourceBundle>(), MessageTest.class.getCanonicalName(), "test_messages", key, variables);
    }
    
    private Message makePopulatedMessage(String key) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        HashMap<String, Object> subMap = new HashMap<String, Object>();
        map.put("a", "b");
        map.put("b", subMap);
        subMap.put("c", "d");
        subMap.put("e", Arrays.asList("a", "b", "c"));
        return makeMessage(key, map);
    }
    
    private Message makePopulatedMessage() {
        return makePopulatedMessage("key");
    }

    /**
     * Check the attribute accessors.
     */
    @Test
    public void accessors() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("a", "b");
        Message message = makeMessage("key", map);
        assertEquals(message.getBundleName(), "test_messages");
        assertEquals(message.getMessageKey(), "key");
        assertEquals(message.getContext(), "com.goodworkalan.verbiage.MessageTest");
        assertEquals(((Map<?, ?>) message.getVariables()).get("a"), "b");
    }
    
    /**
     * Check a single map dereference.
     */
    @Test
    public void mapGet() {
        assertEquals(makePopulatedMessage().get("a"), "b");
    }
   
    /**
     * Check getting a value from a nested map.
     */
    @Test
    public void nestedMapGet() {
        assertEquals(makePopulatedMessage().get("b.c"), "d");
    }
    
    /**
     * Check getting a list value.
     */
    @Test
    public void listGet() {
        assertEquals(makePopulatedMessage().get("b.e.1"), "b");
    }
    
    /**
     * Check referencing a null hash element.
     */
    @Test
    public void noSuchKey() {
        assertNull(makePopulatedMessage().get("z"));
    }
    
    /**
     * Check a missing hash element.
     */
    @Test
    public void noSuchHashElement() {
        assertNull(makePopulatedMessage().get("b.c.d"));
    }
    
    /**
     * Check an out of range array element.
     */
    @Test
    public void noSuchListElement() {
        assertNull(makePopulatedMessage().get("b.e.8"));
        assertNull(makePopulatedMessage().get("b.e.3"));
    }

    /**
     * Check referencing an array with a valid Java identifier.
     */
    @Test
    public void noSuchStringElementInList() {
        assertNull(makePopulatedMessage().get("b.e.f"));
    }
    
    /** Illegal hash path. */
    @Test(expectedExceptions=IllegalArgumentException.class) 
    public void illegalHashPath() {
        makePopulatedMessage().get("b.!");        
    }
    
    /** Illegal list path. */
    @Test(expectedExceptions=IllegalArgumentException.class) 
    public void illegalListPath() {
        makePopulatedMessage().get("b.e.!");        
    }
    
    /**
     * Test a simple message.
     */
    @Test
    public void toStringNoParameters() {
        assertEquals(makePopulatedMessage("none").toString(), "Hello.");
    }
    
    /**
     * Test a parameterized message.
     */
    @Test
    public void toStringOneParameter() {
        assertEquals(makePopulatedMessage("one").toString(), "Hello, d.");
    }
    
    
    /**
     * Test a two parameter message.
     */
    @Test
    public void toStringTwoParameters() {
        assertEquals(makePopulatedMessage("two").toString(), "Hello, d, b.");
    }
    
    /**
     * Test the bundle cache.
     */
    @Test
    public void bundleCache() {
        Message message = makePopulatedMessage("none");
        assertEquals(message.toString(), "Hello.");
        assertEquals(message.toString(), "Hello.");
    }
    
    /**
     * Test missing bundles.
     */
    @Test
    public void missingBundle() {
        Message message = new Message(new ConcurrentHashMap<String, ResourceBundle>(), "com.missing.missing.Missing", "test_messages", "key", new HashMap<String, String>());
        assertEquals(message.toString(), "Missing message bundle [com.missing.missing.test_messages]. Message key is [key]. (This is a meta error message.)");
    }
    
    /**
     * Test missing keys.
     */
    @Test
    public void missingKey() {
        assertEquals(makePopulatedMessage("missing").toString(), "The message key [missing] cannot be found in bundle [com.goodworkalan.verbiage.test_messages]. (This is a meta error message.)");
    }
    
    /**
     * Test empty messages.
     */
    @Test
    public void emptyMessage() {
        assertEquals(makePopulatedMessage("empty").toString(), "The message for message key [empty] in bundle [com.goodworkalan.verbiage.test_messages] is blank. (This is a meta error message.)");
    }
    
    /**
     * Test illegal format arguments.
     */
    @Test
    public void badArgument() {
        assertEquals(makePopulatedMessage("bad_argument").toString(), "Invalid format argument name [b.!] for message key [bad_argument] in bundle [com.goodworkalan.verbiage.test_messages]. (This is a meta error message.)");
    }
    
    /**
     * Test no such element.
     */
    @Test
    public void noSuchElement() {
        assertEquals(makePopulatedMessage("no_such_element").toString(), "Cannot find argument named [b.e.8] for message key [no_such_element] in bundle [com.goodworkalan.verbiage.test_messages]. (This is a meta error message.)");
    }
    
    /**
     * Test bad message formats.
     */
    @Test
    public void badFormat() {
        assertEquals(makePopulatedMessage("bad_format").toString(), "Cannot find argument named [Conversion = s, Flags = 0] for message key [bad_format] in bundle [com.goodworkalan.verbiage.test_messages]. (This is a meta error message.)");
    }
    
    /**
     * Positioned arguments.
     */
    @Test
    public void positionedArguments() {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("$1", "1");
        map.put("$2", "2");
        map.put("fred", "fred");
        Message message = makeMessage("positioned", map);
        assertEquals(message.toString(), "First: 1, Second: 2, Third: fred.");
    }
    
    /** Test class with no package. */
    @Test
    public void noPackage() {
        Message message = new Message(new ConcurrentHashMap<String, ResourceBundle>(), "DefaultPackaged", "test_messages", "a", new HashMap<Object, Object>());
        assertEquals(message.toString(), "Message bundle context [DefaultPackaged] resolves to the default package. Message key is [a]. (This is a meta error message.)");
    }
}
