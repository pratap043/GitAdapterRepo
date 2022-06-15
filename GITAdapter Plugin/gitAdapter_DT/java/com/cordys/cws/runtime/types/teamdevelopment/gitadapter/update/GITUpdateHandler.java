package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.update;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.config.GITRemoteRepositoryConfiguration;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.utils.GitUtils;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.handler.HandlerBase;
import com.cordys.cws.util.StudioLogger;

public class GITUpdateHandler extends HandlerBase
{

	public GITUpdateHandler(final GITClient gitClient)
	{
		super(gitClient);
	}

	@Override
	public void execute()
	{
		GitUtils.showActionProgressMessage(getSession(), Messages.GIT_UPDATE);

		Git gitRepo = getGITWorkingRepo();
		try
		{
			gitRepo.pull().setCredentialsProvider(getGitClient().getProvider()).setRemote(GITRemoteRepositoryConfiguration.ORIGIN).setRemoteBranchName(getGitClient().getRemoteRepositoryConfiguration().getBranch()).call();
		}
		catch (GitAPIException exception)
		{
			throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
		}
		doSynchronize();
	}
}
