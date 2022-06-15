package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.reset;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.handler.HandlerBase;
import com.cordys.cws.util.StudioLogger;

public class GITResetHandler extends HandlerBase
{

	private final static String BRANCH_PATH = "refs/remotes/origin/";

	public GITResetHandler(GITClient GITClient)
	{
		super(GITClient);
	}

	@Override
	public void execute()
	{
		Git git = getGITWorkingRepo();
		try
		{
			git.reset().setRef(BRANCH_PATH + m_gitClient.getRemoteRepositoryConfiguration().getBranch()).setMode(ResetType.MIXED).call();
		}
		catch (GitAPIException exception)
		{
			throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
		}
	}
}
