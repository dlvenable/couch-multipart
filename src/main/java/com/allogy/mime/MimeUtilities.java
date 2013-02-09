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

import org.apache.commons.fileupload.MultipartStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MimeUtilities
{
	static final int CarriageReturnCharacter = MultipartStream.CR;
	static final int LineFeedCharacter = MultipartStream.LF;
	static final String CRLFEnding = new String(new char[] { CarriageReturnCharacter, LineFeedCharacter});

	static int readToCharacter(int stopCharacter, InputStream stream, OutputStream outputStream) throws IOException
	{
		int lastCharacter = stream.read();
		while(lastCharacter != stopCharacter && lastCharacter != -1)
		{
			outputStream.write(lastCharacter);
			lastCharacter = stream.read();
		}
		return lastCharacter;
	}
	
	public static String readStringFromMimeSection(InputStream mimeInputStream) throws IOException
	{
		ByteArrayOutputStream outputStreamToStopCharacter = new ByteArrayOutputStream();
		int lastCharacter = readToCharacter(MimeUtilities.CarriageReturnCharacter, mimeInputStream, outputStreamToStopCharacter);
		if(lastCharacter == -1)
			throw new IOException("Stream ended before reading MIME section");

		lastCharacter = mimeInputStream.read();
		if(lastCharacter != MimeUtilities.LineFeedCharacter)
			throw new IOException("Carriage return found without line feed in MIME section");

		return outputStreamToStopCharacter.toString();
	}
}
