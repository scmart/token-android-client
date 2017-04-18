/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.crypto.signal.util;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> T fromJson(byte[] serialized, Class<T> clazz) throws IOException {
        return fromJson(new String(serialized), clazz);
    }

    public static <T> T fromJson(String serialized, Class<T> clazz) throws IOException {
        return objectMapper.readValue(serialized, clazz);
    }

    public static <T> T fromJson(InputStream serialized, Class<T> clazz) throws IOException {
        return objectMapper.readValue(serialized, clazz);
    }

    public static <T> T fromJson(Reader serialized, Class<T> clazz) throws IOException {
        return objectMapper.readValue(serialized, clazz);
    }

    public static String toJson(Object object) throws IOException {
        return objectMapper.writeValueAsString(object);
    }

    public static ObjectMapper getMapper() {
        return objectMapper;
    }
}
