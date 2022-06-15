package com.cordys.cws.runtime.types.teamdevelopment.gitadapter;

import java.io.File;

import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.action.lock.GitLock;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.action.lock.GitLockRESTHandler;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.locking.IOwnedSCMLockEntry;
import com.cordys.cws.runtime.types.teamdevelopment.locking.ISCMLockEntry;
import com.cordys.cws.teamdevelopment.ITeamDevelopmentWithLockingSupport;

/**
 * IsValidLockHandler
 * 
 * GitAdapter delegates the IsValidLock call defined in {@link ITeamDevelopmentWithLockingSupport} to this class.
 * 
 */
public class IsValidLockHandler
{

	private final GITClient m_gitClient;

	public IsValidLockHandler(final GITClient gitClient)
	{
		this.m_gitClient = gitClient;
	}

	/**
	 * @param lockEntry
	 *          the entry to be checked
	 * @return true if the lockEntry is valid
	 */
	public boolean isValidLock(final ISCMLockEntry lockEntry)
	{
		// get lock based on File, because document ID can be changed because of move of plainText or type unknown
		File documentFile = new File(m_gitClient.getWorkingCopyLocation(), lockEntry.getLockedDocumentRelativePath());
		if (!documentFile.exists())
		{
			return true;
		}
		// only validate local lock for performance reasons
		GitLock localLock = new GitLockRESTHandler(m_gitClient).getLock(documentFile.getAbsolutePath());
		if (null != localLock)
		{
			assert (lockEntry instanceof IOwnedSCMLockEntry);
			return null != localLock.getID() && localLock.getID().equals(lockEntry.getSCMLockToken());
		}
		return false;
	}
}
