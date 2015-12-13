package org.webjars.servlet;

import javax.servlet.ServletConfig;
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
 * @author Angel Ruiz&lt;aruizca@gmail.com&gt;
 */
public class WebjarsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    private static final Logger logger = Logger.getLogger(WebjarsServlet.class.getName());
    
    private static final long DEFAULT_EXPIRE_TIME_MS = 86400000L; // 1 day
    private static final long DEFAULT_EXPIRE_TIME_S = 86400L; // 1 day

    private boolean disableCache = false;

    @Override
    public void init() throws ServletException {
        ServletConfig config = getServletConfig();
        if(config == null) {
            throw new NullPointerException("Expected servlet container to provide a non-null ServletConfig.");
        }
        try {
            String disableCache = config.getInitParameter("disableCache");
            if (disableCache != null) {
                this.disableCache = Boolean.parseBoolean(disableCache);
                logger.log(Level.INFO, "WebjarsServlet cache enabled: " + !this.disableCache);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "The WebjarsServlet configuration parameter \"disableCache\" is invalid");
        }
        logger.log(Level.INFO, "WebjarsServlet initialization completed");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String webjarsResourceURI = "/META-INF/resources" + request.getRequestURI().replaceFirst(request.getContextPath(), "");
        logger.log(Level.INFO, "Webjars resource requested: " + webjarsResourceURI);
        
        String eTagName = this.getETagName(webjarsResourceURI);
        
        if (!disableCache) {
            if (checkETagMatch(request, eTagName)
                   || checkLastModify(request)) {
               // response.sendError(HttpServletResponse.SC_NOT_MODIFIED); 
               response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
               return;
            }
       }
       
        
        InputStream inputStream = this.getClass().getResourceAsStream(webjarsResourceURI);
        if (inputStream != null) {
            try {
                if (!disableCache) {
                    prepareCacheHeaders(response, eTagName);
                }
                String filename = getFileName(webjarsResourceURI);
                String mimeType = this.getServletContext().getMimeType(filename);

                response.setContentType(mimeType != null ? mimeType : "application/octet-stream");
                copy(inputStream, response.getOutputStream());
            } finally {
                inputStream.close();
            }
        } else {
            // return HTTP error
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     *
     * @param webjarsResourceURI
     * @return
     */
    private String getFileName(String webjarsResourceURI) {
        String[] tokens = webjarsResourceURI.split("/");
        return tokens[tokens.length - 1];
    }

    
    /**
     * 
     * @param webjarsResourceURI
     * @return
     */
    private String getETagName(String webjarsResourceURI) {
    	
    	String[] tokens = webjarsResourceURI.split("/");
        String version = tokens[5];
        String fileName = tokens[tokens.length - 1];

        String eTag = fileName + "_" + version;
        return eTag;
    }
    
    /**
     * 
     * @param request
     * @param eTagName
     * @return
     */
    private boolean checkETagMatch(HttpServletRequest request, String eTagName) {
    
       String token = request.getHeader("If-None-Match");
       return (token == null ? false: token.equals(eTagName));
    }

    /**
     * 
     * @param request
     * @return
     */
    private boolean checkLastModify(HttpServletRequest request) {
    	
       long last = request.getDateHeader("If-Modified-Since");
       return (last == -1L? false : (last - System.currentTimeMillis() > 0L));
    }
    
    /**
     *
     * @param response
     * @param webjarsResourceURI
     */
    private void prepareCacheHeaders(HttpServletResponse response, String eTag) {
        
        response.setHeader("ETag", eTag);
        response.setDateHeader("Expires", System.currentTimeMillis() + DEFAULT_EXPIRE_TIME_MS);
        response.addDateHeader("Last-Modified", System.currentTimeMillis() + DEFAULT_EXPIRE_TIME_MS); 
        response.addHeader("Cache-Control", "private, max-age=" + DEFAULT_EXPIRE_TIME_S);
    }

        /* Important!!*/
    /* The code bellow has been copied from apache Commons IO. More specifically from its IOUtils class. */
    /* The reason is becasue I don't want to include any more dependencies */

    /**
     * The default buffer size ({@value}) to use
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
        long count = 0;
        int n = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }
}
