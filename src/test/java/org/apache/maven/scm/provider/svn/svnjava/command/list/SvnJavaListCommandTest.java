package org.apache.maven.scm.provider.svn.svnjava.command.list;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.junit.Test;
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
public class SvnJavaListCommandTest
	  extends ScmTestCase
{

    @Test
    public void testList()
        throws Exception
    {
        String url = System.getProperty( "svnUrl" );
        String scmUrl = "scm:javasvn:" + url;
        SvnJavaScmProviderRepository repository =
            new SvnJavaScmProviderRepository( SVNURL.parseURIEncoded( url ), scmUrl );

        SvnJavaScmProvider provider = (SvnJavaScmProvider) getScmManager().getProviderByUrl( scmUrl );

        ScmFileSet fileSet = new ScmFileSet( new File( "." ), new File( "." ) );

        ListScmResult listScmResult = provider.list( repository, fileSet, null );

        System.out.println( listScmResult.getFiles() );

        boolean containsPom = false;

        for ( ScmFile scmFile : listScmResult.getFiles() )
        {
            if ( StringUtils.equals( scmFile.getPath(), "pom.xml" ) )
            {
                containsPom = true;
            }
        }

        assertTrue( "pom.xml not in list result", containsPom );
    }

}
