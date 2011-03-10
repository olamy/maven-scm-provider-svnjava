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
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SvnJavaCheckInCommand
    extends AbstractCheckInCommand
    implements SvnCommand
{
    /** {@inheritDoc} */
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                                      ScmVersion tag )
        throws ScmException
    {
        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "SVN commit directory: " + fileSet.getBasedir().getAbsolutePath() );
        }

        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) repo;

        CommitHandler handler = new CommitHandler();

        SVNCommitClient svnCommitClient = javaRepo.getClientManager().getCommitClient();

        svnCommitClient.setCommitHandler( handler );

        try
        {
            File[] tmpPaths = fileSet.getFiles();
            File[] paths;
            if ( tmpPaths == null || tmpPaths.length == 0 )
            {
                paths = new File[] { fileSet.getBasedir() };
            }
            else
            {
                paths = new File[tmpPaths.length];
                for ( int i = 0; i < tmpPaths.length; i++ )
                {
                    if ( tmpPaths[i].isAbsolute() )
                    {
                        paths[i] = tmpPaths[i];
                    }
                    else
                    {
                        paths[i] = new File( fileSet.getBasedir(), tmpPaths[i].toString() );
                    }
                }
            }

            SVNCommitInfo svnCommitInfo = SvnJavaUtil.commit( svnCommitClient, paths, false, message, true );

            List files = new ArrayList();
            for ( Iterator iter = handler.getFiles().iterator(); iter.hasNext(); )
            {
                String filePath = (String) iter.next();
                files.add( new ScmFile( filePath, ScmFileStatus.CHECKED_IN ) );

            }

            return new CheckInScmResult( SvnJavaScmProvider.COMMAND_LINE, files, Long.toString( svnCommitInfo
                .getNewRevision() ) );
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
        private List files = new ArrayList();

        public CommitHandler()
        {
            // no op
        }

        public String getCommitMessage( String message, SVNCommitItem[] commitItems )
            throws SVNException
        {
            if ( commitItems != null )
            {
                for ( int i = 0, size = commitItems.length; i < size; i++ )
                {
                    SVNCommitItem commitItem = commitItems[i];
                    if (commitItem.getFile().isFile())
                    {
                        files.add( commitItem.getPath() );
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

        public List getFiles()
        {
            return files;
        }

    }
}
