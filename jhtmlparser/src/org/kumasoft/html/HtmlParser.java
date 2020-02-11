/*
 * $Id: HtmlParser.java,v 1.1.1.1 2006/10/29 12:29:46 jjking Exp $
 *
 * Copyright 2004, Kumasoft
 */

package org.kumasoft.html;

import java.io.*;
import java.util.*;

/**
 * This parser parse the HTML and invoke appropriate method of the handler.
 * @version $Revision: 1.1.1.1 $
 */
public class HtmlParser implements CharCodes
{
    /**
     * The token type.
     */
    private static final int EOF = -1;
    private static final int NOT = 0;
    private static final int STAG = 1;
    private static final int ETAG = 2;
    private static final int CONTENT = 3;

    /**
     * The last char read.
     */
    private int c;
	
	/**
	 * Work buffer size.
	 */
	private int bufferSize = 32000;

    /**
     * Parses HTML document which comes from the stream and invokes the handler's method.
     * The character encoding to read the document is set 
     * @param in the input stream to read a document.
     * @param handler the HTML handler.
     * @exception IOException thrown when I/O error occures.
     */
    public void parse(InputStream in, HtmlHandler handler)
	throws IOException
    {
	parse(in, handler, null);
    }

    /**
     * Parses HTML document which comes from the stream and invokes the handler's method.
     * The character encoding is changed when the parser find new one at meta tag in the document.
     * @param in the input stream to read a document.
     * @param handler the HTML handler.
     * @param encoding the character encoding of the document.
     * @exception IOException thrown when I/O error occures.
     */
    public void parse(InputStream in, HtmlHandler handler, String encoding)
	throws IOException
    {
	BufferedInputStream inBuf = new BufferedInputStream(in);
        inBuf.mark(bufferSize);  // TODO: buffer size is too large.

        // detect page encoding
	EncodeDetector detector = new EncodeDetector(encoding);
	try {
	    detector.startDocument();
	    Reader readIn = encoding != null ? new InputStreamReader(inBuf, encoding) : new InputStreamReader(inBuf);
	    _parse(readIn, detector);
	}
	catch(HtmlException ex) {}
        detector.endDocument();

        // parse
        handler.startDocument();
        Reader readIn = detector.encoding != null
            ? new InputStreamReader(inBuf, detector.encoding)
            : new InputStreamReader(inBuf);
        inBuf.reset();
        _parse(readIn, handler);
	handler.endDocument();
    }


    private void _parse(Reader in, HtmlHandler handler)
	throws IOException
    {
	StringBuffer tokenBuf = new StringBuffer();
	c = -1;

	// init local variables
	Stack stack = new Stack();

	// move pointer to first tag.
	int tt = nextToken(tokenBuf, in);
	//if (tt != STAG) return;

	while(true) {
	    tt = nextToken(tokenBuf, in);
	    if (tt == STAG) {
		Tag tag = createTag(tokenBuf);
		if (tag != null) {
		    stack.push(tag);
		    handler.startTag(tag);
		}
	    }
	    else if (tt == ETAG) {
		// get the depth where the specified tag exists.
		int popNum = stack.search(createTag(tokenBuf));

		// pop until correspond tag is found.
		for (int i = 0; i < popNum; i ++) {
		    Tag tag = (Tag)stack.pop();
		    handler.endTag(tag);
		}
		if (stack.empty()) { break; }
	    }
	    else if (tt == CONTENT) {
		handler.content(tokenBuf.toString());
		//handler.content(decodeEscape(tokenBuf.toString()));
	    }
	    else if (tt == EOF) { break; }
	}
	return;
    }

    /**
     * Reads next token from the stream and put in token buffer.
     * @exception IOException thrown when I/O error occures.
     */
    private int nextToken(StringBuffer tokenBuf, Reader in) throws IOException
    {
	boolean tag = (c == '<');
	boolean etag = false;
	boolean first = true;
	boolean comment = false;
	tokenBuf.setLength(0);

	while((c = in.read()) >= 0) {
	    if (!comment) {
		if (c == '<' && !tag) {
		    return tokenBuf.length() > 0 ? CONTENT : NOT;
		}
		else if (c == '>' && tag) {
		    return etag ? ETAG : STAG;
		}
		else if (c == '/' && first) {
		    etag = true;
		}
		else {
		    tokenBuf.append((char)c);

		    // check in comment?
		    comment = (tokenBuf.length() == 3 && tokenBuf.charAt(0) == '!'
			       && tokenBuf.charAt(1) == '-' && tokenBuf.charAt(2) == '-');
		}
		first = false;
	    }
	    else {
		// in comment now
		tokenBuf.append((char)c);
		int l = tokenBuf.length();
		comment = tokenBuf.charAt(l - 3) != '-' || tokenBuf.charAt(l - 2) != '-' || tokenBuf.charAt(l - 1) != '>';
		if (comment) continue;

		// end of the comment
		tag = false;
		etag = false;
		first = true;
		tokenBuf.setLength(0);
	    }
	}
	return EOF;
    }

    /**
     * Creates a HTML tag from the string included in the start tag.
     * This also extract attributs from the string.
     * Returns null, string dosn't contains any tag information or unsupported tag.
     */
    private static Tag createTag(StringBuffer buf)
    {
	int p, p1;
	int l = buf.length();

	// get tag
	for (p = 0; p < l; p++) {
	    if (buf.charAt(p) > ' ') {
		p1 = p;
		for (; p < l; p++)
		    if (buf.charAt(p) <= ' ') { break; }
		String type = buf.substring(p1, p);
		Map attributes = extractAttributes(buf,p);
		return Tag.createTag(type, attributes);
	    }
	}
	return null;
    }

    private static final int ATTR_NAME = 0;
    private static final int ATTR_VALUE = 1;

    /**
     * Creates the list of attributes contained in a tag.
     * Returns null, string dosn't contains any attribute information.
     */
    private static Map extractAttributes(StringBuffer buf, int p)
    {
	Map attributes = new HashMap();
	int l = buf.length();
	int state = ATTR_NAME;
	int p0 = p;
	String name = null;
	boolean inValue = false;
	boolean esc = false;
	int endChar = 0;
	// get attribute
	while(p < l) {
	    char c = buf.charAt(p);
	    switch(state) {
	    case ATTR_NAME:
		if (c == '=' && p0 < p) {
		    state = ATTR_VALUE;
		    name = buf.substring(p0, p);
		    p0 = p+1;
		}
		else if (c == ' ') {
		    p0 = p+1;
		}
		break;

	    case ATTR_VALUE:
		if (!inValue && c != ' ') {
		    if (c == '"' || c == '\'') {
			endChar = c;
			p0 = p + 1;
		    }
		    else {
			endChar = ' ';
			p0 = p;
		    }
		    inValue = true;
		}
		else if (inValue) {
		    if (c == endChar && !esc) {
			state = ATTR_NAME;
			inValue = false;
			esc = false;
			String value = buf.substring(p0, p);
			attributes.put(name.toLowerCase(), value);
			p0 = p+1;
		    }
		    if (p == l-1 && p0 < p) {
			state = ATTR_NAME;
			inValue = false;
			esc = false;
			String value = buf.substring(p0, p + 1);
			attributes.put(name.toLowerCase(), value);
			p0 = p+1;
		    }
		    else {
			esc = (c == '\\' && endChar != ' ');
		    }
		}
		break;
	    }
	    p++;
	}
	return attributes.isEmpty() ? null : attributes;
    }


    /**
     * The HTML handler to detect the document encoding in a meta tag.
     */
    class EncodeDetector implements HtmlHandler
    {
	String encoding;

	public void startDocument() {}
	public void endDocument() {}
	public void content(String data) {}

	/**
	 * create new instance.
	 */
	public EncodeDetector(String encoding)
	{
	    this.encoding = encoding;
	}

	public void startTag(Tag tag) throws HtmlException
	{
            // Check if the meta tag contains a encoding information.
	    if (tag.isType("meta")) {
		if ("content-type".equalsIgnoreCase(tag.getAttribute("http-equiv"))) {

		    String newEncoding = getEncodingName(tag.getAttribute("content"));
		    if (newEncoding != null && !newEncoding.equalsIgnoreCase(encoding)) {
			encoding = newEncoding;
                        // force stop parsing
			throw new HtmlException();
		    }
		}
	    }

            // Check if there is no head tag
	    else if (tag.isType("body")) {
                // force stop parsing
                throw new HtmlException();
            }
	}

	public void endTag(Tag tag) throws HtmlException
        {
            // Check if the end of head tag.
	    if (tag.isType("head")) {
                // force stop parsing
                throw new HtmlException();
            }
        }

	/**
	 * Extracts character encoding declaration from the specified media type.
	 */
	private String getEncodingName(String mediaTypeStr)
	{
	    if (mediaTypeStr == null) return null;

	    mediaTypeStr = mediaTypeStr.toLowerCase();
	    int l = mediaTypeStr.length();
	    int p1 = mediaTypeStr.indexOf("charset");
	    if (p1 < 0) return null;

	    int p2 = mediaTypeStr.indexOf('=', p1);
	    if (p2 < 0 || p2 >= l - 1 ) return null;
	    p2 ++;

	    int p3 = mediaTypeStr.indexOf(';', p2);
	    return (p3 < 0 ? mediaTypeStr.substring(p2) : mediaTypeStr.substring(p2, p3)).trim();
	}
    }

    /*
    private String decodeEscape(String data)
    {
        int escapeStart = -1;
        StringBuffer dataBuf = new StringBuffer();
        for (int i = 0; i < data.length(); i ++) {
            char c = data.charAt(i);
            if (Character.isWhitespace(c)) {
                escapeStart = -1;
                dataBuf.append(' ');
            }
            else if (c == '&') {
                escapeStart = i;
            }
            else if (c == ';' && escapeStart >= 0) {
                String code = data.substring(escapeStart + 1, i).toLowerCase();
                if (code.equals("nbsp")) c = ' ';
                else if (code.equals("copy")) c = ' ';
                else if (code.equals("lt")) c = '<';
                else if (code.equals("gt")) c = '>';
                else if (code.equals("amp")) c = '&';
                else if (code.length() > 1 && code.charAt(0) == '#') {
                    try {
                        c = (char)Integer.parseInt(code.substring(1));
                    }
                    catch(NumberFormatException ex) {
                        c = ' ';
                    }
                }
                dataBuf.append(c);
                escapeStart = -1;
            }
            else if (escapeStart < 0) {
                dataBuf.append(c);
            }
        }
        if (escapeStart >= 0) {
            dataBuf.append(data.substring(escapeStart));
        }

        return dataBuf.toString().trim();
    }
    */
	
	public int getBufferSize() {
		return bufferSize;
	}
	
	public void setBufferSize(int size) {
		bufferSize = size;
	}
}

