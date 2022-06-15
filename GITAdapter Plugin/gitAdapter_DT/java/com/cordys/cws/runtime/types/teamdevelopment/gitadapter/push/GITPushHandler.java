package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.push;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.RemoteRefUpdate.Status;

import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.config.GITRemoteRepositoryConfiguration;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.handler.HandlerBase;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.reset.GITResetHandler;
import com.cordys.cws.util.StudioLogger;

public class GITPushHandler extends HandlerBase
{

	private static final String PULL_REJECTED_NONFASTFORWARD_STATUS = "REJECTED_NONFASTFORWARD";

	private static final String PULL_STATUS_OK = "OK";

	public GITPushHandler(final GITClient gitClient)
	{
		super(gitClient);
	}

	@Override
	public void execute()
	{
		Git git = getGITWorkingRepo();
		CredentialsProvider credentialsProvider = m_gitClient.getProvider();
		try
		{
			Iterable<PushResult> pushResults = git.push()
																						.setCredentialsProvider(credentialsProvider)
																						.setRemote(GITRemoteRepositoryConfiguration.ORIGIN)
																						.add(m_gitClient.getRemoteRepositoryConfiguration().getBranch())
																						.call();
			Iterator<PushResult> pushResultsItr = pushResults.iterator();
			while (pushResultsItr.hasNext())
			{
				PushResult pushResult = pushResultsItr.next();
				Collection<RemoteRefUpdate> remoteRefUpdates = pushResult.getRemoteUpdates();
				Iterator<RemoteRefUpdate> remoteRefUpdatesItr = remoteRefUpdates.iterator();
				while (remoteRefUpdatesItr.hasNext())
				{
					RemoteRefUpdate remoteRefUpdate = remoteRefUpdatesItr.next();
					Status status = remoteRefUpdate.getStatus();
					String statusName = status.name().toString();
					String remoteRefUpdateMessage = remoteRefUpdate.getMessage();
					// checking if the remote branch is ahead of the local branch
					if (PULL_REJECTED_NONFASTFORWARD_STATUS.equals(statusName))
					{
						resetCommit();
						throw StudioLogger.studioException(this, Messages.NEW_CHANGESET_DETECTED);
					}
					// checking if the pull operation is successful or not
					else
						if (!PULL_STATUS_OK.equals(statusName))
						{
							resetCommit();
							throw StudioLogger.studioException(this, Messages.ERROR, remoteRefUpdateMessage);
						}
				}
			}
		}
		catch (TransportException exception)
		{
			resetCommit();
			throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
		}
		catch (GitAPIException exception)
		{
			throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
		}
		
		
	}

	private void resetCommit()
	{
		new GITResetHandler(m_gitClient).execute();
	}
}
