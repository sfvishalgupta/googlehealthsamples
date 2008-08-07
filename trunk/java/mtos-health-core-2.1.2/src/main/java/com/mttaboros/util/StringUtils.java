/* Copyright (c) 2006 Google Inc.
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

import java.util.*;

/**
 * String utility functions to support AuthSubUtil.
 */
public class StringUtils {

    private StringUtils() {
    }

    /**
     * Treats the provided long as unsigned and converts it to a string.
     *
     * @noinspection JavaDoc
     */
    public static String unsignedLongToString(long value) {
        if (value >= 0) {
            return Long.toString(value);
        } else {
            // Split into two unsigned halves.  As digits are printed out from
            // the bottom half, move data from the top half into the bottom
            // half
            int max_dig = 20;
            char[] cbuf = new char[max_dig];
            int radix = 10;
            int dst = max_dig;
            long top = value >>> 32;
            long bot = value & 0xffffffffl;
            bot += (top % radix) << 32;
            top /= radix;
            while (bot > 0 || top > 0) {
                cbuf[--dst] = Character.forDigit((int) (bot % radix), radix);
                bot = (bot / radix) + ((top % radix) << 32);
                top /= radix;
            }
            return new String(cbuf, dst, max_dig - dst);
        }
    }

    // from stringutil
    public static HashMap/*<String, String>*/ string2Map(String in, String delimEntry, String delimKey, boolean doStripEntry) {

        if (in == null) {
            return null;
        }

        HashMap/*<String, String>*/ out = new HashMap/*<String, String>*/();

        if (org.apache.commons.lang.StringUtils.isEmpty(delimEntry) || org.apache.commons.lang.StringUtils.isEmpty(delimKey)) {
            out.put(org.apache.commons.lang.StringUtils.strip(in), "");
            return out;
        }

        Iterator/*<String>*/ it = string2List(in, delimEntry, false).iterator();
        int len = delimKey.length();
        while (it.hasNext()) {
            String entry = (String) it.next();
            int pos = entry.indexOf(delimKey);
            if (pos > 0) {
                String value = entry.substring(pos + len);
                if (doStripEntry) {
                    value = org.apache.commons.lang.StringUtils.strip(value);
                }
                out.put(org.apache.commons.lang.StringUtils.strip(entry.substring(0, pos)), value);
            } else {
                out.put(org.apache.commons.lang.StringUtils.strip(entry), "");
            }
        }

        return out;
    }

    public static LinkedList/*<String>*/ string2List(String in, String delimiter, boolean doStrip) {
        if (in == null) {
            return null;
        }

        LinkedList/*<String>*/ out = new LinkedList/*<String>*/();
        string2Collection(in, delimiter, doStrip, out);
        return out;
    }

    /**
     * This converts a String to a Set of strings by extracting the substrings
     * between delimiter
     *
     * @param in        - what to process
     * @param delimiter - the delimiting string
     * @param doStrip   - to strip the substrings before adding to the list
     * @return Set
     */
    public static Set string2Set(String in, String delimiter, boolean doStrip) {
        if (in == null) {
            return null;
        }

        HashSet/*<String>*/ out = new HashSet/*<String>*/();
        string2Collection(in, delimiter, doStrip, out);
        return out;
    }

    /**
     * Converts a delimited string to a collection of strings. Substrings between
     * delimiters are extracted from the string and added to a collection that is
     * provided by the caller.
     *
     * @param in         The delimited input string to process
     * @param delimiter  The string delimiting entries in the input string.
     * @param doStrip    Whether to strip the substrings before adding to the
     *                   collection
     * @param collection The collection to which the strings will be added. If
     *                   <code>null</code>, a new <code>List</code> will be created.
     * @return The collection to which the substrings were added. This is
     *         syntactic sugar to allow call chaining.
     */
    public static Collection/*<String>*/ string2Collection(String in, String delimiter, boolean doStrip, Collection/*<String>*/ collection) {
        if (in == null) {
            return null;
        }
        if (collection == null) {
            collection = new ArrayList/*<String>*/();
        }
        if (delimiter == null || delimiter.length() == 0) {
            collection.add(in);
            return collection;
        }

        int fromIndex = 0;
        int pos;
        while ((pos = in.indexOf(delimiter, fromIndex)) >= 0) {
            String interim = in.substring(fromIndex, pos);
            if (doStrip) {
                interim = org.apache.commons.lang.StringUtils.strip(interim);
            }
            if (!doStrip || interim.length() > 0) {
                collection.add(interim);
            }

            fromIndex = pos + delimiter.length();
        }

        String interim = in.substring(fromIndex);
        if (doStrip) {
            interim = org.apache.commons.lang.StringUtils.strip(interim);
        }
        if (!doStrip || interim.length() > 0) {
            collection.add(interim);
        }

        return collection;
    }

    public static Map lowercaseKeys(Map map) {
        Map result = new HashMap(map.size());
        for (Iterator it = map.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            if (result.containsKey(key.toLowerCase())) {
                throw new IllegalArgumentException(
                        "Duplicate string key in map when lower casing");
            }
            result.put(key.toLowerCase(), map.get(key));
        }
        return result;
    }
}
