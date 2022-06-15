package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.cordys.cws.IStudioDocument;
import com.cordys.cws.file.FileInfoHelper;
import com.cordys.cws.file.IFileInfo;
import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.internal.operation.runner.OperationRunner;
import com.cordys.cws.internal.repository.documenttype.IRepositoryStudioDocumentType;
import com.cordys.cws.internal.session.ISessionInternal;
import com.cordys.cws.internal.teamdevelopment.changesets.ISCMChangeSet;
import com.cordys.cws.internal.teamdevelopment.changesets.ISCMChangeSet.ESCMChangeAction;
import com.cordys.cws.old.synchronize.Synchronizer;
import com.cordys.cws.runtime.types.documenttype.IStudioDocumentType;
import com.cordys.cws.runtime.types.folder.StudioFolder;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.config.GITRemoteRepositoryConfiguration;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.operations.GITRemoteRepositoryActions;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.utils.GitUtils;
import com.cordys.cws.session.ISession;
import com.cordys.cws.synchronize.state.root.StateRootHelper;
import com.cordys.cws.util.StudioLogger;

public abstract class HandlerBase
{
	protected final GITClient m_gitClient;

	public final static String GIT_KEEP_FILE = ".gitkeep";

	public final static String DOCUMENT_TYPE_UNKNOWN = "Type unknown";

	public final static String HEAD = "HEAD";

	public abstract void execute();

	public HandlerBase(final GITClient gitClient)
	{
		m_gitClient = gitClient;
	}

	protected File getWorkspaceSynchFolder()
	{
		return m_gitClient.getWorkingCopyLocation();
	}

	protected GITRemoteRepositoryActions repoActions()
	{
		return m_gitClient.getGitRemoteRepositoryActions();
	}

	protected GITRemoteRepositoryConfiguration repoConfig()
	{
		return m_gitClient.getRemoteRepositoryConfiguration();
	}

	protected ISession getSession()
	{
		return m_gitClient.getSession();
	}

	protected GITClient getGitClient()
	{
		return m_gitClient;
	}

	protected Git getGITWorkingRepo()
	{
		return m_gitClient.getGITWorkingRepo();
	}

	public void doSynchronize()
	{
		ISession session = getSession();
		OperationRunner.runOperation(session, Synchronizer.getSynchronizer(session), false);
	}

	public void addEntry(final ESCMChangeAction action, final File fileObj, final ISCMChangeSet fileChangeSet, final IFileInfo fileInfoOriginal)
	{
		File entryFile = fileObj;
		boolean gitKeepFile = false;
		if (fileObj.getAbsolutePath().endsWith(GIT_KEEP_FILE))
		{
			if (action.equals(ESCMChangeAction.Update))
			{
				return;
			}
			gitKeepFile = true;
			entryFile = fileObj.getParentFile();
		}

		IStudioDocumentType documentType = StateRootHelper.getStateRoot(getSession()).getDocumentTypeExtensionMap().findDocumentType(entryFile.getName()).getStudioDocumentType();
		if (gitKeepFile && documentType.getDisplayName().toString().equalsIgnoreCase(DOCUMENT_TYPE_UNKNOWN))
		{
			IStudioDocument docFile = m_gitClient.findDocumentForFile(entryFile);
			if (docFile != null)
			{
				documentType = docFile.getDocumentType().getStudioDocumentType();
			}
			else
			{
				IRepositoryStudioDocumentType iRepositoryStudioDocumentType = ((ISessionInternal) getSession()).getDocumentTypeManager().getDocumentType(StudioFolder.class);
				documentType = iRepositoryStudioDocumentType.getStudioDocumentType();
			}
		}

		fileChangeSet.addEntry(documentType.getDocumentID(), entryFile.getName(), FileInfoHelper.createFileInfo(entryFile), documentType, action, fileInfoOriginal);
	}

	protected RevCommit getLastRevCommit(final Repository repo)
	{
		RevWalk walk = null;
		try
		{
			walk = new RevWalk(repo);
			ObjectId head = repo.resolve(HEAD);
			if (head != null)
			{
				return walk.parseCommit(head);
			}
		}
		catch (IOException exception)
		{
			throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
		}
		finally
		{
			if (walk != null)
			{
				walk.close();
			}
		}
		return null;
	}

	protected void createChangesetFromCommitEntry(final DiffEntry latestCommit, final ISCMChangeSet changeSet, final File workingCopyLocation, final String action)
	{
		File newFile = new File(workingCopyLocation + File.separator + latestCommit.getNewPath());
		switch (action)
		{
		case GITRemoteRepositoryConfiguration.ADD:
		case GITRemoteRepositoryConfiguration.COPY:
			addEntry(ESCMChangeAction.Add, newFile, changeSet, null);
			break;
		case GITRemoteRepositoryConfiguration.MODIFY:
			addEntry(ESCMChangeAction.Update, newFile, changeSet, null);
			break;
		case GITRemoteRepositoryConfiguration.RENAME:
			File originalFile = new File(workingCopyLocation + File.separator + latestCommit.getOldPath());
			boolean gitKeepFile = false;
			if (newFile.getAbsolutePath().endsWith(GIT_KEEP_FILE) && originalFile.getAbsolutePath().endsWith(GIT_KEEP_FILE))
			{
				gitKeepFile = true;
			}
			IFileInfo fileInfoOriginal = FileInfoHelper.createFileInfo(gitKeepFile ? originalFile.getParentFile() : originalFile);
			if (GitUtils.isRenameOnly(gitKeepFile ? originalFile.getParentFile() : originalFile, gitKeepFile ? newFile.getParentFile() : newFile))
			{
				addEntry(ESCMChangeAction.Rename, newFile, changeSet, fileInfoOriginal);
			}
			else
			{
				addEntry(ESCMChangeAction.Move, newFile, changeSet, fileInfoOriginal);
			}
			break;
		default:
			File oldFile = new File(workingCopyLocation + File.separator + latestCommit.getOldPath());
			addEntry(ESCMChangeAction.Delete, oldFile, changeSet, null);
			break;
		}
	}

	public void checkEmptyFoldersAndAddGitKeepFile()
	{
		File repoLocation = getWorkspaceSynchFolder();
		try
		{
			Files.walk(Paths.get(repoLocation.getAbsolutePath())).map(Path::toFile).filter(File::isDirectory).forEach(fileObj -> {
				try
				{
					if (GitUtils.isDirectoryEmpty(fileObj))
					{
						new File(fileObj + File.separator + GIT_KEEP_FILE).createNewFile();
					}
				}
				catch (IOException exception)
				{
					StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
				}
			});
		}
		catch (IOException exception)
		{
			StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
		}
	}

	public List<String> retrieveFilesFromDiffEntries(final List<DiffEntry> lsDiffEntries)
	{
		List<String> lsFiles = new ArrayList<String>();
		lsDiffEntries.forEach(diffEntry -> {
			lsFiles.add(diffEntry.getOldPath());
		});
		return lsFiles;
	}
}
