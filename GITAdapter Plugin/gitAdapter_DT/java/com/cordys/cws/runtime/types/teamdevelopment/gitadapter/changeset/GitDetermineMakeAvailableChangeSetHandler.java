package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.changeset;

import com.cordys.cws.internal.teamdevelopment.changesets.ISCMChangeSet;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.status.GITStatusHandler;

public class GitDetermineMakeAvailableChangeSetHandler
{

	private final GITClient gitClient;

	public GitDetermineMakeAvailableChangeSetHandler(final GITClient gitClient)
	{
		this.gitClient = gitClient;
	}

	public ISCMChangeSet determineChangeSet()
	{
		GITStatusHandler gitStatusFiles = new GITStatusHandler(gitClient);
		gitStatusFiles.execute();
		return gitStatusFiles.getChangeSet();
	}

}
