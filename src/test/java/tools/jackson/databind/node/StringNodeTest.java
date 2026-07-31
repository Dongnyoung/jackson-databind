package tools.jackson.databind.node;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.exc.StreamConstraintsException;

import static org.junit.jupiter.api.Assertions.*;

public class StringNodeTest extends NodeTestBase
{
    @Test
    public void testBasics()
    {
        assertNull(StringNode.valueOf(null));
        StringNode empty = StringNode.valueOf("");
        assertStandardEquals(empty);
        assertSame(StringNode.EMPTY_STRING_NODE, empty);

        assertEquals(0, empty.size());
        assertTrue(empty.isEmpty());

        assertNodeNumbers(StringNode.valueOf("-3"), -3, -3.0);

        long value = 127353264013893L;
        StringNode n = StringNode.valueOf(String.valueOf(value));
        assertEquals(value, n.asLong());

        assertFalse(n.isNumber());
        assertFalse(n.canConvertToInt());
        assertFalse(n.canConvertToLong());
        assertFalse(n.canConvertToExactIntegral());

        // and then with non-numeric input
        n = StringNode.valueOf("foobar");
        assertNodeNumbersForNonNumeric(n);

        assertEquals("foobar", n.asString());
        assertEquals("", empty.asString());

        assertTrue(StringNode.valueOf("true").asBoolean(true));
        assertTrue(StringNode.valueOf("true").asBoolean(false));
        assertFalse(StringNode.valueOf("false").asBoolean(true));
        assertFalse(StringNode.valueOf("false").asBoolean(false));

        assertNonContainerStreamMethods(n);
    }

    @Test
    public void testEquals()
    {
        assertEquals(new StringNode("abc"), new StringNode("abc"));
        assertNotEquals(new StringNode("abc"), new StringNode("def"));
    }

    @Test
    public void testHashCode()
    {
        assertEquals("abc".hashCode(), new StringNode("abc").hashCode());
    }

    @Test
    public void testTooLongNumericStringCoercionFails()
    {
        StringNode n = StringNode.valueOf(_repeat('9',
                StreamReadConstraints.defaults().getMaxNumberLength() + 1));

        assertThrows(StreamConstraintsException.class, () -> n.asInt());
        assertThrows(StreamConstraintsException.class, () -> n.asLong());
        assertThrows(StreamConstraintsException.class, () -> n.asBigInteger());
        assertThrows(StreamConstraintsException.class, () -> n.asFloat());
        assertThrows(StreamConstraintsException.class, () -> n.asDouble());
        assertThrows(StreamConstraintsException.class, () -> n.asDecimal());
    }

    @Test
    public void testTooLongNonNumericStringCoercionDefaults()
    {
        StringNode n = StringNode.valueOf(_repeat('a',
                StreamReadConstraints.defaults().getMaxNumberLength() + 1));

        assertEquals(13, n.asInt(13));
        assertEquals(17L, n.asLong(17L));
        assertEquals(BigInteger.TEN, n.asBigInteger(BigInteger.TEN));
        assertEquals(0.25f, n.asFloat(0.25f));
        assertEquals(0.5d, n.asDouble(0.5d));
        assertEquals(BigDecimal.TEN, n.asDecimal(BigDecimal.TEN));
        assertFalse(n.asBigIntegerOpt().isPresent());
        assertFalse(n.asDecimalOpt().isPresent());
    }

    private static String _repeat(char ch, int len)
    {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; ++i) {
            sb.append(ch);
        }
        return sb.toString();
    }
}
