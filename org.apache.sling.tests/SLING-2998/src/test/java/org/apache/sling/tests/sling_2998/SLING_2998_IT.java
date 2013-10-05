/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.tests.sling_2998;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.inject.Inject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.sling.auth.core.impl.LoginServlet;
import org.apache.sling.auth.core.impl.LogoutServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.launchpad.karaf.testing.KarafTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.util.Filter;
import org.osgi.framework.BundleContext;

import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFileExtend;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class SLING_2998_IT extends KarafTestSupport {

    @Inject
    protected BundleContext bundleContext;

    @Inject
    @Filter(timeout = 240000)
    protected SlingRepository slingRepository;

    public static final String BASE_URL = "http://localhost:8181";

    @Configuration
    public Option[] configuration() {
        return new Option[]{
            karafDistributionConfiguration().frameworkUrl(maven().groupId(karafGroupId()).artifactId(karafArtifactId()).version(karafVersion()).type("tar.gz")).karafVersion(karafVersion()).name(karafName()).unpackDirectory(new File("target/paxexam/")),
            keepRuntimeFolder(),
            logLevel(LogLevelOption.LogLevel.INFO),
            editConfigurationFileExtend("etc/org.apache.karaf.features.cfg", "featuresRepositories", ",mvn:org.apache.sling/org.apache.sling.tests.features/3/xml/features"),
            editConfigurationFileExtend("etc/org.apache.karaf.features.cfg", "featuresBoot", ",SLING-2998"),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.launchpad.karaf-integration-tests").version("0.1.1-SNAPSHOT")
        };
    }

    @Test
    public void testRoot() throws Exception {
        final HttpClient httpClient = new HttpClient();
        final HttpMethod get = new GetMethod(BASE_URL);

        httpClient.executeMethod(get);
        assertEquals(200, get.getStatusCode());
    }

    @Test
    public void testLoginServlet() throws Exception {
        final HttpClient httpClient = new HttpClient();
        final HttpMethod get = new GetMethod(BASE_URL + LoginServlet.SERVLET_PATH);

        httpClient.executeMethod(get);
        assertEquals(403, get.getStatusCode());

        httpClient.getParams().setAuthenticationPreemptive(true);
        httpClient.getState().setCredentials(
            AuthScope.ANY,
            new UsernamePasswordCredentials("admin", "admin")
        );
        httpClient.executeMethod(get);
        assertEquals(200, get.getStatusCode());
    }

    @Test
    public void testLogoutServlet() throws Exception {
        final HttpClient httpClient = new HttpClient();
        final HttpMethod get = new GetMethod(BASE_URL + LogoutServlet.SERVLET_PATH);

        httpClient.executeMethod(get);
        assertEquals(200, get.getStatusCode());
    }

    @Test
    public void testSlingTest() throws Exception {
        final HttpClient httpClient = new HttpClient();
        final HttpMethod get = new GetMethod(BASE_URL + "/sling-test/sling/sling-test.html");

        httpClient.executeMethod(get);
        assertEquals(200, get.getStatusCode());

        final Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put("sling.auth.requirements", "+/sling-test");
        bundleContext.registerService(Object.class.getName(), new Object(), properties);

        httpClient.executeMethod(get);
        assertEquals(401, get.getStatusCode());

        httpClient.getState().setCredentials(
            AuthScope.ANY,
            new UsernamePasswordCredentials("admin", "admin")
        );
        get.setDoAuthentication(true);
        httpClient.executeMethod(get);
        assertEquals(200, get.getStatusCode());
    }

}
