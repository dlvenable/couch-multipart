package com.allogy.couch.multipart;

import com.allogy.mime.MimeStreamingReader;
import org.w3c.www.mime.MultipartInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * A class for reading CouchDB multipart/related messages through streaming. This
 * class will supply the attachment name along with each InputStream.
 */
public class CouchMultipartAttachments
{
    private final MultipartInputStream multipartInputStream;
    private final Iterator<String> currentAttachmentIterator;
    private String currentAttachmentName;

    CouchMultipartAttachments(List<String> orderedAttachmentNames, MultipartInputStream multipartInputStream) throws IOException
    {
        this.multipartInputStream = multipartInputStream;

        currentAttachmentIterator = orderedAttachmentNames.iterator();
    }

    public boolean nextInputStream() throws IOException
    {
        if(currentAttachmentIterator.hasNext() && multipartInputStream.nextInputStream())
        {
            currentAttachmentName = currentAttachmentIterator.next();
            return true;
        }

        return false;
    }

    public InputStream getAttachmentInputStream() throws IOException
    {
        return new MimeStreamingReader(multipartInputStream).getContentInputStream();
    }

    public String getAttachmentName()
    {
        return currentAttachmentName;
    }
}
