package org.apache.maven.scm.provider.svn.svnjava.command.add;

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

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.util.SvnJavaUtil;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author <a href="mailto:dh-maven@famhq.com">David Hawkins</a>
 * @version $Id: SvnJavaAddCommand.java 481 2010-12-31 15:22:23Z oliver.lamy $
 */
public class SvnJavaAddCommand
    extends AbstractAddCommand
    implements SvnCommand
{

    @Override
    protected ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                        CommandParameters parameters )
        throws ScmException
    {
        if ( fileSet.getFileList().isEmpty() )
        {
            throw new ScmException( "You must provide at least one file/directory to add" );
        }

        final List<ScmFile> filesAdded = new ArrayList<>();

        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) repository;

        SVNClientManager clientManager = javaRepo.getClientManager();

        try
        {
            clientManager.getWCClient().setEventHandler( new ISVNEventHandler()
            {

                public void handleEvent( SVNEvent event, double progress )
                {
                    if ( event.getAction() == SVNEventAction.ADD )
                    {
                        filesAdded.add( new ScmFile( event.getFile().getPath(), ScmFileStatus.ADDED ) );
                    }
                }

                public void checkCancelled()
                    throws SVNCancelException
                {
                    // null
                }
            } );

            for ( File file : fileSet.getFileList() )
            {
                File fileToAdd = new File( fileSet.getBasedir(), file.toString() );

                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "SVN adding file: " + fileToAdd.getAbsolutePath() );
                }

                boolean forceAdd = parameters.getBoolean( CommandParameter.FORCE_ADD, false );
                SvnJavaUtil.add( clientManager, fileToAdd, false, forceAdd );
            }
        }
        catch ( SVNException e )
        {
            return new AddScmResult( SvnJavaScmProvider.COMMAND_LINE, "The svn operation failed.", e.getMessage(),
                                     false );
        }
        finally
        {
            clientManager.getWCClient().setEventHandler( null );
        }

        return new AddScmResult( null, filesAdded );
    }

    /**
     * {@inheritDoc}
     */
    protected ScmResult executeAddCommand( ScmProviderRepository repository, ScmFileSet fileSet, String message,
                                           boolean binary )
        throws ScmException
    {
        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.MESSAGE, message );
        return executeCommand( repository, fileSet, commandParameters );
    }
}
