package org.apache.maven.scm.provider.svn.svnjava.command.diff;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.diff.AbstractDiffCommand;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.command.diff.SvnDiffConsumer;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.util.ScmFileEventHandler;
import org.apache.maven.scm.provider.svn.svnjava.util.SvnJavaUtil;
import org.apache.commons.lang.StringUtils;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id: SvnJavaDiffCommand.java 3 2009-03-20 21:49:32Z oliver.lamy $
 */
public class SvnJavaDiffCommand
    extends AbstractDiffCommand
    implements SvnCommand
{
    /**
     * {@inheritDoc}
     */
    protected DiffScmResult executeDiffCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                ScmVersion startRevision, ScmVersion endRevision )
        throws ScmException
    {
        if ( logger.isInfoEnabled() )
        {
        	logger.info( "SVN diff directory: " + fileSet.getBasedir().getAbsolutePath() );
        }

        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) repo;

        SVNRevision start =
                ( startRevision == null ) ? SVNRevision.COMMITTED : SVNRevision.parse( startRevision.getName() );
        SVNRevision end =
                ( endRevision == null ) ? SVNRevision.WORKING : SVNRevision.parse( endRevision.getName() );

        List<String> changeLists = new ArrayList<>();

        ScmFileEventHandler handler = new ScmFileEventHandler( logger, fileSet.getBasedir() );

        SVNDiffClient diffClient = javaRepo.getClientManager().getDiffClient();
        diffClient.setEventHandler( handler );
        
        try (ByteArrayOutputStream out =
                     SvnJavaUtil.diff(diffClient, fileSet.getBasedir(), start, end, SVNDepth.INFINITY, changeLists);
             ByteArrayInputStream bis = new ByteArrayInputStream( out.toByteArray() );
             BufferedReader in = new BufferedReader( new InputStreamReader( bis ) ))
        {


            SvnDiffConsumer consumer = new SvnDiffConsumer( fileSet.getBasedir() );

            String line = in.readLine();
            while ( line != null )
            {
                consumer.consumeLine( line );

                line = in.readLine();
            }

            String userdir = System.getProperty("basedir", System.getProperty("user.dir"));

            if (StringUtils.isNotEmpty(userdir)) {
                String userDirAbs = new File(userdir).getAbsolutePath();
                String baseDirAbs = fileSet.getBasedir().getAbsolutePath();
                // the diff is executed from the current directory so it include the path to fileSet.getBasedir()
                List<ScmFile> changedFiles = consumer.getChangedFiles().stream().map(scmFile -> {
                    String fullPathAbs = Paths.get(userDirAbs, scmFile.getPath()).toFile().getAbsolutePath();
                    String fileRelative = StringUtils.removeStart(fullPathAbs, baseDirAbs);
                    fileRelative = StringUtils.startsWith(fileRelative, "/") ?
                            StringUtils.removeStart(fileRelative,"/"):fileRelative;
                    // update differences as well
                    CharSequence charSequence = consumer.getDifferences().get(scmFile.getPath());
                    consumer.getDifferences().put(fileRelative, charSequence);
                    consumer.getDifferences().remove(scmFile.getPath());

                    return new ScmFile(fileRelative, scmFile.getStatus());

                }).collect(Collectors.toList());
                return new DiffScmResult( SvnJavaScmProvider.COMMAND_LINE, changedFiles,
                        consumer.getDifferences(), consumer.getPatch() );
            }

            return new DiffScmResult( SvnJavaScmProvider.COMMAND_LINE, consumer.getChangedFiles(),
                                      consumer.getDifferences(), consumer.getPatch() );
        }
        catch ( IOException | SVNException e )
        {
            return new DiffScmResult( SvnJavaScmProvider.COMMAND_LINE, "SVN diff failed.", e.getMessage(), false );
        }
    }
}
