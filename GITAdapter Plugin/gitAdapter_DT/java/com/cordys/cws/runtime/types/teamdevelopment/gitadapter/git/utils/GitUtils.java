package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.internal.operation.OperationBase;
import com.cordys.cws.internal.operation.runner.OperationRunner;
import com.cordys.cws.internal.util.FileUtils;
import com.cordys.cws.session.ISession;
import com.cordys.cws.synchronize.state.item.IStateItem;
import com.cordys.cws.synchronize.state.root.StateRootHelper;
import com.cordys.cws.util.StudioLogger;
import com.eibus.localization.IStringResource;

public final class GitUtils
{
	private GitUtils()
	{
	}

	public static void restoreFileLastModifiedDate(final Collection<File> files, final ISession session)
	{
		//As a revert brings back the files to the state before the latest change, that timestamp will be back in history
		//A touch might bring it back on right the same timestamp as already known right now.
		//Then after the touch the synchronizer will not recognize the change anymore.
		//Therefore we make the timestamp now equal to what we knew before and a touch will update it to a different timestamp.
		//E.g.
		//Document update: File touched: BinaryDocument.doc 1408109815000 to 1408109816000
		//Revert will bring back the timestamp to 1408109815000
		//Then a touch will bring it back on 1408109816000, which is not good.
		//With the below code we bring it to 1408109816000 on revert and the touch will bring it to 1408109817000
		for (File file : files)
		{
			if (file.isFile())
			{
				IStateItem stateItem = StateRootHelper.getStateRoot(session).findStateItemForFileName(file);
				if (stateItem != null)
				{
					file.setLastModified(stateItem.getFileLastModified());
				}
			}
		}
	}

	public static void touchUpdatedFile(final Collection<File> files)
	{
		files.forEach(fileObj -> {
			if (fileObj.isFile())
			{
				FileUtils.touchFile(fileObj);
			}
		});
	}

	public static void addToFilesCollection(final Set<String> filesSet, final Collection<File> files, final File sourcePath)
	{
		filesSet.forEach(fileObj -> {
			File file = new File(sourcePath + File.separator + fileObj);
			files.add(file);
		});
	}

	public static boolean isRenameOnly(final File sourceFile, final File destinationFile)
	{
		return sourceFile.getParentFile().equals(destinationFile.getParentFile())
					 && !(sourceFile.getName().equals(destinationFile.getName()));
	}
	
	public static void showActionProgressMessage(final ISession session, final IStringResource identification)
	{
		OperationBase actionOperation = new OperationBase(identification)
		{
			@Override
			protected void doOperation()
			{
				//do nothing as this is just to display the Operation Progress Message
			}
		};

		OperationRunner.runOperation(session, actionOperation, false);
	}
	
	public static boolean isDirectoryEmpty(final File directory)
	{
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory.toPath()))
		{
			return !dirStream.iterator().hasNext();
		}
		catch (IOException exception)
		{
			StudioLogger.studioException(GitUtils.class, exception, Messages.ERROR, exception.getMessage());
		}
		return false;
	}
	
}
