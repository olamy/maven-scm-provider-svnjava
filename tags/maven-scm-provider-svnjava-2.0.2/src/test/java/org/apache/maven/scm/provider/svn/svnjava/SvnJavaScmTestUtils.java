package org.apache.maven.scm.provider.svn.svnjava;

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
import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: SvnJavaScmTestUtils.java 405 2010-01-29 22:38:22Z oliver.lamy@gmail.com $
 */
public final class SvnJavaScmTestUtils
{


    private SvnJavaScmTestUtils()
    {
        // no op
    }

    public static void initializeRepository( File repositoryRoot )
        throws Exception
    {
        if ( repositoryRoot.exists() )
        {
            FileUtils.deleteDirectory( repositoryRoot );
        }

        Assert.assertTrue( "Could not make repository root directory: " + repositoryRoot.getAbsolutePath(),
                           repositoryRoot.mkdirs() );

        //ScmTestCase.execute( repositoryRoot.getParentFile(), SVNADMIN_COMMAND_LINE, "create " + repositoryRoot.getName() );

        if (repositoryRoot.exists())
        {
            FileUtils.cleanDirectory( repositoryRoot );
        }
        
        ISVNOptions options = SVNWCUtil.createDefaultOptions( true );

        SVNClientManager.newInstance().getAdminClient().doCreateRepository( repositoryRoot, null, true, true );
        
        loadSvnDump( repositoryRoot,
                     new SvnJavaScmTestUtils().getClass().getClassLoader().getResourceAsStream( "tck/tck.dump" ) );
    }

    public static void initializeRepository( File repositoryRoot, File dump )
        throws Exception
    {
        if ( repositoryRoot.exists() )
        {
            FileUtils.deleteDirectory( repositoryRoot );
        }

        Assert.assertTrue( "Could not make repository root directory: " + repositoryRoot.getAbsolutePath(),
                           repositoryRoot.mkdirs() );

        //ScmTestCase.execute( repositoryRoot.getParentFile(), SVNADMIN_COMMAND_LINE, "create " + repositoryRoot.getName() );

        SVNAdminClient client = getSVNAdminClient();
        
        client.doCreateRepository( repositoryRoot, null, true, true );
        
        
        Assert.assertTrue( "The dump file doesn't exist: " + dump.getAbsolutePath(), dump.exists() );

        loadSvnDump( repositoryRoot, new FileInputStream( dump ) );
    }
    
    public static SVNAdminClient getSVNAdminClient()
    {
        SVNAdminClient client = SVNClientManager.newInstance().getAdminClient();
         
        //client.setEventHandler( handler );
        return client;
    }

    private static void loadSvnDump( File repositoryRoot, InputStream dumpStream )
        throws Exception
    {
        
        SVNClientManager.newInstance().getAdminClient().doLoad( repositoryRoot, dumpStream );
        
    }

    public static String getScmUrl( File repositoryRootFile )
        throws CommandLineException
    {
        String repositoryRoot = repositoryRootFile.getAbsolutePath();

        // TODO: it'd be great to build this into CommandLineUtils somehow
        // TODO: some way without a custom cygwin sys property?
        if ( "true".equals( System.getProperty( "cygwin" ) ) )
        {
            Commandline cl = new Commandline();

            cl.setExecutable( "cygpath" );

            cl.createArg().setValue( "--unix" );

            cl.createArg().setValue( repositoryRoot );

            CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
            
            int exitValue = CommandLineUtils.executeCommandLine( cl, stdout, null );

            if ( exitValue != 0 )
            {
                throw new CommandLineException( "Unable to convert cygwin path, exit code = " + exitValue );
            }

            repositoryRoot = stdout.getOutput().trim();
        }
        else if ( Os.isFamily( "windows" ) )
        {
            repositoryRoot = "/" + StringUtils.replace( repositoryRoot, "\\", "/" );
        }

        return "scm:javasvn:file://" + repositoryRoot;
    }
}
