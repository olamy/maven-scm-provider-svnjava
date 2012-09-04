package org.apache.maven.scm.provider.svn.svnjava.command.remoteinfo;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.lang.StringUtils;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.remoteinfo.AbstractRemoteInfoCommand;
import org.apache.maven.scm.command.remoteinfo.RemoteInfoScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Olivier Lamy
 */
public class SvnJavaRemoteInfoCommand
    extends AbstractRemoteInfoCommand
    implements SvnCommand
{
    @Override
    public RemoteInfoScmResult executeRemoteInfoCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                         CommandParameters parameters )
        throws ScmException
    {
        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) repository;

        String url = ( (SvnScmProviderRepository) repository ).getUrl();
        // use a default svn layout, url is here http://svn.apache.org/repos/asf/maven/maven-3/trunk
        // so as we presume we have good users using standard svn layout, we calculate tags and branches url
        String baseUrl = StringUtils.endsWith( url, "/" )
            ? StringUtils.substringAfter( StringUtils.removeEnd( url, "/" ), "/" )
            : StringUtils.substringBeforeLast( url, "/" );

        RemoteInfoScmResult remoteInfoScmResult = new RemoteInfoScmResult( null, null, null, true );

        try
        {

            DirEntryHandler dirEntryHandler = new DirEntryHandler( baseUrl );
            javaRepo.getClientManager().getLogClient().doList( SVNURL.parseURIEncoded( baseUrl + "/tags" ),
                                                               SVNRevision.HEAD, SVNRevision.HEAD, false, false,
                                                               dirEntryHandler );
            remoteInfoScmResult.setTags( dirEntryHandler.infos );
        }
        catch ( SVNException e )
        {
            return new RemoteInfoScmResult( null, e.getMessage(), null, false );
        }

        try
        {

            DirEntryHandler dirEntryHandler = new DirEntryHandler( baseUrl );
            javaRepo.getClientManager().getLogClient().doList( SVNURL.parseURIEncoded( baseUrl + "/branches" ),
                                                               SVNRevision.HEAD, SVNRevision.HEAD, false, false,
                                                               dirEntryHandler );
            remoteInfoScmResult.setBranches( dirEntryHandler.infos );
        }
        catch ( SVNException e )
        {
            return new RemoteInfoScmResult( null, e.getMessage(), null, false );
        }

        return remoteInfoScmResult;

    }

    public static class DirEntryHandler
        implements ISVNDirEntryHandler
    {
        String url;

        Map<String, String> infos = new HashMap<String, String>();

        DirEntryHandler( String url )
        {
            this.url = url;
        }

        public void handleDirEntry( SVNDirEntry svnDirEntry )
            throws SVNException
        {
            infos.put( svnDirEntry.getName(), svnDirEntry.getURL().toString() );
        }
    }
}
