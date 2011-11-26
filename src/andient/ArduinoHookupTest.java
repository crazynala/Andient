package andient;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;

/**
 * User: dan
 * Date: 11/26/11
 */
public class ArduinoHookupTest extends TestCase {
    @Test
    public void testHasCRNL() throws Exception {
        ArduinoHookup ah = new ArduinoHookup();
        byte[] arr = {0x31, 0x33, 0xd, 0xa};
        assertTrue(ah.hasCRNL(arr));
        byte[] arr2 = {0x31, 0x33, 0xd};
        assertFalse(ah.hasCRNL(arr2));
        byte[] arr3 = {0x31, 0x33, 0xa};
        assertFalse(ah.hasCRNL(arr3));
    }

    @Test
    public void testIndexOfCR() throws Exception {
        ArduinoHookup ah = new ArduinoHookup();
        byte[] arr = {0x31, 0x33, 0xd, 0xa};
        assertEquals(2, ah.indexOfCR(arr));
        byte[] arr2 = {0x31, 0x33, 0xd};
        assertEquals(2, ah.indexOfCR(arr2));
        byte[] arr3 = {0x31, 0x33, 0xa};
        assertEquals(-1, ah.indexOfCR(arr3));
    }

    @Test
    public void testReadTrimmedFirstLine() throws Exception {
        ArduinoHookup ah = new ArduinoHookup();
        byte[] arr = {0x31, 0x33, 0xd, 0xa};
        assertEquals("13", ah.readTrimmedFirstLine(arr));
        byte[] arr2 = {0x31, 0x33, 0xd};
        assertNull(ah.readTrimmedFirstLine(arr2));
        byte[] arr3 = {0xd, 0xa};
        String x = ah.readTrimmedFirstLine(arr3);
        assertEquals("", ah.readTrimmedFirstLine(arr3));

    }

    @Test
    public void testRemoveFirstLine() throws Exception {
        ArduinoHookup ah = new ArduinoHookup();
        byte[] arr = {0x31, 0x33, 0xd, 0xa};
        assertEquals(0, ah.removeFirstLine(arr).length);
        byte[] arr2 = {0x31, 0x33, 0xd, 0xa, 0x31, 0x33, 0xd, 0xa};
        assertTrue(Arrays.equals(arr, ah.removeFirstLine(arr2)));
    }

}
