/*
 * Copyright 2014 Axel de Sablet
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package ps3joiner;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.google.common.io.ByteStreams;

public class Main {
	
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(final String[] args) throws ParseException, IOException {
		final CommandLine line = parseCommandLine(args);
		
		final boolean dryRun = line.hasOption("-n");
		final boolean deleteSplitFiles = line.hasOption("-d");
		final Path path = createWorkingPath(line);

		logger.trace("Dry-run: " + dryRun);
		logger.trace("Working directory: " + path);

		final Multimap<Path, Path> filesToJoin = findFilesToJoin(path);
		
		mergeFiles(filesToJoin, dryRun, deleteSplitFiles);
		
		return;
	}

	public static void mergeFiles(final Multimap<Path, Path> filesToJoin, final boolean dryRun, final boolean deleteSplitFiles) throws IOException {
		for (final Path wholeFilePath : filesToJoin.keySet()) {
			logger.trace("Rebuilding file {}", wholeFilePath);
			
			final Collection<Path> partialFiles = filesToJoin.get(wholeFilePath);

			try (final OutputStream out = new BufferedOutputStream(Files.newOutputStream(wholeFilePath))) {
				for (final Path partialFilePath : partialFiles) {
					logger.trace("Reading file {}", partialFilePath);
					
					try (final InputStream in = new BufferedInputStream(Files.newInputStream(partialFilePath))) {
						if (!dryRun) {
							ByteStreams.copy(in, out);
						}
					}
					if (deleteSplitFiles && !dryRun) {
						logger.trace("Deleting file {}", partialFilePath);
						Files.delete(partialFilePath);
					}
				}
			}

			logger.trace("Closing file {}", wholeFilePath);
		}
	}

	/**
	 * @return a MultiMap where each key is the Path to a whole file, and the values are the partial files, sorted by name
	 */
	public static ImmutableMultimap<Path, Path> findFilesToJoin(final Path path) throws IOException {
		final Map<Path, Path> splitToWholeFileMapping = new HashMap<>();
		
		final SplitFilesVisitor visitor = new SplitFilesVisitor();
		Files.walkFileTree(path, visitor);
		final List<Path> foundPaths = visitor.foundPaths();
		for (final Path foundPath : foundPaths) {
			final Path originalFilePath = findOriginalFileFromSplitFile(foundPath);
			splitToWholeFileMapping.put(foundPath, originalFilePath);
		}
		
		final Multimap<Path, Path> multimap = TreeMultimap.create();
		for (final Entry<Path, Path> entry : splitToWholeFileMapping.entrySet()) {
			final Path splitFilePath = entry.getKey();
			final Path wholeFilePath = entry.getValue();
			
			multimap.put(wholeFilePath, splitFilePath);
		}
		return ImmutableMultimap.copyOf(multimap);
	}

	public static Path findOriginalFileFromSplitFile(final Path splitFilePath) {
		final String extension = com.google.common.io.Files.getFileExtension(splitFilePath.getFileName().toString());
		final String nameWithoutExtension = com.google.common.io.Files.getNameWithoutExtension(splitFilePath.toAbsolutePath().toString());
	
		final String replacedExtension = extension.replaceAll("666\\d+", "");
		return Paths.get(splitFilePath.getParent().toString(), nameWithoutExtension, replacedExtension);
	}
	
	public static Path createWorkingPath(final CommandLine line) {
		String[] args = line.getArgs();
		final Path path;
		
		if (args.length > 0)
			path = Paths.get(args[0]);
		else
			path = Paths.get(".");
		
		return path.toAbsolutePath();
	}

	public static CommandLine parseCommandLine(final String[] args) throws ParseException {
		final Options cliOptions = new Options();
		cliOptions.addOption("n", false, "dry-run");
		cliOptions.addOption("d", false, "delete split files");
		
		final CommandLine line = new BasicParser().parse(cliOptions, args);
		return line;
	}
}
