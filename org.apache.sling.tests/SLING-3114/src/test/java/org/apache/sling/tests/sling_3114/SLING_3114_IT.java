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
package org.apache.sling.tests.sling_3114;

import java.io.InputStream;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.contentloader.ContentImportListener;
import org.apache.sling.jcr.contentloader.ContentImporter;
import org.apache.sling.jcr.contentloader.ImportOptions;
import org.apache.sling.launchpad.karaf.testing.KarafTestSupport;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.util.Filter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

@RunWith(PaxExam.class)
public class SLING_3114_IT extends KarafTestSupport {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Inject
    @Filter(timeout = 240000)
    protected SlingRepository slingRepository;

    @Inject
    @Filter(timeout = 240000)
    protected ContentImporter contentImporter;

    @Configuration
    public Option[] configuration() {
        return OptionUtils.combine(baseConfiguration(),
            addBootFeature("sling-launchpad-jackrabbit"),
            mavenBundle().groupId("org.mockito").artifactId("mockito-all").version("1.9.5")
        );
    }

    @Test
    public void testImportSystemViewValidContent() throws Exception {
        final String name = "apps.jcr.xml";
        final InputStream inputStream = getClass().getResourceAsStream("valid-".concat(name));
        final Session session = slingRepository.loginAdministrative(null);
        final Node root = session.getRootNode();
        final ImportOptions importOptions = mock(ImportOptions.class);
        when(importOptions.isOverwrite()).thenReturn(true);
        final ContentImportListener contentImportListener = mock(ContentImportListener.class);
        contentImporter.importContent(root, name, inputStream, importOptions, contentImportListener);
        verify(contentImportListener).onCreate("/apps");
        final Node apps = root.getNode("apps");
        assertEquals("/apps", apps.getPath());
    }

    @Test
    public void testImportSystemViewWithInvalidContent() throws Exception {
        final String name = "apps.jcr.xml";
        final InputStream inputStream = getClass().getResourceAsStream("invalid-".concat(name));
        final Session session = slingRepository.loginAdministrative(null);
        final Node root = session.getRootNode();
        final ImportOptions importOptions = mock(ImportOptions.class);
        when(importOptions.isOverwrite()).thenReturn(true);
        final ContentImportListener contentImportListener = mock(ContentImportListener.class);
        contentImporter.importContent(root, name, inputStream, importOptions, contentImportListener);
        verify(contentImportListener, never()).onCreate("/apps");
        exception.expect(PathNotFoundException.class);
        root.getNode("apps");
    }

}
