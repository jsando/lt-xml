package ltxml;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class SimpleXmlGregorianCalendar extends XMLGregorianCalendar {

    private String xmlString;

    public SimpleXmlGregorianCalendar(String xmlString) {
        this.xmlString = xmlString;
    }

    private void usop() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        usop();
    }

    @Override
    public void reset() {
        usop();
    }

    @Override
    public void setYear(BigInteger year) {
        usop();
    }

    @Override
    public void setYear(int year) {
        usop();
    }

    @Override
    public void setMonth(int month) {
        usop();
    }

    @Override
    public void setDay(int day) {
        usop();
    }

    @Override
    public void setTimezone(int offset) {
        usop();
    }

    @Override
    public void setHour(int hour) {
        usop();
    }

    @Override
    public void setMinute(int minute) {
        usop();
    }

    @Override
    public void setSecond(int second) {
        usop();
    }

    @Override
    public void setMillisecond(int millisecond) {
        usop();
    }

    @Override
    public void setFractionalSecond(BigDecimal fractional) {
        usop();
    }

    @Override
    public BigInteger getEon() {
        usop();
        return null;
    }

    @Override
    public int getYear() {
        usop();
        return 0;
    }

    @Override
    public BigInteger getEonAndYear() {
        usop();
        return null;
    }

    @Override
    public int getMonth() {
        usop();
        return 0;
    }

    @Override
    public int getDay() {
        usop();
        return 0;  
    }

    @Override
    public int getTimezone() {
        usop();
        return 0;  
    }

    @Override
    public int getHour() {
        usop();
        return 0;
    }

    @Override
    public int getMinute() {
        usop();
        return 0;
    }

    @Override
    public int getSecond() {
        usop();
        return 0;
    }

    @Override
    public BigDecimal getFractionalSecond() {
        usop();
        return null;
    }

    @Override
    public int compare(XMLGregorianCalendar xmlGregorianCalendar) {
        usop();
        return 0;
    }

    @Override
    public XMLGregorianCalendar normalize() {
        usop();
        return null;
    }

    @Override
    public String toXMLFormat() {
        return xmlString;
    }

    @Override
    public QName getXMLSchemaType() {
        usop();
        return null;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void add(Duration duration) {
        usop();
    }

    @Override
    public GregorianCalendar toGregorianCalendar() {
        usop();
        return null;
    }

    @Override
    public GregorianCalendar toGregorianCalendar(TimeZone timezone, Locale aLocale, XMLGregorianCalendar defaults) {
        usop();
        return null;
    }

    @Override
    public TimeZone getTimeZone(int defaultZoneoffset) {
        usop();
        return null;
    }

    @Override
    public Object clone() {
        usop();
        return null;
    }
}
