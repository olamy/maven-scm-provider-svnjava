package org.apache.maven.scm.provider.svn.svnjava.command.checkout;

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

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.SvnCommandUtils;
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.util.ScmFileEventHandler;
import org.apache.maven.scm.provider.svn.svnjava.util.SvnJavaUtil;
import org.codehaus.plexus.util.StringUtils;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 * @version $Id: SvnJavaCheckOutCommand.java 489 2011-01-05 20:55:21Z oliver.lamy $
 */
public class SvnJavaCheckOutCommand
    extends AbstractCheckOutCommand
    implements SvnCommand
{

    @Override
    protected CheckOutScmResult executeCheckOutCommand(ScmProviderRepository scmProviderRepository, ScmFileSet scmFileSet,
                                                       ScmVersion scmVersion, boolean recursive, boolean shallow) throws ScmException {
        return executeCheckOutCommand(scmProviderRepository, scmFileSet, scmVersion, recursive);
    }

    /**
     * {@inheritDoc}
     */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                        ScmVersion version, boolean recursive )
        throws ScmException
    {
        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "SVN checkout directory: " + fileSet.getBasedir().getAbsolutePath() );
        }

        SvnScmProviderRepository repository = (SvnScmProviderRepository) repo;

        String url = repository.getUrl();

        SVNRevision revision = SVNRevision.HEAD;

        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
            if ( version instanceof ScmTag )
            {
                url = SvnTagBranchUtils.resolveTagUrl( repository, (ScmTag) version );
            }
            else if ( version instanceof ScmBranch )
            {
                url = SvnTagBranchUtils.resolveBranchUrl( repository, (ScmBranch) version );
            }
            else if ( version instanceof ScmRevision )
            {
                try
                {
                    revision = SVNRevision.create( Long.parseLong( (version ).getName() ) );
                }
                catch ( NumberFormatException exc )
                {
                    return new CheckOutScmResult( SvnJavaScmProvider.COMMAND_LINE,
                                                  "SVN checkout failed. Wrong format of revision number.", null,
                                                  false );
                }
            }
        }

        url = SvnCommandUtils.fixUrl( url, repository.getUser() );

        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) repo;

        ScmFileEventHandler handler = new ScmFileEventHandler( getLogger(), fileSet.getBasedir() );
        SVNUpdateClient updateClient = javaRepo.getClientManager().getUpdateClient();
        updateClient.setEventHandler( handler );

        try
        {
            SvnJavaUtil.checkout( updateClient, SVNURL.parseURIEncoded( url ), revision, fileSet.getBasedir(), recursive );

            return new CheckOutScmResult( SvnJavaScmProvider.COMMAND_LINE, handler.getFiles() );
        }
        catch ( SVNException e )
        {
            return new CheckOutScmResult( SvnJavaScmProvider.COMMAND_LINE, "SVN checkout failed.", e.getMessage(),
                                          false );
        }
        finally
        {
            javaRepo.getClientManager().getUpdateClient().setEventHandler( null );
        }
    }

}
