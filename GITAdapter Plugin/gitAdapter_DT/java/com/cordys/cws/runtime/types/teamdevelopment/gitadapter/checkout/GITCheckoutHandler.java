package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.checkout;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.CredentialsProvider;

import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.config.GITRemoteRepositoryConfiguration;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.utils.GitUtils;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.handler.HandlerBase;
import com.cordys.cws.synchronize.state.root.StateRootHelper;
import com.cordys.cws.util.StudioLogger;

public class GITCheckoutHandler extends HandlerBase
{
	public GITCheckoutHandler(final GITClient gitClient)
	{
		super(gitClient);
	}

	@Override
	public void execute()
	{
		CredentialsProvider credentialsProvider = m_gitClient.getProvider();
		try
		{
			doSynchronize();
			GitUtils.showActionProgressMessage(getSession(), Messages.GIT_CHECKING_OUT);
			Git.cloneRepository()
				 .setURI(m_gitClient.getRepository())
				 .setDirectory(new File(StateRootHelper.getStateRoot(getSession(), true).getSynchronizeFolder()))
				 .setCredentialsProvider(credentialsProvider)
				 .setRemote(GITRemoteRepositoryConfiguration.ORIGIN)
				 .setBranch(m_gitClient.getRemoteRepositoryConfiguration().getBranch())
				 .call();
		}
		catch (TransportException exception)
		{
			throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
		}
		catch (GitAPIException exception)
		{
			throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
		}
		doSynchronize();
	}
}
