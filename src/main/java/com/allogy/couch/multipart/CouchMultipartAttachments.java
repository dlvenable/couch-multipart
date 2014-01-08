/*
 * Copyright (c) 2013 David Venable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.allogy.couch.multipart;

import com.allogy.mime.MimeStreamingReader;
import com.allogy.mime.MultipartInputStream;

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
