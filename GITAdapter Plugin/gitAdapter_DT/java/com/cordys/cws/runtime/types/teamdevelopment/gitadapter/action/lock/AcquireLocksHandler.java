package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.action.lock;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cordys.cws.IStudioDocument;
import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.internal.repository.userinfo.UserMappingHelper;
import com.cordys.cws.runtime.types.teamdevelopment.ISCMComment;
import com.cordys.cws.runtime.types.teamdevelopment.SCMComment;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.changeset.GITIncorporateChangeSetStatusHandler;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.handler.HandlerBase;
import com.cordys.cws.runtime.types.teamdevelopment.locking.IOwnedSCMLockEntry;
import com.cordys.cws.runtime.types.teamdevelopment.locking.LockEntryBase;
import com.cordys.cws.runtime.types.teamdevelopment.locking.OwnedSCMLockEntry;
import com.cordys.cws.util.StudioLogger;
import com.cordys.cws.workspace.IRepositoryWorkspaceWrapper;

public class AcquireLocksHandler extends HandlerBase
{
	private final LockEntryBase m_lockFactory;

	private Map<File, IStudioDocument> m_toBeLockedFiles;

	private Collection<IOwnedSCMLockEntry> m_lockEntries;
	
	private List<String> lsModifiedFiles;

	public AcquireLocksHandler(final GITClient gitClient)
	{
		super(gitClient);
		m_lockFactory = new LockEntryBase(gitClient.getSession(), gitClient.getWorkingCopyLocation());
	}

	private boolean requiresExtraLocks()
	{
		return !m_toBeLockedFiles.isEmpty();
	}

	private void initCollections(final int size)
	{
		m_toBeLockedFiles = new HashMap<>(size);
		m_lockEntries = new ArrayList<>(size);
	}

	/**
	 * The only public API for this class.
	 * 
	 * @param models
	 *          The models to be locked
	 * @param lockComment
	 *          the comment to be given when locking
	 * @return collection of Lock entries
	 */
	public Collection<IOwnedSCMLockEntry> acquireLocks(final Collection<IStudioDocument> models, final String lockComment)
	{
		initCollections(models.size());

		for (IStudioDocument model : models)
		{
			readExistingOrLocalLocks(lockComment, model, findFileForModel(model));
		}
		if (requiresExtraLocks())
		{
			createLocks(lockComment);
		}
		return m_lockEntries;
	}

	private File findFileForModel(final IStudioDocument model)
	{
		File documentFile = m_gitClient.findFileForModel(model);
		if (documentFile == null)
		{
			documentFile = model.getDocumentType().getFileInfo(model).getFile();
		}
		return documentFile;
	}

	private void readExistingOrLocalLocks(final String lockComment, final IStudioDocument model, final File documentFile)
	{
		if (!documentFile.exists())
		{
			// its a new file. So we can just create the lock here.
			IOwnedSCMLockEntry lockEntry = m_lockFactory.createLockEntry(model, documentFile, OwnedSCMLockEntry.class, null, createComment(lockComment), false);
			lockEntry.setLockedDocumentChangeCount(model.getChangeCount());
			m_lockEntries.add(lockEntry);
		}
		else
		{
			m_toBeLockedFiles.put(documentFile, model);
		}
	}

	private void createLocks(final String lockComment)
	{
		ISCMComment comment = createComment(lockComment);
		comment.makeTransient();
		fetchUpdatedFiles();
		for (Entry<File, IStudioDocument> entry : m_toBeLockedFiles.entrySet())
		{
			checkIncorporateRequired(entry.getKey());
			GitLock localLock = new GitLockRESTHandler(m_gitClient).createLock(entry.getKey(), comment.createNativeComment());

			if (null != localLock)
			{
				m_lockEntries.add(createOwnedSCMLockEntry(entry.getKey(), entry.getValue(), localLock));
			}
		}
	}

	//pass id and comment
	public IOwnedSCMLockEntry createOwnedSCMLockEntry(final File documentFile, final IStudioDocument document, final GitLock localLock)
	{
		// there is a local lock which is still valid, we can go ahead
		ISCMComment comment = getSession().createStudioDocument(SCMComment.class);
		comment.parseFromNativeComment(localLock.getComment());

		IOwnedSCMLockEntry lockEntry = m_lockFactory.createLockEntry(document, documentFile, OwnedSCMLockEntry.class, localLock.getID(), comment, false);
		lockEntry.setLockedDocumentChangeCount(document.getChangeCount());
		return lockEntry;
	}

	private ISCMComment createComment(final String lockComment)
	{
		ISCMComment comment = getSession().createStudioDocument(SCMComment.class);
		comment.setComment(lockComment);
		comment.setUserDN(UserMappingHelper.getUserName(getSession(), getSession().getTransaction().getUserIdentity().getOrgUserDN()));
		IRepositoryWorkspaceWrapper repWorkspaceWrapper = getSession().getRepositoryWorkspace();
		comment.setWorkspace(String.format("'%s' from organization '%s'", repWorkspaceWrapper.getName(), repWorkspaceWrapper.getOrganizationName()));
		comment.setDateTime(System.currentTimeMillis());
		return comment;
	}

	@Override
	public void execute()
	{
		// Do nothing
	}

	private void checkIncorporateRequired(File fileObj)
	{
		if(!lsModifiedFiles.isEmpty())
		{
  		String filePath = fileObj.getAbsolutePath().replace(m_gitClient.getWorkingCopyLocation().getAbsolutePath(), "").replace("\\", "/");
  		filePath = filePath.substring(1, filePath.length());
  		if(lsModifiedFiles.contains(filePath))
  		{
  			throw StudioLogger.exception(this, Messages.REMOTE_CHANGES_DETECTED);
  		}
		}
	}
	
	private void fetchUpdatedFiles()
	{
		GITIncorporateChangeSetStatusHandler gitChangeSetFiles = new GITIncorporateChangeSetStatusHandler(m_gitClient);
		gitChangeSetFiles.execute();
		lsModifiedFiles = retrieveFilesFromDiffEntries(gitChangeSetFiles.fetchDiffFileEntries());
	}
}
