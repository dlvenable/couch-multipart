package com.allogy.couch.multipart;

import com.allogy.mime.MimeStreamingReader;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import temp.org.ektorp.support.AttachmentsInOrderParser;
import org.w3c.www.mime.MultipartInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * A class which can read a CouchDB MIME multipart/related message. It provides
 * the document and the attachments along with that document.
 */
public class CouchMultipart
{
    public static final String BoundaryHeaderName = "boundary";

    private String revision;
    private CouchMultipartAttachments attachments;
    private String document;

    public CouchMultipart(InputStream stream, List<Header> headers) throws IOException
    {
        String eTag = getHeader(headers, HttpHeaders.ETAG).getValue();
        revision = eTag.startsWith("\"") ? eTag.substring(1, eTag.length() - 1) : eTag;

        Header contentTypeHeader = getHeader(headers, HttpHeaders.CONTENT_TYPE);
        NameValuePair boundaryNameValuePair = contentTypeHeader.getElements()[0].getParameterByName(BoundaryHeaderName);
        String boundary = boundaryNameValuePair != null ?
                boundaryNameValuePair.getValue() :
                null;

        if (boundary == null)
        {
            document = IOUtils.toString(stream);
            attachments = null;
        }
        else
        {
            MultipartInputStream multipartInputStream = new MultipartInputStream(stream,
                    boundary.getBytes());

            multipartInputStream.nextInputStream();

            MimeStreamingReader mimeStreamingReader = new MimeStreamingReader(multipartInputStream);

            JsonFactory jsonFactory = new JsonFactory();
            document = IOUtils.toString(mimeStreamingReader.getContentInputStream());
            JsonParser jsonParser = jsonFactory.createJsonParser(document);
            List<String> orderedAttachmentNames = AttachmentsInOrderParser.parseAttachmentNames(jsonParser);
            attachments = new CouchMultipartAttachments(orderedAttachmentNames, multipartInputStream);
        }
    }

    public String getRevision()
    {
        return revision;
    }

    public CouchMultipartAttachments getAttachments()
    {
        return attachments;
    }

    public boolean hasAttachments()
    {
        return attachments != null;
    }

    public String getDocument()
    {
        return document;
    }

    private static Header getHeader(Iterable<Header> headers, final String headerName)
    {
        return Iterables.find(headers, new Predicate<Header>()
        {
            public boolean apply(Header header)
            {
                return header != null && headerName.equalsIgnoreCase(header.getName());
            }
        });
    }
}
