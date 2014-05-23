package org.baeldung.httpclient;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HttpClientMultipartTest {

    private static final String SERVER = "http://echo.200please.com";
    private static final String textFileName = "temp.txt";
    private static final String imageFileName = "image.jpg";
    private static final String zipFileName = "zipFile.zip";
    private static final Logger LOGGER = Logger.getLogger("org.baeldung.httpclient.HttpClientMultipartTest");
    private CloseableHttpClient client;
    private HttpPost post;
    private BufferedReader rd;
    private CloseableHttpResponse response;
    private java.util.logging.Logger log;

    @Before
    public final void before() {
        client = HttpClientBuilder.create().build();
        post = new HttpPost(SERVER);
    }

    @After
    public final void after() throws IllegalStateException, IOException {
        post.completed();
        try {
            client.close();
        } catch (final IOException e1) {
            LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
            throw e1;
        }
        try {
            rd.close();
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        }
        try {
            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                final InputStream instream = entity.getContent();
                instream.close();
            }
        } finally {
            response.close();
        }
    }

    // tests

    @Test
    public final void givenFileandMultipleTextParts_whenUploadwithAddPart_thenNoExceptions() throws IOException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource("uploads/" + textFileName);
        final File file = new File(url.getPath());
        final FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
        final StringBody stringBody1 = new StringBody("This is message 1", ContentType.MULTIPART_FORM_DATA);
        final StringBody stringBody2 = new StringBody("This is message 2", ContentType.MULTIPART_FORM_DATA);
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("upfile", fileBody);
        builder.addPart("text1", stringBody1);
        builder.addPart("text2", stringBody2);
        final HttpEntity entity = builder.build();
        post.setEntity(entity);
        response = client.execute(post);
        final int statusCode = response.getStatusLine().getStatusCode();
        final String responseString = getContent();
        final String contentTypeInHeader = getContentTypeHeader();
        assertThat(statusCode, equalTo(HttpStatus.SC_OK));
        assertTrue(responseString.contains("Content-Type: multipart/form-data;"));
        assertTrue(contentTypeInHeader.contains("Content-Type: multipart/form-data;"));
        System.out.println(responseString);
        System.out.println("POST Content Type: " + contentTypeInHeader);
    }

    @Test
    public final void givenFileandTextPart_whenUploadwithAddBinaryBodyandAddTextBody_ThenNoExeption() throws ClientProtocolException, IOException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource("uploads/" + textFileName);
        final File file = new File(url.getPath());
        final String message = "This is a multipart post";
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addBinaryBody("upfile", file, ContentType.DEFAULT_BINARY, textFileName);
        builder.addTextBody("text", message, ContentType.DEFAULT_BINARY);
        final HttpEntity entity = builder.build();
        post.setEntity(entity);
        response = client.execute(post);
        final int statusCode = response.getStatusLine().getStatusCode();
        final String responseString = getContent();
        final String contentTypeInHeader = getContentTypeHeader();
        assertThat(statusCode, equalTo(HttpStatus.SC_OK));
        assertTrue(responseString.contains("Content-Type: multipart/form-data;"));
        assertTrue(contentTypeInHeader.contains("Content-Type: multipart/form-data;"));
        System.out.println(responseString);
        System.out.println("POST Content Type: " + contentTypeInHeader);
    }

    @Test
    public final void givenFileandInputStreamandText_whenUploadwithAddBinaryBodyandAddTextBody_ThenNoException() throws ClientProtocolException, IOException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource("uploads/" + zipFileName);
        final URL url2 = Thread.currentThread().getContextClassLoader().getResource("uploads/" + imageFileName);
        final InputStream inputStream = new FileInputStream(url.getPath());
        final File file = new File(url2.getPath());
        final String message = "This is a multipart post";
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addBinaryBody("upfile", file, ContentType.DEFAULT_BINARY, imageFileName);
        builder.addBinaryBody("upstream", inputStream, ContentType.create("application/zip"), zipFileName);
        builder.addTextBody("text", message, ContentType.TEXT_PLAIN);
        final HttpEntity entity = builder.build();
        post.setEntity(entity);
        response = client.execute(post);
        final int statusCode = response.getStatusLine().getStatusCode();
        final String responseString = getContent();
        final String contentTypeInHeader = getContentTypeHeader();
        assertThat(statusCode, equalTo(HttpStatus.SC_OK));
        assertTrue(responseString.contains("Content-Type: multipart/form-data;"));
        assertTrue(contentTypeInHeader.contains("Content-Type: multipart/form-data;"));
        System.out.println(responseString);
        System.out.println("POST Content Type: " + contentTypeInHeader);
        inputStream.close();
    }

    @Test
    public final void givenCharArrayandText_whenUploadwithAddBinaryBodyandAddTextBody_ThenNoException() throws ClientProtocolException, IOException {
        final String message = "This is a multipart post";
        final byte[] bytes = "binary code".getBytes();
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addBinaryBody("upfile", bytes, ContentType.DEFAULT_BINARY, textFileName);
        builder.addTextBody("text", message, ContentType.TEXT_PLAIN);
        final HttpEntity entity = builder.build();
        post.setEntity(entity);
        response = client.execute(post);
        final int statusCode = response.getStatusLine().getStatusCode();
        final String responseString = getContent();
        final String contentTypeInHeader = getContentTypeHeader();
        assertThat(statusCode, equalTo(HttpStatus.SC_OK));
        assertTrue(responseString.contains("Content-Type: multipart/form-data;"));
        assertTrue(contentTypeInHeader.contains("Content-Type: multipart/form-data;"));
        System.out.println(responseString);
        System.out.println("POST Content Type: " + contentTypeInHeader);
    }

    // UTIL

    final String getContent() throws IOException {
        rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String body = "";
        String content = "";
        while ((body = rd.readLine()) != null) {
            content += body + "\n";
        }
        return content.trim();
    }

    final String getContentTypeHeader() throws IOException {
        return post.getEntity().getContentType().toString();
    }

}
