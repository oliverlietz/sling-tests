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
package org.apache.sling.tests.sling_2917;

import javax.inject.Inject;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.launchpad.karaf.testing.KarafTestSupport;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class SLING_2917_IT extends KarafTestSupport {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Inject
    @Filter(timeout = 240000)
    protected SlingRepository slingRepository;

    @Configuration
    public Option[] configuration() {
        return OptionUtils.combine(baseConfiguration(),
            addBootFeature("sling-launchpad-jackrabbit"),
            addBootFeature("sling-launchpad-content")
        );
    }

    @Test
    public void testNodeROOTDoesNotExist() throws Exception {
        Session session = slingRepository.loginAdministrative(null);
        exception.expect(PathNotFoundException.class);
        session.getRootNode().getNode("ROOT");
    }

}
