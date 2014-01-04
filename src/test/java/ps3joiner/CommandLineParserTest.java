/*
 * Copyright 2014 Axel de Sablet
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package ps3joiner;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import com.google.common.io.Files;

public class CommandLineParserTest {

	@Test
	public void testDryRunSet() throws ParseException {
		final CommandLine line = Main.parseCommandLine(new String[] {"-n", "/tmp"});
		assertTrue(line.hasOption("-n"));
	}
	
	@Test
	public void testDryRunUnSet() throws ParseException {
		final CommandLine line = Main.parseCommandLine(new String[] {"/tmp"});
		assertFalse(line.hasOption("-n"));
	}
	
	@Test
	public void testWorkingPath() throws ParseException {
		final File tmpDir = Files.createTempDir();
		final CommandLine line = Main.parseCommandLine(new String[] {"-n", tmpDir.getAbsolutePath()});
		final Path foundPath = Main.createWorkingPath(line);
		
		assertEquals(tmpDir.getAbsoluteFile(), foundPath.toAbsolutePath().toFile());
	}
}
