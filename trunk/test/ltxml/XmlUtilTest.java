package ltxml;

import com.example.schemas.lt_xml.ATypeWithComplexField;
import com.example.schemas.lt_xml.ATypeWithEnumField;
import com.example.schemas.lt_xml.AbstractBaseType;
import com.example.schemas.lt_xml.AllDataTypes;
import com.example.schemas.lt_xml.AllDataTypesList;
import com.example.schemas.lt_xml.AnEnumType;
import com.example.schemas.lt_xml.CapitalizationTest;
import com.example.schemas.lt_xml.DerivedType;
import com.example.schemas.lt_xml.ListItems;
import com.example.schemas.lt_xml.PairOfStrings;
import com.example.schemas.lt_xml.SimpleType;
import com.example.schemas.lt_xml.SubType;
import org.junit.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class XmlUtilTest {

    @Test
    public void testNsToPackage () throws Exception {
        assertEquals("com.example.schemas.lt_xml", XmlUtil.nsToPackage("http://schemas.example.com/lt-xml"));
        //http://www.gamingstandards.com/s2s/schemas/v1.2.6 -- > com.gamingstandards.s2s.schemas.v1_2
        assertEquals("com.example.a.b.v1_2_3", XmlUtil.nsToPackage("http://www.example.com/a/b/v1.2.3"));
    }

    @Test
    public void testWrite () throws Exception {
        SimpleType obj = new SimpleType();
        obj.setANumber(12345);
        obj.setAString(null);
        obj.setThingamajiggy("thingy");
        obj.setTimestamp(54321);

        String xml = XmlUtil.marshall(obj, true);
        System.out.printf("XML: %s\n", xml);
        SimpleType obj2 = (SimpleType) XmlUtil.unmarshall(xml, true);
    }

    @Test
    public void testBigDecimal() throws Exception {
        AllDataTypes obj = createPrototype();

        // BigDecimal
        BigDecimal[] values = {null, new BigDecimal("0"), new BigDecimal("-10000000.00001"), new BigDecimal("0.0100")};
        for (BigDecimal value : values) {
            obj.setObjectBigDecimal(value);
            verify(obj);
        }
    }

    @Test
    public void testBoolean() throws Exception {
        AllDataTypes obj = createPrototype();

        // BigDecimal
        Boolean[] values = {null, Boolean.FALSE, Boolean.TRUE};
        for (Boolean value : values) {
            obj.setObjectBoolean(value);
            verify(obj);
        }
    }

    @Test
    public void testDate() throws Exception {
        AllDataTypes obj = createPrototype();

        // BigDecimal
        XMLGregorianCalendar[] values = {null, DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar())};
        for (XMLGregorianCalendar value : values) {
            obj.setObjectDate(value);
            verify(obj);
        }
    }

    @Test
    public void testInteger() throws Exception {
        AllDataTypes obj = createPrototype();

        // BigDecimal
        Integer[] values = {null, Integer.MIN_VALUE, Integer.MAX_VALUE};
        for (Integer value : values) {
            obj.setObjectInteger(value);
            verify(obj);
        }
    }

    @Test
    public void testLong() throws Exception {
        AllDataTypes obj = createPrototype();

        // BigDecimal
        Long[] values = {null, Long.MIN_VALUE, Long.MAX_VALUE};
        for (Long value : values) {
            obj.setObjectLong(value);
            verify(obj);
        }
    }

    @Test
    public void testString() throws Exception {
        AllDataTypes obj = createPrototype();

        // BigDecimal
        String[] values = {null, "", " ", "<try><some>embedded content</some></try>", "How\nabout\nsome\nnewlines.\n\n"};
        for (String value : values) {
            obj.setObjectString(value);
            verify(obj);
        }
    }

    @Test
    public void testint() throws Exception {
        AllDataTypes obj = createPrototype();

        // BigDecimal
        int[] values = {0, Integer.MIN_VALUE, Integer.MAX_VALUE};
        for (int value : values) {
            obj.setPrimitiveInt(value);
            verify(obj);
        }
    }

    @Test
    public void testlong() throws Exception {
        AllDataTypes obj = createPrototype();

        // BigDecimal
        long[] values = {0, Long.MIN_VALUE, Long.MAX_VALUE};
        for (long value : values) {
            obj.setPrimitiveLong(value);
            verify(obj);
        }
    }

    @Test
    public void testboolean() throws Exception {
        AllDataTypes obj = createPrototype();

        // BigDecimal
        boolean[] values = {true, false};
        for (boolean value : values) {
            obj.setPrimitiveBoolean(value);
            verify(obj);
        }
    }

    @Test
    public void testEnum() throws Exception {
        ATypeWithEnumField obj = new ATypeWithEnumField();

        AnEnumType[] values = {null, AnEnumType.CHOICE_A, AnEnumType.CHOICE_C, AnEnumType.CHOICE_D};
        for (AnEnumType value : values) {
            obj.setStatus(value);
            ATypeWithEnumField obj2 = (ATypeWithEnumField) remarshall(obj);
            assertEquals(obj.getStatus(), obj2.getStatus());
        }
    }

    @Test
    public void testComplexField() throws Exception {
        ATypeWithComplexField obj = new ATypeWithComplexField();

        SubType type = new SubType();
        type.setSubField("abc");

        SubType[] values = {null, type};
        for (SubType value : values) {
            obj.setTypeField(value);
            ATypeWithComplexField obj2 = (ATypeWithComplexField) remarshall(obj);

            SubType field = obj.getTypeField();
            SubType field2 = obj2.getTypeField();

            if (field == null) {
                assertNull(field2);
            } else {
                assertEquals (field.getSubField(), field2.getSubField());
            }
        }
    }

    @Test
    public void testStringList() throws Exception {
        AllDataTypesList obj = new AllDataTypesList();
        lazyVerify(obj);

        obj.getObjectString().add("one");
        lazyVerify(obj);

        obj.getObjectString().add("two");
        lazyVerify(obj);
    }

    @Test
    public void testDecimalList() throws Exception {
        AllDataTypesList obj = new AllDataTypesList();
        lazyVerify(obj);

        obj.getObjectBigDecimal().add(new BigDecimal("123.45"));
        lazyVerify(obj);

        obj.getObjectBigDecimal().add(new BigDecimal("1.23"));
        lazyVerify(obj);
    }

    @Test
    public void testBooleanList() throws Exception {
        AllDataTypesList obj = new AllDataTypesList();
        lazyVerify(obj);

        obj.getObjectBoolean().add(Boolean.FALSE);
        lazyVerify(obj);

        obj.getObjectBoolean().add(Boolean.TRUE);
        lazyVerify(obj);
    }

    @Test
    public void testDateList() throws Exception {
        AllDataTypesList obj = new AllDataTypesList();
        lazyVerify(obj);

        obj.getObjectDate().add(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
        lazyVerify(obj);

        obj.getObjectDate().add(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
        lazyVerify(obj);
    }

    @Test
    public void testIntegerList() throws Exception {
        AllDataTypesList obj = new AllDataTypesList();
        lazyVerify(obj);

        obj.getObjectInteger().add(1);
        lazyVerify(obj);

        obj.getObjectInteger().add(2);
        lazyVerify(obj);
    }

    @Test
    public void testLongList() throws Exception {
        AllDataTypesList obj = new AllDataTypesList();
        lazyVerify(obj);

        obj.getObjectLong().add(1l);
        lazyVerify(obj);

        obj.getObjectLong().add(1l);
        lazyVerify(obj);
    }

    @Test
    public void testComplexList() throws Exception {
        AllDataTypesList obj = new AllDataTypesList();
        obj.setThingamajiggy("wahooooooo!");

        AbstractBaseType e = new AbstractBaseType();
        e.setTimestamp(42424242);
        e.setThingamajiggy("one and a two");
        PairOfStrings p = new PairOfStrings();
        p.setS1("i am s1");
        p.setS2("i am s2");
        obj.getObjectComplexType().add(p);

        p = new PairOfStrings();
        p.setS1("i am s1 #2");
        p.setS2("i am s2 #2");
        obj.getObjectComplexType().add(p);

        lazyVerify(obj);
    }

    @Test
    public void testDerived () throws Exception {
        DerivedType obj = new DerivedType();
        obj.setStatus(AnEnumType.CHOICE_B);
        obj.setDescription("Hi there you smooth frood.");
        lazyVerify(obj);
    }

    @Test
    public void testCapitalization() throws Exception {
        CapitalizationTest obj = new CapitalizationTest();
        obj.setRUN2ANumber("1234");
        obj.setSTUDLYCaps("foo");
        obj.setTLAFooSQLStumpy("asdfasdf");
        obj.setLowercase("test this lowercase stuff");
        lazyVerify(obj);
    }

    @Test
    public void testListAgain () throws Exception {
        ListItems obj = new ListItems();
        ListItems.StringAlias item = new ListItems.StringAlias();
        item.setField1(12345l);
        item.setStringAlias("12345");
        obj.getStringAlias().add(item);
        lazyVerify (obj);
    }
    //--------------------------------------------------------------------------------------------------------- Internal

    private void lazyVerify(Object obj) throws Exception {
        String xml = XmlUtil.marshall(obj, true);
        System.out.printf("XML: %s\n", xml);
        Object obj2 = XmlUtil.unmarshall(xml, true);
        String xml2 = XmlUtil.marshall(obj2, true);
        System.out.printf("XML2: %s\n", xml2);
        assertEquals(xml, xml2);
    }

    private AllDataTypes createPrototype() throws DatatypeConfigurationException {
        AllDataTypes obj = new AllDataTypes();
        obj.setObjectBigDecimal(new BigDecimal("123.45"));
        obj.setObjectBoolean(Boolean.TRUE);
        obj.setObjectDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
        obj.setObjectInteger(1);
        obj.setObjectLong(1l);
        obj.setObjectString("Hello there.");
        obj.setPrimitiveBoolean(false);
        obj.setPrimitiveInt(1);
        obj.setPrimitiveLong(1l);
        return obj;
    }

    private void verify (AllDataTypes obj1) throws Exception {
        AllDataTypes obj2 = (AllDataTypes) remarshall(obj1);

        assertEquals(obj1.getObjectBigDecimal(), obj2.getObjectBigDecimal());
        assertEquals(obj1.getObjectDate(), obj2.getObjectDate());
        assertEquals(obj1.getObjectInteger(), obj2.getObjectInteger());
        assertEquals(obj1.getObjectLong(), obj2.getObjectLong());
        assertEquals(obj1.getObjectString(), obj2.getObjectString());

        assertEquals(obj1.getPrimitiveInt(), obj2.getPrimitiveInt());
        assertEquals(obj1.getPrimitiveLong(), obj2.getPrimitiveLong());
    }

    private Object remarshall(Object obj1) throws Exception {
        String xml = XmlUtil.marshall(obj1, true);
        System.out.printf("XML: %s\n", xml);
        return XmlUtil.unmarshall(xml, true);
    }
}
