package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.changeset;

import com.cordys.cws.internal.teamdevelopment.changesets.ISCMChangeSet;
import com.cordys.cws.internal.teamdevelopment.changesets.SCMChangeSet;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;

public class GITIncorporateChangeSetCreator
{

	private final GITClient gitClient;

	public GITIncorporateChangeSetCreator(GITClient gitClient)
	{
		this.gitClient = gitClient;
	}

	public ISCMChangeSet determineIncorporateChangeSet()
	{

		GITIncorporateChangeSetStatusHandler gitChangeSetFiles = new GITIncorporateChangeSetStatusHandler(gitClient);
		ISCMChangeSet incomingChangeSet = createChangeSet();
		gitChangeSetFiles.execute();
		gitChangeSetFiles.determineIncomingChangesFromRepository(incomingChangeSet);

		return incomingChangeSet;
	}

	private ISCMChangeSet createChangeSet()
	{

		ISCMChangeSet incomingChanges = this.gitClient.getSession().createStudioDocument(SCMChangeSet.class);
		incomingChanges.makeTransient();
		return incomingChanges;
	}

}
