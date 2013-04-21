/**
 * Copyright (c) 2005-2013, Rory Ye
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the Jdkcn.com nor the names of its contributors may
 *       be used to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jdkcn.jabber.util;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

/**
 * The json utility.
 *
 * @author <a href="mailto:rory.cn@gmail.com">Rory</a>
 * @version $Id$
 */
public final class JsonUtil {

    /**
     * the jackson's object mapper.
     */
    private static ObjectMapper m;

    static {
        m = new ObjectMapper();
        m.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        m.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ"));
    }

    private static JsonFactory jf = new JsonFactory();


    /**
     * The private constructor for non instance this class.
     */
    private JsonUtil() {
    }

    /**
     * deserializer the json string to object.
     *
     * @param jsonAsString the json string.
     * @param pojoClass    the object java class.
     * @param <T>          the object's generic type.
     * @return deserializer java object
     * @throws IOException
     */
    public static <T> T fromJson(String jsonAsString, Class<T> pojoClass) throws IOException {
        return m.readValue(jsonAsString, pojoClass);
    }

    /**
     * deserializer the json string to object.
     *
     * @param jsonAsString the json string.
     * @param valueTypeRef full generics type.
     * @param <T>          the object's generic type.
     * @return deserializer java object
     * @throws IOException
     */
    public static <T> T fromJson(String jsonAsString, TypeReference<T> valueTypeRef) throws IOException {
        return m.readValue(jsonAsString, valueTypeRef);
    }

    /**
     * deserializer the json file to object.
     *
     * @param fr        the file reader.
     * @param pojoClass the object java class.
     * @param <T>       the object's generic type.
     * @return deserializer java object.
     * @throws IOException
     */
    public static <T> T fromJson(FileReader fr, Class<T> pojoClass) throws IOException {
        return m.readValue(fr, pojoClass);
    }

    public static <T> T fromJson(FileReader fr, TypeReference<T> valueTypeRef) throws IOException {
        return m.readValue(fr, valueTypeRef);
    }

    public static <T> T fromJson(InputStream is, Class<T> pojoClass) throws IOException {
        return m.readValue(is, pojoClass);
    }

    public static <T> T fromJson(InputStream is, TypeReference<T> valueTypeRef) throws IOException {
        return m.readValue(is, valueTypeRef);
    }

    public static <T> T fromJson(JsonNode root, Class<T> valueType) throws IOException {
        return m.readValue(root, valueType);
    }

    public static String toJson(Object pojo, boolean prettyPrint) throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator jg = jf.createJsonGenerator(sw);
        if (prettyPrint) {
            jg.useDefaultPrettyPrinter();
        }
        m.writeValue(jg, pojo);
        return sw.toString();
    }

    public static void toJson(Object pojo, FileWriter fw, boolean prettyPrint) throws IOException {
        JsonGenerator jg = jf.createJsonGenerator(fw);
        if (prettyPrint) {
            jg.useDefaultPrettyPrinter();
        }
        m.writeValue(jg, pojo);
    }
}
