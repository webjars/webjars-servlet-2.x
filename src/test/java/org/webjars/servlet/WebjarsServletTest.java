package org.webjars.servlet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebjarsServletTest {

    private static final String ANY_CONTEXT_PATH = "/app";
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ServletContext servletContext;
    @Mock
    private ServletConfig servletConfig;
    @Mock
    private ServletOutputStream servletOutputStream;
    @Spy
    private WebjarsServlet sut;

    @Before
    public void setUp() throws Exception {
        when(request.getContextPath()).thenReturn(ANY_CONTEXT_PATH);
        when(sut.getServletConfig()).thenReturn(servletConfig);
        when(sut.getServletContext()).thenReturn(servletContext);
        when(response.getOutputStream()).thenReturn(servletOutputStream);
    }

    @Test
    public void shouldWriteContentForFileAvailable() throws Exception {
        when(request.getRequestURI()).thenReturn(ANY_CONTEXT_PATH + "/webjars/foo/1.0/bar.js");
        when(servletContext.getMimeType("bar.js")).thenReturn("application/javascript");

        sut.doGet(request, response);

        verify(servletOutputStream, atLeastOnce()).write(any(byte[].class), anyInt(), anyInt());
        verify(response).setHeader("ETag", "bar.js_1.0");
    }

    @Test
    public void shouldSendForbiddenForDirectoryRequest() throws Exception {
        when(request.getRequestURI()).thenReturn(ANY_CONTEXT_PATH + "/webjars/foo/1.0/");

        sut.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void shouldSendNotFoundForInsufficientFileRequest() throws Exception {
        when(request.getRequestURI()).thenReturn(ANY_CONTEXT_PATH + "/foo/bar.js");

        sut.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
