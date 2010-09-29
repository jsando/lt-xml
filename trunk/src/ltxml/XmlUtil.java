package ltxml;

import org.xmlpull.mxp1.MXParser;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.beans.Introspector;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/*
 * Binding XML to Java
 *
 * Primitives:
 *  int
 *  long
 *  boolean
 *
 * Object:
 *  Integer
 *  Long
 *  Boolean
 *  String
 *  BigDecimal
 *  XMLGregorianCalendar
 *  Complex Type
 *  enum
 *  List<any object type>
 */
public class XmlUtil {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private boolean soap;

    public XmlUtil(boolean soap) {
        this.soap = soap;
    }

    public Object parse(InputStream istream, String encoding) throws Exception {
        MXParser parser = new MXParser();
        parser.setFeature(MXParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.setInput(istream, encoding);
        return parse2(parser);
    }

    public Object parse(Reader reader) throws Exception {
        MXParser parser = new MXParser();
        parser.setFeature(MXParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.setInput(reader);
        return parse2(parser);
    }

    private Object parse2(MXParser parser) throws Exception {

        // Skip over soap envelope, to first child of Body tag.
        if (soap) {
            while (true) {
                if (parser.next() == MXParser.START_TAG) {
                    if (parser.getName().equals("Body")) {
                        break;
                    }
                }
            }
        }

        while (parser.next() != MXParser.START_TAG) {
            // just advance.
        }
        String root = parser.getName();
        String ns = parser.getNamespace();
        String packageName = nsToPackage(ns);
        Class rootClass = Class.forName(packageName + "." + root);
        Object bean = rootClass.newInstance();
        bind(parser, parser.getName(), parser.getNamespace(), bean, true);
        return bean;
    }

    /*
     * http://a.b.c/d/e --> c.b.a.d.e
     */
    public static String nsToPackage(String ns) throws URISyntaxException, IOException {
        String pn = null;
        URI uri = new URI(ns);
        if (uri.getScheme().equals("http")) {
            String host = uri.getHost();
            String path = uri.getPath();

            String[] parts = host.split("\\.");

            StringBuilder b = new StringBuilder(ns.length());
            for (int i = parts.length - 1; i >= 0; i--) {
                if ("www".equals(parts[i])) {
                    continue;
                }
                if (b.length() > 0)
                    b.append('.');
                b.append(parts[i]);
            }
            if (path != null && path.length() > 0) {
                path = path.replace('.', '_');
                path = path.replace('/', '.');
                b.append(path);
            }
            pn = b.toString();
            pn = pn.replace('-', '_');
        }
        if (pn == null)
            throw new IOException("Unknown package (input namespace: '" + ns + "').");
        return pn;
    }

    @SuppressWarnings({"unchecked"})
    public void bind(MXParser parser, String tagName, String ns, Object bean, boolean advance) throws Exception {

        //System.out.printf("bind (tag %s, bean %s)\n", tagName, bean.getClass().getSimpleName());

        boolean ended = false;
        int type;
        while (!ended) {

            if (advance) {
                type = parser.next();
                if (type == MXParser.END_DOCUMENT)
                    break;
            } else {
                type = parser.getEventType();
            }
            advance = true;

            switch (type) {
                case MXParser.START_TAG:
                    String name = parser.getName();
                    String nameNs = parser.getNamespace();
                    String value = null;
                    String xsiType = parser.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");
                    boolean empty = parser.isEmptyElementTag();

                    while ((type = parser.next()) != MXParser.END_TAG) {
                        if (type == MXParser.START_TAG) {
                            break;
                        } else if (type == MXParser.TEXT) {
                            value = parser.getText();
                        }
                    }

                    if (type == MXParser.END_TAG && name.equals(tagName)) {
                        ended = true;
                    } else if (type == MXParser.END_TAG && name.equals(parser.getName()) && value == null) {
                        empty = true;
                    }

                    String property = decapitalize(name);
                    Field field = resolveField(bean, property);
                    if (field == null)
                        continue;
                    
                    field.setAccessible(true);
                    Class fieldType = field.getType();

                    if (xsiType != null) {
                        fieldType = Class.forName(bean.getClass().getPackage().getName() + "." + xsiType);
                    }

                    if (fieldType.equals(List.class)) {
                        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                        fieldType = (Class) genericType.getActualTypeArguments()[0];
                    }

                    Object rval = null;

                    if (fieldType.equals(Integer.TYPE)) {
                        if (value != null)
                            rval = Integer.parseInt(value);
                    } else if (fieldType.equals(Integer.class)) {
                        if (value != null)
                            rval = new Integer(value);
                    } else if (fieldType.equals(Long.TYPE)) {
                        if (value != null)
                            rval = Long.parseLong(value);
                    } else if (fieldType.equals(Long.class)) {
                        if (value != null)
                            rval = new Long(value);
                    } else if (fieldType.equals(String.class)) {
                        if (value != null)
                            rval = value;
                        else
                            rval = "";
                    } else if (fieldType.equals(BigDecimal.class)) {
                        if (value != null)
                            rval = new BigDecimal(value);
                    } else if (fieldType.equals(Boolean.class) || fieldType.equals(Boolean.TYPE)) {
                        if (value != null)
                            rval = Boolean.valueOf(value);
                    } else if (fieldType.equals(XMLGregorianCalendar.class)) {
                        if (value != null)
                            rval = DatatypeFactory.newInstance().newXMLGregorianCalendar(value);
                    } else if (fieldType.isEnum()) {
                        if (value != null) {
                            Method fromValueString = fieldType.getDeclaredMethod("fromValue", String.class);
                            rval = fromValueString.invoke(null, value);
                        }
                    } else if (!empty) {
                        rval = fieldType.newInstance();
                        bind(parser, name, nameNs, rval, false);
                    }

                    if (rval != null) {
                        if (field.getType().equals(List.class)) {
                            List list = (List) field.get(bean);
                            if (list == null) {
                                list = new ArrayList();
                                field.set(bean, list);
                            }
                            list.add(rval);
//                            System.out.printf("Added item to list %s in bean %s\n", tagName, bean.getClass().getSimpleName());
                        } else {
                            field.set(bean, rval);
                        }
                    }
                    break;


                case MXParser.END_TAG:
                    if (parser.getNamespace().equals(ns) && parser.getName().equals(tagName)) {
                        return;
                    }
                    break;
            }
        }
    }

    public static String decapitalize(final String name) {
        final StringBuilder b = new StringBuilder(name);
        final int length = name.length();
        b.setCharAt(0, Character.toLowerCase(b.charAt(0)));
        for (int i = 1; i < length; i++) {
            // If there's another character after this one that is NOT uppercase, then we are done.
            if (i < length - 1 && Character.isLetter(b.charAt(i + 1)) && !Character.isUpperCase(b.charAt(i + 1))) {
                break;
            }
            char ch = b.charAt(i);
            if (!Character.isLetter(ch)) {
                break;
            }
            b.setCharAt(i, Character.toLowerCase(ch));
        }
        return b.toString();
    }

    private Field resolveField(Object bean, String property) throws NoSuchFieldException {
        Field field = null;
        Class claz = bean.getClass();
        do {
            try {
                field = claz.getDeclaredField(property);
            } catch (NoSuchFieldException e) {
                claz = claz.getSuperclass();
            }
        } while (field == null && !claz.equals(Object.class));
        if (field == null) {
//            System.err.printf("WARNING: Field not found for XML tag (%s.%s)\n", bean.getClass().getSimpleName(), property);
//            throw new NoSuchFieldException(bean.getClass().getName() + "." + property);
        }
        return field;
    }

    private void writeElements(Object obj, Class claz, XmlWriter xml) throws Exception {
        if (!claz.getSuperclass().equals(Object.class)) {
            writeElements(obj, claz.getSuperclass(), xml);
        }
        for (Field field : claz.getDeclaredFields()) {
            field.setAccessible(true);
            String tagName = null;
            XmlElement element = field.getAnnotation(XmlElement.class);
            if (element != null) {
                tagName = element.name();
                if ("##default".equals(tagName))
                    tagName = null;
            }

            if (tagName == null) {
                tagName = field.getName();
//                tagName = Character.toUpperCase(tagName.charAt(0)) + tagName.substring(1);
            }

            if (field.getType().equals(List.class)) {
                List list = (List) field.get(obj);
                if (list != null) {
                    for (Object o : list) {
//                        System.out.printf("----------> Serializing list item type %s\n", o.getClass().getName());
                        writeSingleValue(xml, tagName, o.getClass(), o);
                    }
                }
            } else {
                writeSingleValue(xml, tagName, field.getType(), field.get(obj));
            }
        }
    }

    private void writeSingleValue(XmlWriter xml, String tagName, Class type, Object value) throws Exception {
        if (value == null) {
//            System.out.printf(">>>>> Ignoring null value for %s.\n", tagName);
            return;
        }

        //System.out.printf("Writing %s = '%s'\n", tagName, String.valueOf(value));
        xml.startElement(tagName);
        Object xmlValue = null;

        if (type.equals(String.class)) {
            xmlValue = value;
        } else if (type.equals(Long.class)) {
            xmlValue = value;
        } else if (type.equals(Long.TYPE)) {
            xmlValue = value;
        } else if (type.equals(Integer.class)) {
            xmlValue = value;
        } else if (type.equals(Integer.TYPE)) {
            xmlValue = value;
        } else if (type.equals(BigDecimal.class)) {
            xmlValue = value;
        } else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
            xmlValue = value;
        } else if (XMLGregorianCalendar.class.isAssignableFrom(type)) {
            xmlValue = value;
        } else if (type.isEnum()) {
            if (value != null) {
                Method valueMethod = type.getDeclaredMethod("value", new Class[]{});
                xmlValue = valueMethod.invoke(value);
            }
        } else {
            if (value != null)
                writeObject(value, value.getClass(), null, xml);
        }

        if (xmlValue != null)
            xml.addText(String.valueOf(xmlValue));

        xml.endElement(tagName);
    }

    private void writeObject(Object obj, Class claz, String tagName, XmlWriter xml) throws Exception {
        // TODO: only generate once per pass
        if (tagName != null) {
            xml.startElement(tagName);
            XmlSchema annotation = claz.getPackage().getAnnotation(XmlSchema.class);
            xml.addAttribute("xmlns", annotation.namespace());
        }

        // If supertype is not Object, and tag name doesn't match simple class name, it may be polymorphic.
        // TODO: If this is unrealiable, use the @XmlSeeAlso annotation on the declared type
        if (!claz.getSuperclass().equals(Object.class) && !xml.getElement().equals(claz.getSimpleName())) {
            xml.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            xml.addAttribute("xsi:type", claz.getSimpleName());
        }
//        if (!xml.getElement().equals(claz.getSimpleName())) {
//            xml.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
//            xml.addAttribute("xsi:type", claz.getSimpleName());
//        }

        writeElements(obj, claz, xml);
        if (tagName != null)
            xml.endElement(tagName);
    }

    public String marshall(Object obj) throws Exception {
        StringWriter sw = new StringWriter();
        XmlWriter xml = new XmlWriter(sw);
        xml.addXmlVersion("1.0", null);

        if(soap) {
            xml.startElement("SOAP-ENV:Envelope");
            xml.addAttribute("xmlns:SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/");
            xml.startElement("SOAP-ENV:Header");
            xml.endElement("SOAP-ENV:Header");
            xml.startElement("SOAP-ENV:Body");
        }

        writeObject(obj, obj.getClass(), obj.getClass().getSimpleName(), xml);

        if (soap) {
            xml.endElement("SOAP-ENV:Body");
            xml.endElement("SOAP-ENV:Envelope");
        }
        String s = sw.toString();
//        System.out.printf("Marshalled: %s\n", s);
        return s;
    }

    public static Object unmarshall(String xml, boolean soap) throws Exception {
        XmlUtil xmlUtil = new XmlUtil(soap);
        return xmlUtil.parse(new StringReader(xml));
    }

    public static Object unmarshall(byte[] bytes, int offset, int length, boolean soap) throws Exception {
//        String s = new String(bytes, offset, length, UTF_8);
//        System.out.printf("Parsing XML: %s\n", s);
        return new XmlUtil(soap).parse(new InputStreamReader(new ByteArrayInputStream(bytes, offset, length), UTF_8));
    }

    public static String marshall(Object obj, boolean soap) throws Exception {
        XmlUtil util = new XmlUtil(soap);
        return util.marshall(obj);
    }

    public static void main(String[] args) throws Exception {
        XmlUtil xmlUtil = new XmlUtil(true);
        Object o = xmlUtil.parse(new FileReader(args[0]));

        OutputStreamWriter owriter = new OutputStreamWriter(System.out);
        XmlWriter xml = new XmlWriter(owriter);
        xml.setNewlines(true);
        xmlUtil.writeObject(o, o.getClass(), o.getClass().getSimpleName(), xml);
        owriter.flush();
    }
}
