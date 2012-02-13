package ltxml;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Stack;

/**
 * Append XML to a Writer one element or attribute at a time.
 */
public final class XmlWriter {

    // Whether to print a newline to start each tag
    private boolean _newlines;

    // True if started a tag but haven't printed the matching ">" yet.
    private boolean _inStartTag;

    // Stack of tags that have been opened but not yet closed.
    private Stack<String> _tags = new Stack<String> ();

    // Writer to write to.  User must flush the writer manually.
    private Writer _out;

    /**
     * Create a new XmlWriter to write to the given Writer.
     *
     * @param out
     */
    public XmlWriter(Writer out) {
        _out = out;
    }

    /**
     * Whether to print newlines before starting a new tag or not.
     */
    public boolean isNewlines () {
        return _newlines;
    }

    /**
     * Whether to print newlines before starting a new tag or not.
     */
    public XmlWriter setNewlines (boolean newlines) {
        this._newlines = newlines;
        return this;
    }

    public XmlWriter addXmlVersion () throws IOException {
        addXmlVersion ("1.0", null, null);
        return this;
    }

    public XmlWriter addXmlVersion (String version, String encoding) throws IOException {
        addXmlVersion (version, encoding, null);
        return this;
    }

    public XmlWriter addXmlVersion (String version, String encoding, String standalone) throws IOException {
        _out.write("<?xml version=\"");
        _out.write(version);
        if(encoding != null) {
            _out.write("\" encoding=\"");
            _out.write(encoding);
        }
        if(standalone != null) {
            _out.write("\" standalone=\"");
            _out.write(standalone);
        }
        _out.write("\" ?>");
        return this;
    }

    public XmlWriter addDocType (String name, String publicIdentifier, String systemIdentifier)
            throws IOException {
        _out.write ("\n<!DOCTYPE ");
        _out.write (name);

        if (publicIdentifier != null) {
            _out.write ("\n  PUBLIC \"");
            _out.write (publicIdentifier);
            _out.write ("\" ");
        }

        if (systemIdentifier != null) {
            _out.write ("\n  \"");
            _out.write (systemIdentifier);
            _out.write ("\" ");
        }

        _out.write (">\n");
        return this;
    }

    public XmlWriter startElement (String name) throws IOException {

        if (_inStartTag) {
            _out.write (">");

            if (_newlines)
                _out.write ("\n");
        }

        _out.write ("<");
        _out.write (name);
        _tags.push (name);
        _inStartTag = true;
        return this;
    }

    public XmlWriter addElement (String tag, String textValue) throws IOException {
        startElement (tag);
        addText (textValue);
        endElement (tag);
        return this;
    }

    public String getElement() {
        return _tags.peek();
    }

    public XmlWriter endElement (String name) throws IOException {
        if (_tags.empty ())
            throw new IllegalArgumentException ("No tag is currently open: " + name);
        String top = _tags.pop ();
        if (!top.equals (name))
            throw new IllegalArgumentException ("Tag '" + top + "' is open, cannot close '" + name + "'");

        if (_inStartTag)
            _out.write ("/>");
        else {
            _out.write ("</");
            _out.write (name);
            _out.write (">");
        }

        if (_newlines) {
            _out.write ("\n");
        }

        _inStartTag = false;
        return this;
    }

    public XmlWriter addAttribute (String name, String value) throws IOException {
        if (!_inStartTag)
            throw new IllegalArgumentException ("Not in open start tag.");

        _out.write (' ');
        _out.write (name);
        _out.write ("=\"");
        printEscaped (value);
        _out.write ('"');
        return this;
    }

    public XmlWriter addText (String text) throws IOException {
        if (_inStartTag) {
            _out.write ('>');
            _inStartTag = false;
        }
        printEscaped (text);
        return this;
    }

    public XmlWriter addRawText (String text) throws IOException {
        if (_inStartTag) {
            _out.write ('>');
            _inStartTag = false;
        }
        _out.write(text);
        return this;
    }

    /**
     * Escape embedded quotes, ampersands, and angle brackets.
     *
     * <p> Technically attribute values and character data have different escaping requirements,
     * according to the XML 1.0 specification.  But this should be safe for both.
     *
     * @param s
     *
     * @throws java.io.IOException
     */
    public XmlWriter printEscaped (String s) throws IOException {
        if (s != null) {
            for (int i = 0, size = s.length (); i < size; i++) {
                char ch = s.charAt (i);
                switch (ch) {
                    case '\'': _out.write("&#39;"); break;
                    case '"': _out.write("&#34;"); break;
                    case '&': _out.write ("&amp;"); break;
                    case '<': _out.write("&lt;"); break;
                    case '>': _out.write("&gt;"); break;
                    default: _out.write(ch); break;
                }
            }
        }
        return this;
    }


}