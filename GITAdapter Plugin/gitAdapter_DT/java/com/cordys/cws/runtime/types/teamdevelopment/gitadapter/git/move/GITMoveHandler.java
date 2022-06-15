package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.move;

import com.cordys.cws.file.FileInfoHelper;
import com.cordys.cws.file.IFileInfo;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.utils.GitUtils;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.handler.HandlerBase;
import com.cordys.cws.tasks.MoveFileTask;
import com.cordys.cws.tasks.StudioTaskHelper;

public class GITMoveHandler extends HandlerBase
{
	final IFileInfo originalFile;

	final IFileInfo destinationFile;

	final boolean deleteDestinationWhenExists;

	public GITMoveHandler(final GITClient GITClient, final IFileInfo originalFile, final IFileInfo destinationFile, final boolean deleteDestinationWhenExists)
	{
		super(GITClient);
		this.originalFile = originalFile;
		this.destinationFile = destinationFile;
		this.deleteDestinationWhenExists = deleteDestinationWhenExists;
	}

	@Override
	public void execute()
	{
		if (GitUtils.isRenameOnly(originalFile.getFile(), destinationFile.getFile()))
		{
			handleRename(originalFile, destinationFile);
		}
		else
		{
			handleMove(originalFile, destinationFile);
		}
	}

	private void handleMove(final IFileInfo originalFile, final IFileInfo destinationFile)
	{
		StudioTaskHelper.executeTask(new MoveFileTask(originalFile, destinationFile, true));
	}

	private void handleRename(final IFileInfo originalFile, final IFileInfo destinationFile)
	{
		FileInfoHelper.rename(originalFile, destinationFile);
	}

}
