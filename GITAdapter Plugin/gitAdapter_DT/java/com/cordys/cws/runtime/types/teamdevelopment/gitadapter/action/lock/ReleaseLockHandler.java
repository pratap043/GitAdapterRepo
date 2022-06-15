package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.action.lock;

import java.io.File;
import java.util.Collection;

import com.cordys.cws.IStudioDocument;
import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.internal.repository.ModelChangeHelper;
import com.cordys.cws.runtime.types.teamdevelopment.TeamDevelopment;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.handler.HandlerBase;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.status.GITStatusHandler;
import com.cordys.cws.runtime.types.teamdevelopment.locking.IOwnedSCMLockEntry;
import com.cordys.cws.util.StudioLogger;

public class ReleaseLockHandler extends HandlerBase
{
	public ReleaseLockHandler(final GITClient gitClient)
	{
		super(gitClient);
	}

	public void releaseAllLocks()
	{
		GITStatusHandler gitStatusFiles = new GITStatusHandler(getGitClient());
		gitStatusFiles.execute();
		Collection<String> files = gitStatusFiles.getChangedAndDeletedSet();
		releaseAllLocks(files);
		TeamDevelopment.clearLockAdministration(getSession());
	}

	public void releaseAllLocks(Collection<String> files)
	{
		files.forEach(file -> releaseAllLocksOnWorkingCopy(file));
		TeamDevelopment.clearLockAdministration(getSession());
	}

	private void releaseAllLocksOnWorkingCopy(String documentPath)
	{
		GitLockRESTHandler gitLockRESTHandler = new GitLockRESTHandler(m_gitClient);
		GitLock lock = gitLockRESTHandler.getLock(documentPath);
		if (lock != null)
		{
			gitLockRESTHandler.deleteLock(lock.getID());
		}

	}

	public boolean releaseLock(final String documentID)
	{
		// do not force, if model version is different then don't release lock
		IOwnedSCMLockEntry lock = (IOwnedSCMLockEntry) getSession().loadStudioDocument(documentID);
		if (null == lock)
		{
			return true;
		}
		IStudioDocument lockedDocument = lock.getLockedDocument();

		if (null == lockedDocument)
		{
			StudioLogger.info(this, Messages.NO_DOCUMENT_FOUND_ON_LOCK_LOCATION, lock.getPath());
			return false;
		}

		if (!lock.getLockedDocumentModelVersion().equals(ModelChangeHelper.getModelVersionString(lockedDocument)))
		{
			StudioLogger.info(this,
												Messages.ERROR_RELEASING_lOCK_OF_CHANGED_DOCUMENT,
												lock.getLockedDocumentModelVersion(),
												lockedDocument.getPath(),
												ModelChangeHelper.getModelVersionString(lockedDocument));

			return false;
		}

		return releaseLock(lock);
	}

	public boolean releaseLock(final IOwnedSCMLockEntry lock)
	{
		File lockedFile = new File(m_gitClient.getWorkingCopyLocation(), lock.getLockedDocumentRelativePath());
		if (lockedFile.exists())
		{
			new GitLockRESTHandler(m_gitClient).deleteLock(lock.getSCMLockToken());
		}
		getSession().deleteStudioDocument(lock);
		return true;
	}

	@Override
	public void execute()
	{
		// Do nothing
	}
}
