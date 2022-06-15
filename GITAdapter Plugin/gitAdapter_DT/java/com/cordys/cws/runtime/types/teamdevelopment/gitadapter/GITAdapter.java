package com.cordys.cws.runtime.types.teamdevelopment.gitadapter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;

import com.cordys.cws.IStudioDocument;
import com.cordys.cws.exception.StudioException;
import com.cordys.cws.file.IFileInfo;
import com.cordys.cws.internal.synchronize.ISynchronizeTeamDevelopment;
import com.cordys.cws.internal.teamdevelopment.changehistorysets.ISCMChangeHistorySet;
import com.cordys.cws.internal.teamdevelopment.changesets.CombinedSCMChangeSet;
import com.cordys.cws.internal.teamdevelopment.changesets.ICombinedSCMChangeSet;
import com.cordys.cws.internal.teamdevelopment.changesets.ISCMChangeSet;
import com.cordys.cws.internal.teamdevelopment.changesets.SCMChangeSet;
import com.cordys.cws.methods.LocalizableMessageDefinition;
import com.cordys.cws.plaintext.IObjectIdentityProvider;
import com.cordys.cws.runtime.types.teamdevelopment.actionsbyscm.ISCMDoesDelete;
import com.cordys.cws.runtime.types.teamdevelopment.actionsbyscm.ISCMDoesMove;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.action.lock.AcquireLocksHandler;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.action.lock.ReleaseLockHandler;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.changeset.GITIncorporateChangeSetCreator;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.changeset.GitDetermineMakeAvailableChangeSetHandler;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.checkout.GITCheckoutHandler;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.commit.GITCommitHandler;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.move.GITMoveHandler;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.handler.IdentifierHandler;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.revert.GITRevertHandler;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.update.GITUpdateHandler;
import com.cordys.cws.runtime.types.teamdevelopment.locking.IOwnedSCMLockEntry;
import com.cordys.cws.runtime.types.teamdevelopment.locking.ISCMLockEntry;
import com.cordys.cws.synchronize.state.root.IStateRoot;
import com.cordys.cws.tasks.DeleteFileTask;
import com.cordys.cws.tasks.StudioTaskHelper;
import com.cordys.cws.teamdevelopment.ITeamDevelopment;
import com.cordys.cws.teamdevelopment.ITeamDevelopmentWithLockingSupport;
import com.cordys.cws.teamdevelopment.pluginregistration.TeamPluginDefinition;
import com.cordys.cws.util.StudioLogger;

@TeamPluginDefinition(teamPluginType = GITAdapter.class, configurationViewType = "com.cordys.cws.runtime.types.teamdevelopment.gitadapter.views.properties.view.GITPropertiesView", name = @LocalizableMessageDefinition(messageID = "Cordys.CWS.Messages.gitForTeamDevelopment"))
public class GITAdapter extends GITAdapter_Base_ implements
												ITeamDevelopment,
												ISynchronizeTeamDevelopment,
												ITeamDevelopmentWithLockingSupport,
												ISCMDoesMove,
												ISCMDoesDelete,
												IObjectIdentityProvider
{
	private final GITClient gitClient = new GITClient(this);

	@Override
	public boolean isSynchronizable(final IFileInfo fileInfo)
	{
		try
		{
			Status status = gitClient.getGITWorkingRepo().status().call();
			String filePath = fileInfo.getFile().getAbsolutePath().replace(gitClient.getWorkingCopyLocation().getAbsolutePath(), "").replace("\\", "/");
			String formattedValue = filePath.substring(1, filePath.length());
			return !status.getMissing().contains(formattedValue)
						 && !status.getRemoved().contains(formattedValue)
						 && !status.getUntracked().contains(formattedValue)
						 && !status.getIgnoredNotInIndex().contains(formattedValue);
		}
		catch (GitAPIException exception)
		{
			throw StudioLogger.studioException(this, exception);
		}
	}

	@Override
	public boolean checkAllowedInSCM(final String path, final String documentFileName)
	{
		return true;
	}

	@Override
	public String getCheckSum(final IFileInfo fileInfo)
	{
		return "";
	}

	@Override
	public String getIdentifier(final IFileInfo fileInfoObj)
	{
		IdentifierHandler identifierHandler = new IdentifierHandler(gitClient, fileInfoObj, IdentifierHandler.READ);
		identifierHandler.execute();
		return identifierHandler.getFileIdentifier();
	}

	@Override
	public void setIdentifier(final IFileInfo fileInfoObj, final String fileIdentifier)
	{
		new IdentifierHandler(gitClient, fileInfoObj, fileIdentifier).execute();
	}

	@Override
	public void doDelete(final IFileInfo fileInfoToDelete)
	{
		StudioTaskHelper.executeTask(new DeleteFileTask(fileInfoToDelete));
	}

	@Override
	public void doMove(final IFileInfo originalFileInfo, final IFileInfo destinationFileInfo, final boolean deleteDestinationWhenExists)
	{
		new GITMoveHandler(gitClient, originalFileInfo, destinationFileInfo, deleteDestinationWhenExists).execute();
	}

	@Override
	public Collection<IOwnedSCMLockEntry> acquireLocks(final Collection<IStudioDocument> models, final String lockComment)
	{
		return new AcquireLocksHandler(gitClient).acquireLocks(models, lockComment);
	}

	@Deprecated
	@Override
	public boolean forceReleaseLock(final IOwnedSCMLockEntry lockEntry)
	{
		return new ReleaseLockHandler(gitClient).releaseLock(lockEntry);
	}

	@Override
	public boolean isValidLock(final ISCMLockEntry lockEntry)
	{
		return new ValidLockHandler(gitClient).isValidLock(lockEntry);
	}

	@Override
	public boolean releaseLock(final String documentID)
	{
		return new ReleaseLockHandler(gitClient).releaseLock(documentID);
	}

	@Override
	public void releaseLock(final File docFile) throws StudioException
	{
		new ReleaseLockHandler(gitClient).releaseLock(docFile.getAbsolutePath());
	}

	@Override
	public void registerAddToSCM(final IFileInfo arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void registerMoveInSCM(final IFileInfo arg0, final IFileInfo arg1)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void registerRemoveFromSCM(final IFileInfo arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void registerReplaceInSCM(final IFileInfo arg0, final IFileInfo arg1)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public IStateRoot getWorkingCopyStateRoot()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createWorkspace()
	{
		new GITCheckoutHandler(gitClient).execute();
	}

	@Override
	public ISCMChangeHistorySet determineChangeHistorySet(final String arg0, final long arg1, final long arg2, final String arg3)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICombinedSCMChangeSet determineChangeSet()
	{
		gitClient.doSynchronize();
		ICombinedSCMChangeSet combinedSCMChangeSet = gitClient.getSession().createStudioDocument(CombinedSCMChangeSet.class);
		combinedSCMChangeSet.makeTransient();
		ISCMChangeSet outgoingChangeSet = new GitDetermineMakeAvailableChangeSetHandler(gitClient).determineChangeSet();
		combinedSCMChangeSet.setOutgoingChanges(outgoingChangeSet);
		ISCMChangeSet incomingChanges = this.gitClient.getSession().createStudioDocument(SCMChangeSet.class);
		incomingChanges.makeTransient();
		if (!combinedSCMChangeSet.getOutgoingChanges().isEmpty())
		{
			combinedSCMChangeSet.setIncomingChanges(incomingChanges);
		}
		return combinedSCMChangeSet;
	}

	@Override
	public ISCMChangeSet determineIncorporateChangeSet()
	{
		return new GITIncorporateChangeSetCreator(gitClient).determineIncorporateChangeSet();
	}

	@Override
	public Collection<ISCMLockEntry> determineLocksOnRepository()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void incorporate()
	{
		new GITUpdateHandler(gitClient).execute();

	}

	@Override
	public boolean isConsistent()
	{
		try
		{
			Git git = Git.open(gitClient.getWorkingCopyLocation());
			Status status;
			try
			{
				status = git.status().call();
			}
			catch (GitAPIException exception)
			{
				throw StudioLogger.studioException(this, exception);
			}
			return status.getConflicting().isEmpty();
		}
		catch (RepositoryNotFoundException exception)
		{
			return true;
		}
		catch (IOException exception)
		{
			throw StudioLogger.studioException(this, exception);
		}
	}

	@Override
	public void makeAvailable(final String comment, final long revision)
	{
		new GITCommitHandler(gitClient, comment).execute();
	}

	@Override
	public void releaseAllLocks()
	{
		new ReleaseLockHandler(gitClient).releaseAllLocks();
	}

	@Override
	public void removeWorkspace()
	{
		new ReleaseLockHandler(gitClient).releaseAllLocks();
	}

	@Override
	public void revert()
	{
		new GITRevertHandler(gitClient).execute();
	}

	@Override
	public void validateSCMVersionSupported()
	{
		// TODO Auto-generated method stub

	}

	@Override
	@SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation")
	public boolean testConnection()
	{
		gitClient.getGitRemoteRepositoryActions().verifyConnection();
		return true;
	}

	@Override
	public boolean hasProject()
	{
		return true;
	}

}
