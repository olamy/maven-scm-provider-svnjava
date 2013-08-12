package org.apache.maven.scm.provider.svn.svnjava.command.list;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.list.AbstractListCommand;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.SvnCommandUtils;
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Olivier Lamy
 * @since 2.0.6
 */
public class SvnJavaListCommand
    extends AbstractListCommand
    implements SvnCommand
{

    @Override
    public ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        boolean recursive = parameters == null ? false : parameters.getBoolean( CommandParameter.RECURSIVE );
        ScmVersion scmVersion =
            parameters == null ? null : parameters.getScmVersion( CommandParameter.SCM_VERSION, null );
        return executeListCommand( repository, fileSet, recursive, scmVersion );
    }

    @Override
    protected ListScmResult executeListCommand( ScmProviderRepository scmProviderRepository, ScmFileSet scmFileSet,
                                                boolean recursive, ScmVersion version )
        throws ScmException
    {
        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) scmProviderRepository;

        String url = javaRepo.getUrl();

        SVNRevision revision = SVNRevision.HEAD;

        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
            if ( version instanceof ScmTag )
            {
                url = SvnTagBranchUtils.resolveTagUrl( javaRepo, (ScmTag) version );
            }
            else if ( version instanceof ScmBranch )
            {
                url = SvnTagBranchUtils.resolveBranchUrl( javaRepo, (ScmBranch) version );
            }
            else if ( version instanceof ScmRevision )
            {
                try
                {
                    revision = SVNRevision.create( Long.parseLong( ( (ScmRevision) version ).getName() ) );
                }
                catch ( NumberFormatException exc )
                {
                    return new ListScmResult( SvnJavaScmProvider.COMMAND_LINE,
                                              "SVN checkout failed. Wrong format of revision number.", null, false );
                }
            }
        }

        if ( url != null )
        {
            url = SvnCommandUtils.fixUrl( url, javaRepo.getUser() );
        }
        ListEntryHandler listEntryHandler = new ListEntryHandler();

        /*
        SVNURL url,
        SVNRevision pegRevision,
        SVNRevision revision,
        boolean fetchLocks,
        SVNDepth depth,
        int entryFields,
        ISVNDirEntryHandler handler
        */

        try
        {
            javaRepo.getClientManager().getLogClient().doList(
                url == null ? javaRepo.getSvnUrl() : SVNURL.parseURIEncoded( url ), revision, revision, true,
                // boolean fetchLocks,
                SVNDepth.IMMEDIATES, 0, listEntryHandler );
        }
        catch ( SVNException e )
        {
            throw new ScmException( "Error while executing svn list.", e );
        }

        List<ScmFile> scmFiles = new ArrayList<ScmFile>( listEntryHandler.relativePaths.size() );

        for ( String path : listEntryHandler.relativePaths )
        {
            scmFiles.add( new ScmFile( path, ScmFileStatus.CHECKED_IN ) );
        }

        ListScmResult listScmResult = new ListScmResult( scmFiles, new ScmResult( null, null, null, false ) );
        return listScmResult;

    }

    private static class ListEntryHandler
        implements ISVNDirEntryHandler
    {
        private List<String> relativePaths = new ArrayList<String>();

        public void handleDirEntry( SVNDirEntry svnDirEntry )
            throws SVNException
        {
            if ( StringUtils.isNotEmpty( svnDirEntry.getRelativePath() ) )
            {
                relativePaths.add( svnDirEntry.getRelativePath() );
            }
        }
    }
}
