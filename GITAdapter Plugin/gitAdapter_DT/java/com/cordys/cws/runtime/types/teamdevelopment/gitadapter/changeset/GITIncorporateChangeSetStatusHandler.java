package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.changeset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.internal.teamdevelopment.changesets.ISCMChangeSet;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.config.GITRemoteRepositoryConfiguration;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.handler.HandlerBase;
import com.cordys.cws.util.StudioLogger;

public class GITIncorporateChangeSetStatusHandler extends HandlerBase
{

	private FetchResult fetchResult;

	public GITIncorporateChangeSetStatusHandler(final GITClient gitClient)
	{
		super(gitClient);
	}

	@Override
	public void execute()
	{
		try
		{
			fetchResult = getGITWorkingRepo().fetch()
																			 .setRemote(GITRemoteRepositoryConfiguration.ORIGIN)
																			 .setRefSpecs(Constants.R_HEADS + m_gitClient.getRemoteRepositoryConfiguration().getBranch())
																			 .setCredentialsProvider(m_gitClient.getProvider())
																			 .call();
		}
		catch (GitAPIException exception)
		{
			throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
		}
	}

	public void determineIncomingChangesFromRepository(final ISCMChangeSet changeSet)
	{
		final File workingCopyLocation = m_gitClient.getWorkingCopyLocation();
		final List<DiffEntry> listOfAllCommits = fetchDiffFileEntries();
		listOfAllCommits.forEach(latestCommit -> {
			final String action = latestCommit.getChangeType().name();
			createChangesetFromCommitEntry(latestCommit, changeSet, workingCopyLocation, action);
		});
	}
	
	public List<DiffEntry> fetchDiffFileEntries()
	{
		final Repository gitWorkingRepo = getGITWorkingRepo().getRepository();
		// a RevWalk allows to walk over commits based on some filtering that is defined
		try (RevWalk workingRepoWalk = new RevWalk(gitWorkingRepo))
		{
			ObjectId youngestCommit = null;

			for (final Ref refUpdate : fetchResult.getAdvertisedRefs())
			{
				youngestCommit = refUpdate.getObjectId();
			}
			final ObjectId wcRepoHead = gitWorkingRepo.resolve(Constants.HEAD);

			if (youngestCommit != null && wcRepoHead != null && !youngestCommit.equals(wcRepoHead))
			{
				final RevCommit workingHead = workingRepoWalk.parseCommit(wcRepoHead);
				final DiffFormatter diffFormat = new DiffFormatter(DisabledOutputStream.INSTANCE);
				diffFormat.setRepository(gitWorkingRepo);
				diffFormat.setDiffComparator(RawTextComparator.DEFAULT);
				diffFormat.setDetectRenames(true);
				final List<DiffEntry> listOfAllCommits = diffFormat.scan(workingHead.getTree(), youngestCommit);

				if (diffFormat != null)
				{
					diffFormat.close();
				}
				return listOfAllCommits;
			}
		}
		catch (MissingObjectException | IncorrectObjectTypeException exception)
		{
			throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
		}
		catch (final IOException exception)
		{
			throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
		}
		
		return new ArrayList<DiffEntry>();

	}
}
