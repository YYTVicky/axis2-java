/*
* $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/server/ResponseWriter.java,v 1.5 2004/11/07 12:31:42 olegk Exp $
* $Revision: 155418 $
* $Date: 2005-02-26 08:01:52 -0500 (Sat, 26 Feb 2005) $
*
* ====================================================================
*
*  Copyright 1999-2004 The Apache Software Foundation
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation.  For more
* information on the Apache Software Foundation, please see
* <http://www.apache.org/>.
*
*/


package org.apache.axis2.transport.http.server;

import java.io.BufferedWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * Provides a hybrid Writer/OutputStream for sending HTTP response data
 */
public class ResponseWriter extends FilterWriter {
    public static final String CRLF = "\r\n";
    public static final String ISO_8859_1 = "ISO-8859-1";
    private OutputStream outStream = null;
    private String encoding = null;

    public ResponseWriter(final OutputStream outStream) throws UnsupportedEncodingException {
        this(outStream, CRLF, ISO_8859_1);
    }

    public ResponseWriter(final OutputStream outStream, final String encoding)
            throws UnsupportedEncodingException {
        this(outStream, CRLF, encoding);
    }

    public ResponseWriter(final OutputStream outStream, final String lineSeparator,
                          final String encoding)
            throws UnsupportedEncodingException {
        super(new BufferedWriter(new OutputStreamWriter(outStream, encoding)));
        this.outStream = outStream;
        this.encoding = encoding;
    }

    public void close() throws IOException {
        if (outStream != null) {
            super.close();
            outStream = null;
        }
    }

    /*
     *  (non-Javadoc)
     * @see java.io.Writer#flush()
     */
    public void flush() throws IOException {
        if (outStream != null) {
            super.flush();
            outStream.flush();
        }
    }

    public void print(int i) throws IOException {
        write(Integer.toString(i));
    }

    public void print(String s) throws IOException {
        if (s == null) {
            s = "null";
        }

        write(s);
    }

    public void println() throws IOException {
        write(CRLF);
    }

    public void println(int i) throws IOException {
        write(Integer.toString(i));
        write(CRLF);
    }

    public void println(String s) throws IOException {
        print(s);
        write(CRLF);
    }

    public void write(byte b) throws IOException {
        super.flush();
        outStream.write((int) b);
    }

    public void write(byte[] b) throws IOException {
        super.flush();
        outStream.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        super.flush();
        outStream.write(b, off, len);
    }

    public String getEncoding() {
        return encoding;
    }
}
