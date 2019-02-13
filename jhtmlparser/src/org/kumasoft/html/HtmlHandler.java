/*
 * $Id: HtmlHandler.java,v 1.1.1.1 2006/10/29 12:29:46 jjking Exp $
 *
 * Copyright 2004, Kumasoft
 */

package org.kumasoft.html;

/**
 * This is the interface of the object to handle contents in HTML.
 * @version $Revision: 1.1.1.1 $
 */
public interface HtmlHandler
{
    /**
     * This method is invoked when the parser starts to parse a document.
     * @exception HtmlException throws if application error occures
     */
    public void startDocument() throws HtmlException;

    /**
     * This method is invoked when the parser enbds to parse a document.
     * @exception HtmlException throws if application error occures
     */
    public void endDocument() throws HtmlException;

    /**
     * This method is invoked when parser find text part in HTML.
     * @param data the content.
     * @exception HtmlException throws if application error occures
     */
    public void content(String data) throws HtmlException;

    /**
     * This method is invoked when parser find a start of tag.
     * @param tag the tag.
     * @exception HtmlException throws if application error occures
     */
    public void startTag(Tag tag) throws HtmlException;

    /**
     * This method is invoked when parser find a end of tag.
     * @param tag the tag.
     * @exception HtmlException throws if application error occures
     */
    public void endTag(Tag tag) throws HtmlException;
}







