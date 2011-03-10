package org.apache.maven.scm.provider.svn.svnjava.command.info;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.command.info.SvnInfoItem;
import org.apache.maven.scm.provider.svn.command.info.SvnInfoScmResult;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 */
public class SvnJavaInfoCommand
    extends AbstractCommand
    implements SvnCommand
{
    protected ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                        CommandParameters parameters )
        throws ScmException
    {
        return executeInfoCommand( repository, fileSet, parameters, false, "" );
    }

    public SvnInfoScmResult executeInfoCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                CommandParameters parameters, boolean recursive, String revision )
        throws ScmException
    {
        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) repository;
        List infoItems = new ArrayList();
        SvnInfoScmResult svnInfoScmResult = new SvnInfoScmResult( null, infoItems );

        Iterator i = fileSet.getFileList().iterator();
        if ( i.hasNext() )
        {
            while ( i.hasNext() )
            {
                SvnInfoItem currentItem = executeSingleInfoCommand( javaRepo, (File) ( i.next() ), revision );
                infoItems.add( currentItem );
            }
        }
        else
        {
            // no files just a base directory
            SvnInfoItem currentItem = executeSingleInfoCommand( javaRepo, fileSet.getBasedir(), revision );
            infoItems.add( currentItem );
        }
        return svnInfoScmResult;
    }

    private SvnInfoItem executeSingleInfoCommand( SvnJavaScmProviderRepository javaRepo, File f, String revision )
        throws ScmException
    {
        try
        {
            SVNRevision svnRev = null;
            if ( revision != null )
            {
                svnRev = SVNRevision.parse( revision );
            }

            SVNInfo svnInfo = javaRepo.getClientManager().getWCClient().doInfo( f, svnRev );

            SvnInfoItem currentItem = new SvnInfoItem();

            currentItem.setRevision( svnInfo.getRevision() != null ? Long.toString( svnInfo.getRevision().getNumber() )
                                                                  : null );
            currentItem.setLastChangedAuthor( svnInfo.getAuthor() );
            currentItem.setLastChangedRevision( svnInfo.getCommittedRevision() != null ? Long.toString( svnInfo
                .getCommittedRevision().getNumber() ) : null );
            currentItem.setLastChangedDate( svnInfo.getCommittedDate() != null ? svnInfo.getCommittedDate().toString()
                                                                              : null );
            currentItem.setURL( svnInfo.getURL() != null ? svnInfo.getURL().toString() : null );
            currentItem.setRepositoryUUID( svnInfo.getRepositoryUUID() );
            currentItem.setRepositoryRoot( svnInfo.getRepositoryRootURL() != null ? svnInfo.getRepositoryRootURL()
                .toString() : null );
            currentItem.setNodeKind( svnInfo.getKind() != null ? svnInfo.getKind().toString() : null );
            return currentItem;
        }
        catch ( SVNException e )
        {
            throw new ScmException( e.getMessage(), e );
        }
    }
}
