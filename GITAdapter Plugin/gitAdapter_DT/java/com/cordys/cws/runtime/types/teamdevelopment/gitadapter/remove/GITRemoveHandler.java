package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.remove;

import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;

import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.handler.HandlerBase;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.status.GITStatusHandler;
import com.cordys.cws.util.StudioLogger;

public class GITRemoveHandler extends HandlerBase
{

	public GITRemoveHandler(final GITClient gitClient)
	{
		super(gitClient);
	}

	@Override
	public void execute()
	{
		Git git = getGITWorkingRepo();
		GITStatusHandler gitStatusHandler = new GITStatusHandler(getGitClient());
		gitStatusHandler.execute();
		Set<String> deletedFiles = gitStatusHandler.getMissingFiles();
		deletedFiles.forEach(fileObj -> {
			try
			{
				git.rm().addFilepattern(fileObj).call();
			}
			catch (NoFilepatternException exception)
			{
				throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
			}
			catch (GitAPIException exception)
			{
				throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
			}
		});
	}
}
