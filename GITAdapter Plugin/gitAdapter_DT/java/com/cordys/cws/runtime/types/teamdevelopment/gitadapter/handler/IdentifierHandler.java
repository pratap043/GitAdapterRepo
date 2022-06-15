package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.handler;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.cordys.cws.file.IFileInfo;
import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.util.StudioLogger;

public class IdentifierHandler extends HandlerBase
{
	private final IFileInfo fileObj;

	private String fileIdentifier;

	public static final String READ = "READ";

	public static final String WRITE = "WRITE";

	private String identifierOperation = READ;

	public IdentifierHandler(final GITClient gitClient, final IFileInfo gitFile, final String fileIdentifier)
	{
		super(gitClient);
		fileObj = gitFile;
		if (!fileIdentifier.equals(READ))
		{
			identifierOperation = WRITE;
			this.fileIdentifier = fileIdentifier;
		}
	}

	public String getFileIdentifier()
	{
		return fileIdentifier;
	}

	public void readIdentifier()
	{
		if (fileObj.isDirectory())
		{
			Path path = Paths.get(fileObj.getAbsoluteFileName() + File.separator + GIT_KEEP_FILE);
			readFileEntry(path);
		}
	}

	public void readFileEntry(final Path path)
	{
		if (path.toFile().exists())
		{
			try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8))
			{
				stream.forEach(line -> {
					if (!line.startsWith("/*"))
					{
						fileIdentifier = line.strip();
					}
				});
			}
			catch (IOException exception)
			{
				StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
			}
		}
	}

	public void writeIdentifier()
	{
		if (fileObj.isDirectory())
		{
			File gitKeepFile = new File(fileObj.getAbsoluteFileName() + File.separator + GIT_KEEP_FILE);
			if (!gitKeepFile.exists())
			{
				try (PrintStream printStream = new PrintStream(gitKeepFile, StandardCharsets.UTF_8))
				{
					gitKeepFile.createNewFile();
					printStream.println("/* This is a system generated file, please do not modify the file */");
					printStream.println(fileIdentifier);
				}
				catch (IOException exception)
				{
					StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
				}
			}
		}
	}

	@Override
	public void execute()
	{
		switch (identifierOperation)
		{
		case READ:
			readIdentifier();
			break;
		case WRITE:
			writeIdentifier();
			break;
		default:
			break;
		}
	}
}