/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.gateway.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class Urls {

  public static String ensureLeadingSlash( String s ) {
    if( s == null ) {
      return "/";
    } else if( s.startsWith( "/" ) ) {
      return s;
    } else {
      return "/" + s;
    }
  }

  public static String stripLeadingSlash( String s ) {
    if( s != null ) {
      s = s.trim();
      while( s.startsWith( "/" ) ) {
        s = s.substring(1);
      }
    }
    return s;
  }

  public static String concatUrl( String prefix, String suffix ) {
    if( suffix == null ) {
      return prefix;
    } else if ( prefix.endsWith( "/" ) && suffix.startsWith( "/" ) ) {
      return prefix + stripLeadingSlash( suffix );
    } else if( !prefix.endsWith( "/" ) && !suffix.startsWith( "/" ) ) {
      return prefix + "/" + suffix;
    } else {
      return prefix + suffix;
    }
  }

  public static boolean isIp(String domain) {
    Pattern p = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    Matcher m = p.matcher(domain);
    return m.find();
  }

  public static int dotOccurrences(String domain) {
    return domain.length() - domain.replace(".", "").length();
  }

  public static String getDomainName(String url) throws URISyntaxException {
    URI uri = new URI(url);
    String domain = uri.getHost();
    // if accessing via ip address do not wildcard the cookie domain
    if (isIp(domain)) {
      return domain;
    }
    if (dotOccurrences(domain) < 2) {
      if (!domain.startsWith(".")) {
        domain = "." + domain;
      }
      return domain;
    }
    int idx = domain.indexOf('.');
    if (idx == -1) {
      idx = 0;
    }
    return domain.substring(idx);
  }
}
