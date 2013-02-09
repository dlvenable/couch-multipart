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

package com.allogy.mime;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MimeStreamingReader
{
	private final InputStream mimeInputStream;
	private Iterable<Header> headers;

	public MimeStreamingReader(InputStream mimeInputStream)
	{
		if(mimeInputStream == null)
			throw new IllegalArgumentException("mimeInputStream");
		this.mimeInputStream = mimeInputStream;
	}

	private void readHeaders() throws IOException
	{
		if(headers != null)
			return;

		String currentHeaderString = MimeUtilities.readStringFromMimeSection(mimeInputStream);
		List<Header> headerList = new ArrayList<Header>();
		while (currentHeaderString.length() > 0)
		{
			Header header = createHeader(currentHeaderString);
			headerList.add(header);

			currentHeaderString = MimeUtilities.readStringFromMimeSection(mimeInputStream);
		}

		headers = headerList;
	}

	private Header createHeader(String currentHeaderString)
	{
		int nameValueSplitIndex = currentHeaderString.indexOf(":");

		String headerName = currentHeaderString.substring(0, nameValueSplitIndex);
		String headerValue = currentHeaderString.substring(nameValueSplitIndex + 1);
		headerValue = headerValue.trim();

		return new BasicHeader(headerName, headerValue);
	}

	public InputStream getContentInputStream() throws IOException
	{
		readHeaders();
		return mimeInputStream;
	}

	public Iterable<Header> getHeaders() throws IOException
	{
		readHeaders();
		return headers;
	}
}
