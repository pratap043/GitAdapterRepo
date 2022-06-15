package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.status;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.internal.teamdevelopment.changesets.ISCMChangeSet;
import com.cordys.cws.internal.teamdevelopment.changesets.ISCMChangeSet.ESCMChangeAction;
import com.cordys.cws.internal.teamdevelopment.changesets.SCMChangeSet;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.utils.GitUtils;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.handler.HandlerBase;
import com.cordys.cws.util.StudioLogger;

public class GITStatusHandler extends HandlerBase
{
	private final Set<String> addedFiles = new HashSet<String>();

	private final Set<String> missingFiles = new HashSet<String>();

	private Set<String> changedFiles = new HashSet<String>();

	private Set<String> uncommittedFiles = new HashSet<String>();

	public GITStatusHandler(final GITClient gitClient)
	{
		super(gitClient);
	}

	@Override
	public void execute()
	{
		Status status = getStatus();
		Set<String> addedFilesObj = status.getAdded();
		addedFilesObj.forEach(file -> {
			addedFiles.add(file);
		});
		Set<String> missingFilesObj = status.getMissing();
		missingFilesObj.forEach(file -> {
			missingFiles.add(file);
		});
		changedFiles = status.getChanged();
		uncommittedFiles = status.getUncommittedChanges();
	}

	private Status getStatus()
	{
		try
		{
			Git gitRepo = getGITWorkingRepo();
			checkEmptyFoldersAndAddGitKeepFile();
			gitRepo.add().addFilepattern(".").call();
			Status status = gitRepo.status().call();
			return status;
		}
		catch (GitAPIException exception)
		{
			throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
		}
	}

	public Set<String> getAddedFiles()
	{
		return addedFiles;
	}

	public Set<String> getMissingFiles()
	{
		return missingFiles;
	}

	public Set<String> getChangedFiles()
	{
		return changedFiles;
	}

	public Set<String> getUncommittedFiles()
	{
		return uncommittedFiles;
	}

	private ISCMChangeSet createChangeSet()
	{
		ISCMChangeSet outgoingChanges = getSession().createStudioDocument(SCMChangeSet.class);
		outgoingChanges.makeTransient();
		return outgoingChanges;
	}

	public ISCMChangeSet getChangeSet()
	{
		ISCMChangeSet filesChangeSet = prepareChangeset();
		File workingCopyLocation = getGitClient().getWorkingCopyLocation();
		addEntries(ESCMChangeAction.Add, addedFiles, filesChangeSet, workingCopyLocation);
		addEntries(ESCMChangeAction.Delete, missingFiles, filesChangeSet, workingCopyLocation);
		addEntries(ESCMChangeAction.Update, changedFiles, filesChangeSet, workingCopyLocation);
		return filesChangeSet;
	}

	public Set<String> getChangedAndDeletedSet()
	{
		Status status = getStatus();
		Set<String> files = new HashSet<String>();
		files.addAll(status.getChanged());
		files.addAll(status.getMissing());
		return files;
	}

	private ISCMChangeSet prepareChangeset()
	{
		ISCMChangeSet filesChangeSet = createChangeSet();
		File workingCopyLocation = getGitClient().getWorkingCopyLocation();
		fetchAddAndRenameOperations(workingCopyLocation, filesChangeSet);
		checkAndRemoveDeletedFiles();
		return filesChangeSet;
	}

	public Collection<File> getUpdatedFiles()
	{
		File sourcePath = getGitClient().getWorkingCopyLocation();
		Collection<File> files = new ArrayList<>();
		GitUtils.addToFilesCollection(uncommittedFiles, files, sourcePath);
		return files;
	}

	public Map<String, File> getFiles()
	{
		File sourcePath = getGitClient().getWorkingCopyLocation();
		Map<String, File> files = new HashMap<>();
		uncommittedFiles.forEach(fileObj -> {
			File file = new File(sourcePath + File.separator + fileObj);
			files.put(fileObj, file);
		});
		return files;
	}

	@SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
	private void fetchAddAndRenameOperations(final File workingCopyLocation, final ISCMChangeSet filesChangeSet)
	{
		Repository repo = getGitClient().getGITWorkingRepo().getRepository();
		TreeWalk treeWalk = new TreeWalk(repo);
		treeWalk.setRecursive(true);
		try
		{
			RevCommit revCommit = getLastRevCommit(repo);
			if (revCommit != null)
			{
				treeWalk.addTree(revCommit.getTree());
				treeWalk.addTree(new FileTreeIterator(repo));

				RenameDetector renameDetector = new RenameDetector(repo);
				renameDetector.addAll(DiffEntry.scan(treeWalk));

				List<DiffEntry> diffEntryList = renameDetector.compute(treeWalk.getObjectReader(), null);
				for (DiffEntry diffEntry : diffEntryList)
				{
					if (diffEntry.getScore() >= renameDetector.getRenameScore())
					{
						String operationType = diffEntry.getChangeType().toString();

						createChangesetFromCommitEntry(diffEntry, filesChangeSet, workingCopyLocation, operationType);

						if (operationType.equalsIgnoreCase(ESCMChangeAction.Rename.toString()))
						{
							missingFiles.remove(diffEntry.getOldPath());
							addedFiles.remove(diffEntry.getNewPath());
						}
						else if (operationType.equalsIgnoreCase(ESCMChangeAction.Copy.toString()))
						{
							addedFiles.remove(diffEntry.getNewPath());
						}
					}
				}
			}
		}
		catch (CanceledException | IOException exception)
		{
			throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
		}
	}

	private void checkAndRemoveDeletedFiles()
	{
		Set<String> filesToBeRemoved = new HashSet<String>();
		missingFiles.forEach(file -> {
			if (addedFiles.contains(file))
			{
				filesToBeRemoved.add(file);
			}
		});

		filesToBeRemoved.forEach(file -> {
			missingFiles.remove(file);
			addedFiles.remove(file);
		});
	}

	private void addEntries(final ESCMChangeAction action, final Set<String> filesSet, final ISCMChangeSet fileChangeSet, final File workingCopyLocation)
	{
		filesSet.forEach(fileObj -> {
			File fileTobeAdded = new File(workingCopyLocation + File.separator + fileObj);
			addEntry(action, fileTobeAdded, fileChangeSet, null);
		});
	}

}