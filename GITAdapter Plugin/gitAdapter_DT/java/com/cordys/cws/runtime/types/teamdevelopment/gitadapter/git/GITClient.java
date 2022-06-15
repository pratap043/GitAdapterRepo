package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.cordys.cws.IStudioDocument;
import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.internal.operation.runner.OperationRunner;
import com.cordys.cws.old.synchronize.Synchronizer;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.GITAdapter;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.config.GITRemoteRepositoryConfiguration;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.operations.GITRemoteRepositoryActions;
import com.cordys.cws.session.ISession;
import com.cordys.cws.synchronize.state.root.IStateRoot;
import com.cordys.cws.synchronize.state.root.StateRootHelper;
import com.cordys.cws.util.StudioLogger;

public class GITClient
{
	private final GITAdapter m_gitAdapter;

	private GITRemoteRepositoryConfiguration m_gitRemoteRepositoryConfiguration;

	private GITRemoteRepositoryActions m_gitRemoteRepositoryActions;

	public GITClient(final GITAdapter gitAdapter)
	{
		m_gitAdapter = gitAdapter;
	}

	/**
	 * This method is for now required to access the GIT Cache. It might be that this cache functionality will move, then
	 * this method has to be reconsidered.
	 */
	public GITAdapter getGitAdapter()
	{
		return m_gitAdapter;
	}

	public ISession getSession()
	{
		return m_gitAdapter.getSession();
	}

	public CredentialsProvider getProvider()
	{
		return new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", m_gitAdapter.getPassword());
	}

	public String getRepository()
	{
		return m_gitAdapter.getURL();
	}

	public void doSynchronize()
	{
		ISession session = this.getSession();
		OperationRunner.runOperation(session, Synchronizer.getSynchronizer(session), false);
	}

	public File getWorkingCopyLocation()
	{
		return new File(getSynchronizationStateRoot().getSynchronizeFolder());

	}

	private IStateRoot getSynchronizationStateRoot()
	{
		return StateRootHelper.getStateRoot(getSession(), true);
	}

	public File findFileForModel(final IStudioDocument model)
	{
		return getSynchronizationStateRoot().findFileForDocument(model);
	}

	public IStudioDocument findDocumentForFile(final File file)
	{
		return getSynchronizationStateRoot().findDocumentForFileName(file);
	}

	public Git getGITWorkingRepo()
	{
		try
		{
			return Git.open(this.getWorkingCopyLocation());
		}
		catch (IOException exception)
		{
			throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
		}
	}

	public final String getBasicAuthenticationHeader()
	{
		String valueToEncode = m_gitAdapter.getUsername() + ":" + m_gitAdapter.getPassword();
		return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
	}

	public GITRemoteRepositoryConfiguration getRemoteRepositoryConfiguration()
	{
		if (null == m_gitRemoteRepositoryConfiguration)
		{
			if (m_gitAdapter.getProxyEnabled())
			{
				m_gitRemoteRepositoryConfiguration = new GITRemoteRepositoryConfiguration(m_gitAdapter.getURL(),
																																									m_gitAdapter.getUsername(),
																																									m_gitAdapter.getPassword(),
																																									m_gitAdapter.getProxyHost(),
																																									m_gitAdapter.getProxyPort(),
																																									m_gitAdapter.getProxyUsername(),
																																									m_gitAdapter.getProxyPassword(),
																																									m_gitAdapter.getBranch());
			}
			else
			{
				m_gitRemoteRepositoryConfiguration = new GITRemoteRepositoryConfiguration(m_gitAdapter.getURL(), m_gitAdapter.getUsername(), m_gitAdapter.getPassword(), m_gitAdapter.getBranch());
			}

			m_gitRemoteRepositoryConfiguration.constructGitURL();
		}
		return m_gitRemoteRepositoryConfiguration;
	}

	public GITRemoteRepositoryActions getGitRemoteRepositoryActions()
	{
		if (null == m_gitRemoteRepositoryActions)
		{
			m_gitRemoteRepositoryActions = new GITRemoteRepositoryActions(this, getRemoteRepositoryConfiguration());
		}
		return m_gitRemoteRepositoryActions;
	}
}
