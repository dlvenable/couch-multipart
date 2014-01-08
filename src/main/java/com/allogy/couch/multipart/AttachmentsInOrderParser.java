package com.allogy.couch.multipart;


import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is available in Ektorp source, but not in a release as of 1.3.0.
 * Including it here also allows us to keep Ektorp out of the dependency list.
 *
 * A document's MIME multipart/related representation produced by CouchDB
 * uses the order of the attachments in the JSON _attachments object as the
 * order of the attachments in the multipart/related. Thus, the order must
 * be preserved in order to parse a CouchDB multipart/related message.
 *
 * This class parses a document and returns the order of the attachments.
 *
 * @author David Venable
 */
class AttachmentsInOrderParser
{
    private static final String ATTACHMENTS_NAME = "_attachments";

    /**
     * Parses a CouchDB document in the form of a JsonParser to get the
     * attachments order. It is important that the JsonParser come straight
     * from the source document and not from an object, or the order will
     * be incorrect.
     * @param documentJsonParser a JsonParser which is at the very root of a JSON CouchDB document
     * @return the list of attachment names in the order provided in the document
     * @throws java.io.IOException
     */
    public static List<String> parseAttachmentNames(JsonParser documentJsonParser) throws IOException
    {
        documentJsonParser.nextToken();

        JsonToken jsonToken;
        while((jsonToken = documentJsonParser.nextToken()) != JsonToken.END_OBJECT)
        {
            if(ATTACHMENTS_NAME.equals(documentJsonParser.getCurrentName()))
            {
                return readAttachments(documentJsonParser);
            }
            else if(jsonToken == JsonToken.START_OBJECT)
            {
                readIgnoreObject(documentJsonParser);
            }
        }
        return null;
    }

    private static List<String> readAttachments(JsonParser jsonParser) throws IOException
    {
        jsonParser.nextToken();
        return readAttachmentsObject(jsonParser);
    }

    private static List<String> readAttachmentsObject(JsonParser jsonParser) throws IOException
    {
        List<String> attachmentNameList = new ArrayList<String>();
        while(jsonParser.nextToken() != JsonToken.END_OBJECT)
        {
            String attachmentName = jsonParser.getCurrentName();

            jsonParser.nextToken();
            if(jsonParser.getCurrentToken() != JsonToken.START_OBJECT)
            {
                String message = ATTACHMENTS_NAME + " contains an invalid object.";
                throw new JsonParseException(message, jsonParser.getCurrentLocation());
            }

            readIgnoreObject(jsonParser);

            attachmentNameList.add(attachmentName);
        }

        return attachmentNameList;
    }

    private static void readIgnoreObject(JsonParser jsonParser) throws IOException
    {
        while(jsonParser.nextToken() != JsonToken.END_OBJECT)
        {
            if(jsonParser.getCurrentToken() == JsonToken.START_OBJECT)
            {
                readIgnoreObject(jsonParser);
            }
        }
    }
}
