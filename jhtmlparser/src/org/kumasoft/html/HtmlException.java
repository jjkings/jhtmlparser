/*
 * $Id: HtmlException.java,v 1.1.1.1 2006/10/29 12:29:46 jjking Exp $
 *
 * Copyright 2004, Kumasoft.
 */

package org.kumasoft.html;

import java.io.IOException;

/**
 * The excepton to notify the HTML parsing error.
 * @version $Revision: 1.1.1.1 $
 */
public class HtmlException extends IOException
{
    /**
     * Create an empty exception.
     */
    public HtmlException ()
    {
        this("");
    }

    /**
     * Create new exception with the given information.
     */
    public HtmlException (String detail)
    {
        super(detail);
    }
}
