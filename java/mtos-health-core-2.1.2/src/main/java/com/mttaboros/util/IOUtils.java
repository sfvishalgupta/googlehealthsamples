/* Copyright (c) 2007 Mt. Tabor OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mttaboros.util;

import java.io.*;

/**
 * IO Utility class.
 *
 * @author cb@mttaboros.com
 */
public class IOUtils {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Converts an InputStream into a string.
     *
     * @param input The <code>InputStream</code> to convert to a string
     * @return the contents of the InputStream as a String
     */
    public static String toString(InputStream input) {
        StringWriter sw = new StringWriter();
        copy(new InputStreamReader(input), sw);
        return sw.toString();
    }

    /**
     * Copies characters from a reader to a writer.
     *
     * @param input  The Reader to copy characters from.
     * @param output The Writer to copy the characters to.
     */
    public static void copy(Reader input, Writer output) {
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int n;
        try {
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
