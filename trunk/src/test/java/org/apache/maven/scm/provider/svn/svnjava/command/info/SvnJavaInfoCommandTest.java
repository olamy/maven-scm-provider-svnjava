package org.apache.maven.scm.provider.svn.svnjava.command.info;

import java.io.File;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.info.InfoItem;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.codehaus.plexus.PlexusTestCase;
import org.tmatesoft.svn.core.SVNURL;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 */
public class SvnJavaInfoCommandTest
    extends PlexusTestCase
{
    public void testInfo()
        throws Exception
    {
        ScmManager scmManager = (ScmManager) lookup( ScmManager.ROLE );
        String url = System.getProperty( "scmUrlProject" );
        String scmUrl = "scm:javasvn:" + url;
        SvnJavaScmProviderRepository repository = new SvnJavaScmProviderRepository( SVNURL.parseURIEncoded( url ),
                                                                                    scmUrl );

        SvnJavaScmProvider provider = (SvnJavaScmProvider) scmManager.getProviderByUrl( scmUrl );

        InfoScmResult result = provider.info( repository, new ScmFileSet( new File( getBasedir() ) ), null );
        InfoItem item = result.getInfoItems().get( 0 );
        assertTrue( item.getRevision() != null );

        SvnJavaInfoCommand command = new SvnJavaInfoCommand();
        result = command.executeInfoCommand( repository, new ScmFileSet( new File( getBasedir() ) ), null, true, null );
        item = result.getInfoItems().get( 0 );
        assertTrue( item.getRevision() != null );
        System.out.println( item.getRevision() );
    }

    public void testInfoLocale()
        throws Exception
    {
        ScmManager scmManager = (ScmManager) lookup( ScmManager.ROLE );
        String url = getBasedir();
        String scmUrl = "scm:javasvn:" + url;
        SvnJavaScmProviderRepository repository = new SvnJavaScmProviderRepository( SVNURL
            .fromFile( new File( getBasedir() ) ), scmUrl );

        SvnJavaScmProvider provider = (SvnJavaScmProvider) scmManager.getProviderByUrl( scmUrl );

        InfoScmResult result = provider.info( repository, new ScmFileSet( new File( getBasedir() ) ), null );
        InfoItem item = result.getInfoItems().get( 0 );
        assertTrue( item.getRevision() != null );

        SvnJavaInfoCommand command = new SvnJavaInfoCommand();
        result = command.executeInfoCommand( repository, new ScmFileSet( new File( getBasedir() ) ), null, true, null );
        item = result.getInfoItems().get( 0 );
        assertTrue( item.getRevision() != null );
        System.out.println( item.getRevision() );
    }    
}
