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
package org.apache.hadoop.gateway.service.knoxsso;

import org.apache.hadoop.gateway.util.RegExUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class WebSSOResourceTest {
    /**
     * Domain name creation follows the following algorithm:
     * 1. if the incoming request hostname endsWith a configured domain suffix return the suffix - with prefixed dot
     * 2. if the request hostname is an ip address return null for default domain
     * 3. if the request hostname has less than 3 dots return null for default domain
     * 4. if request hostname has more than two dots strip the first element and return the remainder as domain
     * @throws Exception
     */
    @Test
    public void testDomainNameCreation() throws Exception {
        WebSSOResource resource = new WebSSOResource();
        // determine parent domain and wildcard the cookie domain with a dot prefix
        Assert.assertTrue(resource.getDomainName("http://www.local.com", null).equals(".local.com"));
        Assert.assertTrue(resource.getDomainName("http://ljm.local.com", null).equals(".local.com"));

        // test scenarios that will leverage the default cookie domain
        Assert.assertEquals(resource.getDomainName("http://local.home", null), null);
        Assert.assertEquals(resource.getDomainName("http://localhost", null), null); // chrome may not allow this

        Assert.assertTrue(resource.getDomainName("http://local.home.test.com", null).equals(".home.test.com"));

        // check the suffix config feature
        Assert.assertTrue(resource.getDomainName("http://local.home.test.com", ".test.com").equals(".test.com"));
        Assert.assertEquals(".novalocal", resource.getDomainName("http://34526yewt.novalocal", ".novalocal"));

        // make sure that even if the suffix doesn't start with a dot that the domain does
        // if we are setting a domain suffix then we want a specific domain for SSO and that
        // will require all hosts in the domain in order for it to work
        Assert.assertEquals(".novalocal", resource.getDomainName("http://34526yewt.novalocal", "novalocal"));

        // ip addresses can not be wildcarded - may be a completely different domain
        Assert.assertEquals(resource.getDomainName("http://127.0.0.1", null), null);
    }

    @Test
    public void testWhitelistMatching() throws Exception {
        String whitelist = "^https?://.*example.com:8080/.*$;" +
                "^https?://.*example.com/.*$;" +
                "^https?://.*example2.com:\\d{0,9}/.*$;" +
                "^https://.*example3.com:\\d{0,9}/.*$;" +
                "^https?://localhost:\\d{0,9}/.*$;^/.*$";

        // match on explicit hostname/domain and port
        Assert.assertTrue("Failed to match whitelist", RegExUtils.checkWhitelist(whitelist,
                "http://host.example.com:8080/"));
        // match on non-required port
        Assert.assertTrue("Failed to match whitelist", RegExUtils.checkWhitelist(whitelist,
                "http://host.example.com/"));
        // match on required but any port
        Assert.assertTrue("Failed to match whitelist", RegExUtils.checkWhitelist(whitelist,
                "http://host.example2.com:1234/"));
        // fail on missing port
        Assert.assertFalse("Matched whitelist inappropriately", RegExUtils.checkWhitelist(whitelist,
                "http://host.example2.com/"));
        // fail on invalid port
        Assert.assertFalse("Matched whitelist inappropriately", RegExUtils.checkWhitelist(whitelist,
                "http://host.example.com:8081/"));
        // fail on alphanumeric port
        Assert.assertFalse("Matched whitelist inappropriately", RegExUtils.checkWhitelist(whitelist,
                "http://host.example.com:A080/"));
        // fail on invalid hostname/domain
        Assert.assertFalse("Matched whitelist inappropriately", RegExUtils.checkWhitelist(whitelist,
                "http://host.example.net:8080/"));
        // fail on required port
        Assert.assertFalse("Matched whitelist inappropriately", RegExUtils.checkWhitelist(whitelist,
                "http://host.example2.com/"));
        // fail on required https
        Assert.assertFalse("Matched whitelist inappropriately", RegExUtils.checkWhitelist(whitelist,
                "http://host.example3.com/"));
        // match on localhost and port
        Assert.assertTrue("Failed to match whitelist", RegExUtils.checkWhitelist(whitelist,
                "http://localhost:8080/"));
        // match on local/relative path
        Assert.assertTrue("Failed to match whitelist", RegExUtils.checkWhitelist(whitelist,
                "/local/resource/"));
    }
}
