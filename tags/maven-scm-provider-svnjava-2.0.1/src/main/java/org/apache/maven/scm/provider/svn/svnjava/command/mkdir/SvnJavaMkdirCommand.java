package org.apache.maven.scm.provider.svn.svnjava.command.mkdir;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.mkdir.AbstractMkdirCommand;
import org.apache.maven.scm.command.mkdir.MkdirScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.util.SvnJavaUtil;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @version $Id: SvnJavaMkdirCommand.java 490 2011-01-09 14:16:20Z oliver.lamy $
 * @since 1.11
 */
public class SvnJavaMkdirCommand
    extends AbstractMkdirCommand
    implements SvnCommand
{

    /**
     * @see org.apache.maven.scm.command.mkdir.AbstractMkdirCommand#executeMkdirCommand(org.apache.maven.scm.provider.ScmProviderRepository, org.apache.maven.scm.ScmFileSet, java.lang.String)
     */
    @Override
    protected MkdirScmResult executeMkdirCommand( ScmProviderRepository repository, ScmFileSet fileSet, String message,
                                                  boolean createInLocal )
        throws ScmException
    {
        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) repository;
        Iterator<File> it = fileSet.getFileList().iterator();
        String dirPath = ( (File) it.next() ).getPath();
        // replacing \ with / for windauze
        if ( dirPath != null && Os.isFamily( Os.FAMILY_DOS ) )
        {
            dirPath = StringUtils.replace( dirPath, "\\", "/" );
        }
        String url = javaRepo.getUrl() + "/" + dirPath;
        if ( createInLocal )
        {
            url = dirPath;
        }
        List<SVNURL> svnurls = new ArrayList<SVNURL>( 1 );
        try
        {
            svnurls.add( SVNURL.parseURIEncoded( url ) );

            SVNCommitInfo commitInfo =
                SvnJavaUtil.mkdir( javaRepo.getClientManager(), svnurls.toArray( new SVNURL[svnurls.size()] ),
                                   message );
            ScmResult scmResult = new ScmResult( null, null, null, true );

            return new MkdirScmResult( Long.toString( commitInfo.getNewRevision() ), scmResult );
        }
        catch ( SVNException e )
        {
            throw new ScmException( e.getMessage(), e );
        }
    }

}
