package org.apache.maven.scm.provider.svn.svnjava.command.info;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.info.InfoItem;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.codehaus.plexus.PlexusTestCase;
import org.junit.Before;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNURL;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    private Path checkoutPath = Paths.get( getBasedir(), "/target/", getClass().getName() );

    public void prepareCopy()
        throws Exception
    {

        if (checkoutPath.toFile().exists()){
            return;
        }

        ScmManager scmManager = (ScmManager) lookup( ScmManager.ROLE );
        String url = System.getProperty( "svnUrl" );
        String scmUrl = "scm:javasvn:" + url;

        SvnJavaScmProvider provider = (SvnJavaScmProvider) scmManager.getProviderByUrl( scmUrl );

        provider.checkOut( scmManager.makeScmRepository( scmUrl ), //
                           new ScmFileSet( checkoutPath.toFile() ) );
    }

    @Test
    public void testInfo()
        throws Exception
    {
        prepareCopy();
        ScmManager scmManager = (ScmManager) lookup( ScmManager.ROLE );

        String url = System.getProperty( "scmUrlProject" );
        String scmUrl = "scm:javasvn:" + url;
        SvnJavaScmProviderRepository repository =
            new SvnJavaScmProviderRepository( SVNURL.parseURIEncoded( url ), scmUrl );

        SvnJavaScmProvider provider = (SvnJavaScmProvider) scmManager.getProviderByUrl( scmUrl );

        InfoScmResult result = provider.info( repository, new ScmFileSet( checkoutPath.toFile() ), null );
        InfoItem item = result.getInfoItems().get( 0 );
        assertTrue( item.getRevision() != null );

        SvnJavaInfoCommand command = new SvnJavaInfoCommand();
        result = command.executeInfoCommand( repository, new ScmFileSet( checkoutPath.toFile() ), null, true, null );
        item = result.getInfoItems().get( 0 );
        assertTrue( item.getRevision() != null );
        System.out.println( item.getRevision() );
    }

    public void testInfoLocale()
        throws Exception
    {

        prepareCopy();
        ScmManager scmManager = (ScmManager) lookup( ScmManager.ROLE );
        String url = getBasedir();
        String scmUrl = "scm:javasvn:" + url;
        SvnJavaScmProviderRepository repository =
            new SvnJavaScmProviderRepository( SVNURL.fromFile( checkoutPath.toFile() ), scmUrl );

        SvnJavaScmProvider provider = (SvnJavaScmProvider) scmManager.getProviderByUrl( scmUrl );

        InfoScmResult result = provider.info( repository, new ScmFileSet( checkoutPath.toFile() ), null );
        InfoItem item = result.getInfoItems().get( 0 );
        assertTrue( item.getRevision() != null );

        SvnJavaInfoCommand command = new SvnJavaInfoCommand();
        result = command.executeInfoCommand( repository, new ScmFileSet( checkoutPath.toFile() ), null, true, null );
        item = result.getInfoItems().get( 0 );
        assertTrue( item.getRevision() != null );
        System.out.println( item.getRevision() );
    }
}
