package com.google.health.android.example.googleapis;

import com.google.api.client.xml.XmlNamespaceDictionary;

import java.util.Map;

public class Namespace {
  public static final XmlNamespaceDictionary DICTIONARY = new XmlNamespaceDictionary();

  static {
    Map<String, String> map = DICTIONARY.namespaceAliasToUriMap;
//    map.put("", "http://www.w3.org/2005/Atom");
    map.put("atom", "http://www.w3.org/2005/Atom");
    map.put("batch", "http://schemas.google.com/gdata/batch");
    map.put("openSearch", "http://a9.com/-/spec/opensearch/1.1/");
    map.put("xml", "http://www.w3.org/XML/1998/namespace");
    map.put("ccr", "urn:astm-org:CCR");
    map.put("", "urn:astm-org:CCR");
  }
}
