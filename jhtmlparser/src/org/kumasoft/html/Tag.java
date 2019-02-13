/*
 * $Id: Tag.java,v 1.1.1.1 2006/10/29 12:29:46 jjking Exp $
 *
 * Copyright 2004, Kumasoft
 */

package org.kumasoft.html;

import java.util.Map;
import java.util.Iterator;

/**
 * This class represents a tag for HTML documents.
 * @version $Revision: 1.1.1.1 $
 */
public class Tag
{
    /**
     * The type for html tag.
     */
    public final static String HTML = "HTML";
    public final static String HEAD = "HEAD";
    public final static String TITLE = "TITLE";
    public final static String BODY = "BODY";
    public final static String UL = "UL";
    public final static String OL = "OL";
    public final static String LI = "LI";
    public final static String H1 = "H1";
    public final static String H2 = "H2";
    public final static String H3 = "H3";
    public final static String H4 = "H4";
    public final static String H5 = "H5";
    public final static String H6 = "H6";
    public final static String IMG = "IMG";
    public final static String A = "A";
    public final static String P = "P";
    public final static String B = "B";
    public final static String HR = "HR";
    public final static String BR = "BR";
    public final static String PRE = "PRE";
    public final static String TABLE = "TABLE";
    public final static String TR = "TR";
    public final static String TH = "TH";
    public final static String TD = "TD";
    public final static String META = "META";

    public final static String FRAMESET = "FRAMESET";
    public final static String FRAME = "FRAME";

    private final static String[] TYPES = {
	IMG
	, TD
	, TH
	, TR
	, A
	, P
	, B
	, UL
	, OL
	, LI
	, HR
	, BR
	, PRE
	, H1
	, H2
	, H3
	, H4
	, H5
	, H6
	, TABLE
	, HTML
	, HEAD
	, TITLE
	, BODY,
	META,
	FRAME,
	FRAMESET,
    };

    /**
     * The attributes for html tag.
     */
    public final static String ATTR_HREF = "href";
    public final static String ATTR_NAME = "name";
    public final static String ATTR_SRC = "src";

    private final static String[] ATTRS = { ATTR_HREF
					    , ATTR_NAME
					    , ATTR_SRC
    };

    /**
     * The type of tag.
     */
    private String type;

    /**
     * The list of attributes.
     */
    private Map attributes;

    /**
     * Create a HTML tag that has specified type.
     * @param type HTML tag type.
     */
    public Tag(String type)
    {
	this(type, null);
    }

    /**
     * Create a HTML tag that has specified type.
     * If emptyElement is true, this tag has end tag.
     * @param type HTML tag type.
     * @param attributes the attributes list.
     */
    public Tag(String type, Map attributes)
    {
	this.type = type;
	this.attributes = attributes;
    }

    /**
     * Return the type of tag.
     */
    public String getType()
    {
	return type;
    }

    /**
     * Return the attribute which has the specified name.
     * If no attribute is found, returns null.
     */
    public String getAttribute(String name)
    {
	if (attributes == null) return null;
	return (String)attributes.get(name.toLowerCase());
    }

    /**
     * Returns true if the type is the specified one.
     */
    public boolean isType(String t)
    {
	return type.equals(t.toUpperCase());
    }

    /**
     * Return the String reprensetation of the tag.
     */
    public String toString()
    {
	StringBuffer buf = new StringBuffer();
	buf.append(type);
	buf.append('|');
	if (attributes != null) {
	    for (Iterator enu = attributes.keySet().iterator(); enu.hasNext();) {
		String name = (String)enu.next();
		buf.append(name);
		buf.append('=');
		buf.append(attributes.get(name));
		buf.append(';');
	    }
	}
	return buf.toString();
    }

    /**
     * Return true if this and given object is same.
     * If the given object is Tag, compares tag name and attributes.
     * If it is string, compares only tag name.
     */
    public boolean equals(Object obj)
    {
	if (obj instanceof Tag) {
	    return type.equals(((Tag)obj).getType());
	}
		
	return false;
    }

    /**
     * Allocate shared object for the given type to save heap memory.
     * @param type the type string.
     * @param attrbutes the list of attributes belongs this tag.
     */
    protected static Tag createTag(String type, Map attributes)
    {
	return new Tag(type.toUpperCase(), attributes);
    }
}
