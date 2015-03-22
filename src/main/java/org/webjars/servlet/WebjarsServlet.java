package org.webjars.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>This servlet enables Servlet 2.x compliant containers to serve up Webjars resources</p>
 * <p>To use it just declare it in your web.xml as follows:</p>
 * <pre>
 &lt;!--Webjars Servlet--&gt;
 &lt;servlet&gt;
     &lt;servlet-name&gt;WebjarsServlet&lt;/servlet-name&gt;
     &lt;servlet-class&gt;org.webjars.servlet.WebjarsServlet&lt;/servlet-class&gt;
 &lt;/servlet&gt;
 &lt;servlet-mapping&gt;
     &lt;servlet-name&gt;WebjarsServlet&lt;/servlet-name&gt;
     &lt;url-pattern&gt;/webjars/*&lt;/url-pattern&gt;
 &lt;/servlet-mapping&gt;œ
 </pre>
 * @author Angel Ruiz<aruizca@gmail.com>
 */
public class WebjarsServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(WebjarsServlet.class.getName());

    private static final long DEFAULT_EXPIRE_TIME_MS = 86400000L; // 1 day
    private static final long DEFAULT_EXPIRE_TIME_S = 86400L; // 1 day

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String webjarsResourceURI = "/META-INF/resources" + request.getRequestURI().replaceFirst(request.getContextPath(), "");
        logger.log(Level.INFO, "Webjars resource requested: " + webjarsResourceURI);
        InputStream inputStream = this.getClass().getResourceAsStream(webjarsResourceURI);
        if (inputStream != null) {
            prepareChacheHeaders(response, webjarsResourceURI);
            copy(inputStream, response.getOutputStream());
        } else {
            // return HTTP error
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     *
     * @param response
     * @param webjarsResourceURI
     */
    private void prepareChacheHeaders(HttpServletResponse response, String webjarsResourceURI) {
        String[] tokens = webjarsResourceURI.split("/");
        String version = tokens[5];
        String fileName = tokens[tokens.length - 1];

        String eTag = fileName + "_" + version;
        response.setHeader("ETag", eTag);
        response.setDateHeader("Expires", System.currentTimeMillis() + DEFAULT_EXPIRE_TIME_MS);
        response.addHeader("Cache-Control", "private, max-age=" + DEFAULT_EXPIRE_TIME_S);
    }

        /* Important!!*/
    /* The code bellow has been copied from apache Commons IO. More specifically from its IOUtils class. */
    /* The reason is becasue I don't want to include any more dependencies */

    /**
     * The default buffer size ({@value}) to use for
     * {@link #copyLarge(InputStream, java.io.OutputStream)}
     * and
     * {@link #copyLarge(java.io.InputStream, java.io.OutputStream)}
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private static final int EOF = -1;

    // copy from InputStream
    //-----------------------------------------------------------------------
    /**
     * Copy bytes from an <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * <p>
     * Large streams (over 2GB) will return a bytes copied value of
     * <code>-1</code> after the copy has completed since the correct
     * number of bytes cannot be returned as an int. For large streams
     * use the <code>copyLarge(InputStream, OutputStream)</code> method.
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output  the <code>OutputStream</code> to write to
     * @return the number of bytes copied, or -1 if &gt; Integer.MAX_VALUE
     * @throws NullPointerException if the input or output is null
     * @throws IOException if an I/O error occurs
     * @since 1.1
     */
    private static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    /**
     * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * <p>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output  the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException if an I/O error occurs
     * @since 1.3
     */
    private static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        return copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE]);
    }

    /**
     * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p>
     * This method uses the provided buffer, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * <p>
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output  the <code>OutputStream</code> to write to
     * @param buffer the buffer to use for the copy
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException if an I/O error occurs
     * @since 2.2
     */
    private static long copyLarge(InputStream input, OutputStream output, byte[] buffer)
            throws IOException {
        long count = 0;
        int n = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
