package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.revert;

import java.io.File;
import java.util.Collection;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.action.lock.ReleaseLockHandler;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.utils.GitUtils;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.handler.HandlerBase;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.status.GITStatusHandler;
import com.cordys.cws.util.StudioLogger;

public class GITRevertHandler extends HandlerBase
{

	public GITRevertHandler(final GITClient gitClient)
	{
		super(gitClient);
	}

	@Override
	public void execute()
	{
		try
		{
			doSynchronize();
			GitUtils.showActionProgressMessage(getSession(), Messages.GIT_REVERTING);
			GITStatusHandler gitStatusFiles = new GITStatusHandler(getGitClient());
			gitStatusFiles.execute();
			Collection<File> files = gitStatusFiles.getUpdatedFiles();

			Git workingRepo = getGITWorkingRepo();
			workingRepo.add().addFilepattern(".").call();
			workingRepo.reset().setMode(ResetType.HARD).call();
			workingRepo.clean().setCleanDirectories(true).call();

			GitUtils.restoreFileLastModifiedDate(files, getSession());
			GitUtils.touchUpdatedFile(files);
			doSynchronize();

			// Always release locks, regardless whether revert did something
			new ReleaseLockHandler(m_gitClient).releaseAllLocks(gitStatusFiles.getChangedAndDeletedSet());
		}
		catch (GitAPIException exception)
		{
			throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
		}
	}

}
