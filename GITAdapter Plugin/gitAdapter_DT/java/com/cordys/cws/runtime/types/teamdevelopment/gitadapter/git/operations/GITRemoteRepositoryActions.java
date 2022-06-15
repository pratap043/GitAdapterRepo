package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.operations;

import java.util.Collection;

import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;

import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.config.GITRemoteRepositoryConfiguration;
import com.cordys.cws.util.StudioLogger;

public class GITRemoteRepositoryActions
{
	private final GITClient m_gitClient;

	private final GITRemoteRepositoryConfiguration m_remoteRepositoryConfig;
	
	private static final String HEAD_REF = "refs/heads/";

	public GITRemoteRepositoryActions(final GITClient gitClient, final GITRemoteRepositoryConfiguration remoteRepoConfiguration)
	{
		m_gitClient = gitClient;
		m_remoteRepositoryConfig = remoteRepoConfiguration;
	}

	public void verifyConnection()
	{
		try
		{
			CredentialsProvider credentialsProvider = m_gitClient.getProvider();
			String branchPath = HEAD_REF + m_remoteRepositoryConfig.getBranch();
			Collection<Ref> remoteReferences = new LsRemoteCommand(null).setCredentialsProvider(credentialsProvider).setRemote(m_remoteRepositoryConfig.getURL()).call();
			boolean branchFound = remoteReferences.stream().anyMatch(ref -> ref.getName().equals(branchPath));
			if (!branchFound)
			{
				throw StudioLogger.studioException(this, Messages.INVALID_GIT_BRANCH, m_remoteRepositoryConfig.getBranch());
			}
		}
		catch (TransportException e)
		{
			throw StudioLogger.studioException(this, e, Messages.INVALID_GIT_CREDENTIALS, m_remoteRepositoryConfig.getUserName(), m_remoteRepositoryConfig.getOriginalUrl());
		}
		catch (InvalidRemoteException e)
		{
			throw StudioLogger.studioException(this, e, Messages.INVALID_GIT_URL, m_remoteRepositoryConfig.getOriginalUrl());
		}
		catch (GitAPIException e)
		{
			throw StudioLogger.studioException(this, e, Messages.INVALID_GIT_CREDENTIALS, m_remoteRepositoryConfig.getUserName(), m_remoteRepositoryConfig.getOriginalUrl());
		}
	}

}
