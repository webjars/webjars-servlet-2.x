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
    public void shouldWriteContentForAvailableFile() throws Exception {
    	shouldWriteContentForAvailableFile("/webjars/foo/1.0/bar.js");
    }

    @Test
    public void shouldWriteContentForAvailableFileInSubFolder() throws Exception {
    	shouldWriteContentForAvailableFile("/webjars/foo/1.0/sub/bar.js");
    }

    @Test
    public void shouldWriteContentForAvailableFileVersionAgnostic() throws Exception {
    	shouldWriteContentForAvailableFile("/webjars/foo/bar.js");
    }

    @Test
    public void shouldWriteContentForAvailableFileInSubFolderVersionAgnostic() throws Exception {
        shouldWriteContentForAvailableFile("/webjars/foo/sub/bar.js");
    }

    private void shouldWriteContentForAvailableFile(String path) throws Exception {
        when(request.getRequestURI()).thenReturn(ANY_CONTEXT_PATH + path);
        when(request.getServletPath()).thenReturn("/webjars/");
        
        when(servletContext.getMimeType("bar.js")).thenReturn("application/javascript");

        sut.init();
        sut.doGet(request, response);

        verify(servletOutputStream, atLeastOnce()).write(any(byte[].class), anyInt(), anyInt());
        verify(response).setHeader("ETag", "bar.js_1.0");
    }

    @Test
    public void shouldSendForbiddenForDirectoryRequest() throws Exception {
        shouldSendForbiddenForDirectoryRequest("/webjars/foo/1.0/");
    }

    @Test
    public void shouldSendForbiddenForDirectoryRequestVersionAgnostic() throws Exception {
        shouldSendForbiddenForDirectoryRequest("/webjars/foo/sub/");
    }

    private void shouldSendForbiddenForDirectoryRequest(String path) throws Exception {
        when(request.getRequestURI()).thenReturn(ANY_CONTEXT_PATH + path);

        sut.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void shouldSendNotFoundForInsufficientFileRequest() throws Exception {
        shouldSendNotFound("/foo/bar.js");
    }

    @Test
    public void shouldSendNotFoundForNonExistingFileRequest() throws Exception {
        shouldSendNotFound("/webjars/foo/1.0/wrong.js");
    }

    @Test
    public void shouldSendNotFoundForNonExistingFileRequestVersionAgnostic() throws Exception {
        shouldSendNotFound("/webjars/foo/bar/wrong.js");
    }

    private void shouldSendNotFound(String path) throws Exception {
        when(request.getRequestURI()).thenReturn(ANY_CONTEXT_PATH + path);

        sut.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
