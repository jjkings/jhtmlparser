/*
 * $Id: HtmlUtil.java,v 1.1.1.1 2006/10/29 12:29:46 jjking Exp $
 *
 * Copyright 2004, Kumasoft
 */

package org.kumasoft.html;

/**
 * The utility class to process HTML document.
 * @version $Revision: 1.1.1.1 $
 */
public class HtmlUtil
{
    /**
     * Decodes HTML escaped code in the specified data.
     */
    public static String decodeEscape(String data)
    {
        int length = data.length();
        StringBuffer decoded = new StringBuffer();
        int s = 0;
        int p0 = 0;
        for(int p = 0; p < length; p ++) {
            char c = data.charAt(p);
            switch(s) {
            case 0:
                if (c == '&') {
                    // start escape
                    s = 1;
                    p0 = p;
                }
                else {
                    decoded.append(c);
                }
                break;
            case 1:
                if (c == ';') {
                    String code = data.substring(p0 + 1, p).toLowerCase();
                    if (code.equals("nbsp")) c = ' ';
                    else if (code.equals("copy")) c = '?';
                    else if (code.equals("lt")) c = '<';
                    else if (code.equals("gt")) c = '>';
                    else if (code.equals("amp")) c = '&';
                    else if (code.length() > 1 && code.charAt(0) == '#') {
                        try {
                            c = (char)Integer.parseInt(code.substring(1));
                        }
                        catch(NumberFormatException ex) {
                            c = '?';
                        }
                    }
                    decoded.append(c);
                    s = 0;
                }
                else if (c == '&') {
                    // reset escape
                    decoded.append(data.substring(p0, p));
                    p0 = p;
                }
                else if (p - p0 > 8) {
                    // too long
                    decoded.append(data.substring(p0, p));
                    s = 0;
                } 
                break;
            }
        }

        return decoded.toString();
    }
}

