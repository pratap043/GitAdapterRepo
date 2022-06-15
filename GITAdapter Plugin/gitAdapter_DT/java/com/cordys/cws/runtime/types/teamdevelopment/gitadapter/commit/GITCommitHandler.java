package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.commit;

import java.util.Collection;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;

import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.action.lock.ReleaseLockHandler;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.utils.GitUtils;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.handler.HandlerBase;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.push.GITPushHandler;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.remove.GITRemoveHandler;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.status.GITStatusHandler;
import com.cordys.cws.util.StudioLogger;

public class GITCommitHandler extends HandlerBase
{
	private final String m_comment;

	public GITCommitHandler(final GITClient gitClient, final String comment)
	{
		super(gitClient);
		this.m_comment = comment;
	}

	@Override
	public void execute()
	{
		Git git = getGITWorkingRepo();
		CredentialsProvider credentialsProvider = m_gitClient.getProvider();
		try
		{
			doSynchronize();
			GITStatusHandler gitStatusFiles = new GITStatusHandler(getGitClient());
			gitStatusFiles.execute();
			Collection<String> files = gitStatusFiles.getChangedAndDeletedSet();
			new GITRemoveHandler(m_gitClient).execute();
			GitUtils.showActionProgressMessage(getSession(), Messages.GIT_COMMITING);
			git.commit().setCredentialsProvider(credentialsProvider).setMessage(this.m_comment).call();
			new GITPushHandler(m_gitClient).execute();
			new ReleaseLockHandler(m_gitClient).releaseAllLocks(files);
		}
		catch (GitAPIException exception)
		{
			throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
		}
		doSynchronize();
	}
}