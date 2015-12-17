package org.webjars.servlet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class WebjarsServletTest {
    
    private WebjarsServlet webjarsServlet;
    
    @Before
    public void beforeSetup() {
        
        this.webjarsServlet = new WebjarsServlet();
    }
    
    /*
     * 
     */
    @Test
    public void testNormalGetETagName() {
        
        String webjarsResourceURI = "/META-INF/resources/webjars/jquery/1.11.3/jquery.js";
        
        String eTagName = webjarsServlet.getETagName(webjarsResourceURI);
        
        Assert.assertEquals("jquery.js_1.11.3", eTagName);
        
    }

    /*
     * fix the ArrayIndexOutOfBoundsException to return empty string.
     */
    @Test
    public void testExceptionGetETagName() {
        
        String webjarsResourceURI = "/META-INF/resources/webjars/jquery";
        
        String eTagName = webjarsServlet.getETagName(webjarsResourceURI);
        
        Assert.assertEquals("", eTagName);
        
    }
}
