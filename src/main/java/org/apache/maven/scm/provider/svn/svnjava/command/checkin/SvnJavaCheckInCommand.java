package org.apache.maven.scm.provider.svn.svnjava.command.checkin;

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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.util.SvnJavaUtil;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.wc.ISVNCommitHandler;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNCommitItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author Olivier Lamy
 * @version $Id: SvnJavaCheckInCommand.java 486 2011-01-02 18:40:36Z oliver.lamy $
 */
public class SvnJavaCheckInCommand
    extends AbstractCheckInCommand
    implements SvnCommand
{
    /**
     * {@inheritDoc}
     */
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                                      ScmVersion tag )
        throws ScmException
    {
        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "SVN commit directory: " + fileSet.getBasedir().getAbsolutePath() );
        }

        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) repo;

        CommitHandler handler = new CommitHandler( fileSet.getBasedir().getAbsolutePath() );

        SVNCommitClient svnCommitClient = javaRepo.getClientManager().getCommitClient();

        svnCommitClient.setCommitHandler( handler );

        try
        {
            List<File> tmpPaths = fileSet.getFileList();
            List<File> paths;
            if ( tmpPaths == null || tmpPaths.isEmpty() )
            {
                paths = new ArrayList<>( 1 );
                paths.add( fileSet.getBasedir() );
            }
            else
            {
                paths = new ArrayList<>( tmpPaths.size() );
                for ( File f : tmpPaths )
                {
                    if ( f.isAbsolute() )
                    {
                        paths.add( f );
                    }
                    else
                    {
                        paths.add( new File( fileSet.getBasedir(), f.toString() ) );
                    }
                }
            }

            SVNCommitInfo svnCommitInfo =
                SvnJavaUtil.commit( svnCommitClient, paths.toArray(new File[0]), false, message, true );

            List<ScmFile> files = new ArrayList<>();
            for ( String filePath : handler.getFiles() )
            {
                files.add( new ScmFile( filePath, ScmFileStatus.CHECKED_IN ) );
            }

            return new CheckInScmResult( SvnJavaScmProvider.COMMAND_LINE, files,
                                         Long.toString( svnCommitInfo.getNewRevision() ) );
        }
        catch ( SVNException e )
        {
            return new CheckInScmResult( SvnJavaScmProvider.COMMAND_LINE, "SVN commit failed.", e.getMessage(), false );
        }
        finally
        {
            javaRepo.getClientManager().getCommitClient().setEventHandler( null );
        }
    }

    public static class CommitHandler
        implements ISVNCommitHandler
    {
        private List<String> files = new ArrayList<>();

        private String baseDirectory;

        public CommitHandler( String baseDirectory )
        {
            this.baseDirectory = FilenameUtils.separatorsToUnix( baseDirectory );
            if ( !StringUtils.endsWith( this.baseDirectory, "/" ) )
            {
                this.baseDirectory += "/";
            }
        }

        public String getCommitMessage( String message, SVNCommitItem[] commitItems )
            throws SVNException
        {
            if ( commitItems != null )
            {

                {
                    for ( SVNCommitItem commitItem : commitItems )
                    {
                        if ( commitItem.getFile().isFile() )
                        {
                            String path =
                                StringUtils.removeStartIgnoreCase( commitItem.getFile().getPath(), baseDirectory );

                            files.add( path );
                        }
                    }
                }
            }
            return message;
        }

        public SVNProperties getRevisionProperties( String arg0, SVNCommitItem[] arg1, SVNProperties svnProperties )
            throws SVNException
        {
            return svnProperties;
        }

        public List<String> getFiles()
        {
            return files;
        }

    }
}
