/*
 * Copyright 2014 Axel de Sablet
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package ps3joiner;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

public class SplitFilesVisitor extends SimpleFileVisitor<Path> {

	private final List<Path> foundPaths = new ArrayList<>();
	
	public List<Path> foundPaths() {
		return ImmutableList.copyOf(foundPaths);
	}
	
	@Override
	public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
		final String extension = Files.getFileExtension(file.getFileName().toString());
		if (extension.startsWith("666")) {
			foundPaths.add(file);
		}
		
		return FileVisitResult.CONTINUE;
	}

}
