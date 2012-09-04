package org.apache.maven.scm.provider.svn.svnjava.command.remoteinfo;
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

import org.apache.maven.scm.command.remoteinfo.RemoteInfoScmResult;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.codehaus.plexus.PlexusTestCase;
import org.tmatesoft.svn.core.SVNURL;

/**
 * @author Olivier Lamy
 */
public class SvnJavaRemoteInfoCommandTest
    extends PlexusTestCase
{
    public void testRemoteInfo()
        throws Exception
    {
        SvnJavaRemoteInfoCommand c = new SvnJavaRemoteInfoCommand();
        SvnJavaScmProviderRepository svnScmProviderRepository = new SvnJavaScmProviderRepository(
            SVNURL.parseURIEncoded( "http://svn.apache.org/repos/asf/maven/scm/trunk" ),
            "http://svn.apache.org/repos/asf/maven/scm/trunk" );
        RemoteInfoScmResult r = c.executeRemoteInfoCommand( svnScmProviderRepository, null, null );
        System.out.println( "r:"+ r.toString() );
        assertTrue( r.getTags().containsKey( "maven-scm-1.7" ) );

    }
}
