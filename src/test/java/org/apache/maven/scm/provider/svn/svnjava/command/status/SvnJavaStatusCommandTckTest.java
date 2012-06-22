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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.svn.command.status.SvnStatusCommandTckTest;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmTestUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

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


    /**
     *
     * @param expectedPath
     * @param actualPath
     * @throws Exception
     */
    public void assertPathRelativePath( String expectedPath, String actualPath )
        throws Exception
    {
        System.out.println( "expectedPath: " + expectedPath + ",actualPath:" + actualPath );
        assertEquals( StringUtils.replace( expectedPath, "\\", "/" ), StringUtils.replace( actualPath, "\\", "/" ) );
    }


    public void testStatusCommandNewFile()
        throws Exception
    {
        ScmRepository repository = makeScmRepository( getScmUrl() );

        checkOut( getUpdatingCopy(), repository );

        // ----------------------------------------------------------------------
        // Change the files
        // ----------------------------------------------------------------------

        /*
         * readme.txt is changed (changed file in the root directory)
         * project.xml is added (added file in the root directory)
         */

        // /readme.txt
        ScmTestCase.makeFile( getWorkingCopy(), "/readme.txt", "changed readme.txt" );

        // /project.xml
        ScmTestCase.makeFile( getWorkingCopy(), "/project.xml", "changed project.xml" );

        ScmTestCase.makeFile( getUpdatingCopy(), "/foo.xml", "new foo.xml" );

        //addToWorkingTree( getWorkingCopy(), new File( "project.xml" ), repository );

        //commit( getWorkingCopy(), repository );

        // /pom.xml
        ScmTestCase.makeFile( getUpdatingCopy(), "/pom.xml", "changed pom.xml" );

        // /src/test/java/org
        //ScmTestCase.makeDirectory( getUpdatingCopy(), "/src/test/java/org" );

        //addToWorkingTree( getUpdatingCopy(), new File( "src/test/java/org" ), repository );

        // /src/main/java/org/Foo.java
        //ScmTestCase.makeFile( getUpdatingCopy(), "/src/main/java/org/Foo.java" );

        //addToWorkingTree( getUpdatingCopy(), new File( "src/main/java/org" ), repository );

        // src/main/java/org/Foo.java
        //addToWorkingTree( getUpdatingCopy(), new File( "src/main/java/org/Foo.java" ), repository );

        ScmManager scmManager = getScmManager();

        // ----------------------------------------------------------------------
        // Check status the project
        // src/main/java/org/Foo.java is added
        // /pom.xml is modified
        // check that readme and project.xml are not updated/created
        // ----------------------------------------------------------------------

        StatusScmResult result =
            scmManager.getProviderByUrl( getScmUrl() ).status( repository, new ScmFileSet( getUpdatingCopy() ) );

        assertNotNull( "The command returned a null result.", result );

        assertResultIsSuccess( result );

        List<ScmFile> changedFiles = result.getChangedFiles();

        assertEquals( "Expected 2 files in the updated files list " + changedFiles, 2, changedFiles.size() );

        System.out.println( "changedFiles: " + changedFiles );

        // ----------------------------------------------------------------------
        // Assert the files in the updated files list
        // ----------------------------------------------------------------------

        Iterator<ScmFile> files = new TreeSet<ScmFile>( changedFiles ).iterator();

        ScmFile file = files.next();

        assertPathRelativePath( "pom.xml", file.getPath() );
        assertEquals( ScmFileStatus.MODIFIED, file.getStatus() );

        file = files.next();
        assertPathRelativePath( "foo.xml", file.getPath() );
        assertEquals( ScmFileStatus.UNKNOWN, file.getStatus() );

        assertFalse( "project.xml created incorrectly", new File( getUpdatingCopy(), "/project.xml" ).exists() );
    }

    /**
     * FIXME remove when upgrading to scm 1.8 as the method commit has been to protected
     */
    protected void doCommit( File workingDirectory, ScmRepository repository )
        throws Exception
    {
        CheckInScmResult result = getScmManager().checkIn( repository, new ScmFileSet( workingDirectory ), "No msg" );

        assertTrue( "Check result was successful, output: " + result.getCommandOutput(), result.isSuccess() );

        List<ScmFile> committedFiles = result.getCheckedInFiles();

        assertEquals( "Expected 2 files in the committed files list " + committedFiles, 2, committedFiles.size() );
    }
}
