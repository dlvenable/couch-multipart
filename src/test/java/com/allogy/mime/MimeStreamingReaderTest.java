/*
 * Copyright (c) 2012 David Venable.
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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class MimeStreamingReaderTest
{
	private InputStream mimeInputStream;
	private List<Header> knownHeaders;
	private String contentString;

	@Before
	public void setUp()
	{
		StringBuilder mimeBodyPartStringBuilder = new StringBuilder();
		knownHeaders = new ArrayList<Header>();
		for(int i = 0; i < 3; i++)
		{
			Header header = new BasicHeader(UUID.randomUUID().toString(), UUID.randomUUID().toString());
			knownHeaders.add(header);
			mimeBodyPartStringBuilder.append(header.toString());
			mimeBodyPartStringBuilder.append("\r\n");
		}

		mimeBodyPartStringBuilder.append("\r\n");

		contentString = "hello world";

		mimeBodyPartStringBuilder.append(contentString);

		mimeInputStream = IOUtils.toInputStream(mimeBodyPartStringBuilder.toString());
	}
	
	private MimeStreamingReader createObjectUnderTest()
	{
		return new MimeStreamingReader(mimeInputStream);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_should_throw_if_input_stream_is_null()
	{
		new MimeStreamingReader(null);
	}

	@Test
	public void getHeaders_should_return_an_iterable_of_headers() throws IOException
	{
		Iterable<Header> headers = createObjectUnderTest().getHeaders();
		assertThat(Iterables.size(headers), is(knownHeaders.size()));

		for(final Header header: headers)
		{
			Header knownHeader = Iterables.find(knownHeaders, new Predicate<Header>()
            {
                public boolean apply(Header headerFromKnownHeaders)
                {
                    return headerFromKnownHeaders.getName().equals(header.getName());
                }
            });

			assertThat(header.getName(), is(knownHeader.getName()));
			assertThat(header.getValue(), is(knownHeader.getValue()));
		}
	}

	@Test
	public void getContentInputStream_should_not_return_null() throws IOException
	{
		assertThat(createObjectUnderTest().getContentInputStream(), notNullValue());
	}

	@Test
	public void getContentInputStream_should_return_the_mime_body_input_stream_excluding_headers() throws IOException
	{
		InputStream contentInputStream = createObjectUnderTest().getContentInputStream();

		assertThat(IOUtils.toString(contentInputStream), is(contentString));
	}

	@Test(expected = IOException.class)
	public void getContentInputStream_should_throw_exception_if_header_ends_without_carriage_return_line_feed() throws IOException
	{
		StringBuilder mimeBodyPartStringBuilder = new StringBuilder();
		String header = "header: value\r";
		mimeBodyPartStringBuilder.append(header);
		mimeBodyPartStringBuilder.append("\r\n");
		mimeBodyPartStringBuilder.append(contentString);

		mimeInputStream = IOUtils.toInputStream(mimeBodyPartStringBuilder.toString());

		createObjectUnderTest().getContentInputStream();
	}
	
	@Test(expected = IOException.class)
	public void getContentInputStream_should_throw_exception_if_header_part_ends_without_carriage_return_line_feed() throws IOException
	{
		StringBuilder mimeBodyPartStringBuilder = new StringBuilder();
		String header = "header: value\r\n";
		mimeBodyPartStringBuilder.append(header);
		mimeBodyPartStringBuilder.append("\r");
		mimeBodyPartStringBuilder.append(contentString);

		mimeInputStream = IOUtils.toInputStream(mimeBodyPartStringBuilder.toString());

		createObjectUnderTest().getContentInputStream();
	}

	@Test(expected = IOException.class)
	public void getContentInputStream_should_throw_exception_if_stream_ends_before_header_part_ends() throws IOException
	{
		String header = "header: value";

		mimeInputStream = IOUtils.toInputStream(header);

		createObjectUnderTest().getContentInputStream();
	}

	@Test
	public void getContentInputStream_after_explicit_call_to_getHeaders_should_return_the_rest_of_the_mime_body_input_stream() throws IOException
	{
		MimeStreamingReader objectUnderTest = createObjectUnderTest();
		objectUnderTest.getHeaders();
		InputStream contentInputStream = objectUnderTest.getContentInputStream();

		assertThat(IOUtils.toString(contentInputStream), is(contentString));
	}

	@Test
	public void getHeaders_after_explicit_call_to_getContentInputStream_should_return_the_headers() throws IOException
	{
		MimeStreamingReader objectUnderTest = createObjectUnderTest();
		objectUnderTest.getContentInputStream();
		Iterable<Header> headers = objectUnderTest.getHeaders();

		assertThat(Iterables.size(headers), is(knownHeaders.size()));
	}
}
