/*
 * Copyright 2014 Axel de Sablet
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package ps3joiner;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.LoggerFactory;

public class Main {
	
	public static void main(final String[] args) throws ParseException {
		final CommandLine line = parseCommandLine(args);
		
		final boolean dryRun = line.hasOption("-n");
		final Path path = createWorkingPath(line);

		LoggerFactory.getLogger(Main.class).trace("Dry-run: " + dryRun);
	}

	public static Path createWorkingPath(final CommandLine line) {
		final Path path = Paths.get(line.getArgs()[0]);
		return path;
	}

	public static CommandLine parseCommandLine(final String[] args) throws ParseException {
		final Options cliOptions = new Options();		
		cliOptions.addOption("n", false, "dry-run");
		
		final CommandLine line = new BasicParser().parse(cliOptions, args);
		return line;
	}
}
