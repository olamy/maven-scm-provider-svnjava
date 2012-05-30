package org.apache.maven.scm.provider.svn.svnjava.command.status;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.svn.command.status.SvnStatusCommandTckTest;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmTestUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;

/**
 * @author <a href="mailto:dh-maven@famhq.com">David Hawkins</a>
 * @version $Id: SvnJavaStatusCommandTckTest.java 98 2009-03-25 23:49:02Z oliver.lamy $
 */
public class SvnJavaStatusCommandTckTest
    extends SvnStatusCommandTckTest
{
    /**
     * {@inheritDoc}
     */
    public void initRepo()
        throws Exception
    {
        SvnJavaScmTestUtils.initializeRepository( getRepositoryRoot() );
    }

    /**
     * {@inheritDoc}
     */
    public String getScmUrl()
        throws Exception
    {
        return SvnJavaScmTestUtils.getScmUrl( new File( getRepositoryRoot(), "trunk" ) );
    }


    @Override
    public void testStatusCommand()
        throws Exception
    {
        super.testStatusCommand();

        ScmRepository repository = makeScmRepository( getScmUrl() );

        ScmManager scmManager = getScmManager();

        // ----------------------------------------------------------------------
        // Check status the project
        // src/main/java/org/Foo.java is added
        // /pom.xml is modified
        // check that readme and project.xml are not updated/created
        // ----------------------------------------------------------------------

        StatusScmResult result =
            scmManager.getProviderByUrl( getScmUrl() ).status( repository, new ScmFileSet( getUpdatingCopy() ) );

        //assertEquals(  );

    }

    public void assertPath( String expectedPath, String actualPath )
        throws Exception
    {
        assertEquals( StringUtils.replace( expectedPath, "\\", "/" ), StringUtils.replace( actualPath, "\\", "/" ) );
    }
}
