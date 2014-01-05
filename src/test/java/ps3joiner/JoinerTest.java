package ps3joiner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

public class JoinerTest {

	private Path tempDir;

	@Before
	public void setUp() throws IOException {
		tempDir = Files.createTempDir().toPath().toAbsolutePath();
		createSplitFile("foo.66601", "abc");
		createSplitFile("foo.66602", "def");
	}
	
	private void createSplitFile(final String name, final String contents) throws IOException {
		final Path path = tempDir.resolve(name);
		try (final OutputStream out = new BufferedOutputStream(java.nio.file.Files.newOutputStream(path))) {
			out.write(contents.getBytes(Charsets.UTF_8));
		}
	}
	
	@Test
	public void retrieveSplitFileList() throws IOException {
		final Multimap<Path, Path> filesToJoin = Main.findFilesToJoin(tempDir);
		assertEquals(1, filesToJoin.keySet().size());
		final Path fooPath = tempDir.resolve("foo");
		assertTrue(filesToJoin.containsKey(fooPath));
		final Collection<Path> fooValues = filesToJoin.get(fooPath);
		assertEquals(ImmutableList.of(tempDir.resolve("foo.66601"), tempDir.resolve("foo.66602")), fooValues);
	}
	
	@Test
	public void joinFiles() throws IOException {
		final Path joinedFilePath = tempDir.resolve("foo");
		assertFalse(java.nio.file.Files.exists(joinedFilePath));
		
		final Multimap<Path, Path> filesToJoin = TreeMultimap.create();
		filesToJoin.put(tempDir.resolve("foo"), tempDir.resolve("foo.66601"));
		filesToJoin.put(tempDir.resolve("foo"), tempDir.resolve("foo.66602"));
		Main.mergeFiles(ImmutableMultimap.copyOf(filesToJoin), false, false);

		assertTrue(java.nio.file.Files.exists(joinedFilePath));
		final String joinedFileContents = CharStreams.toString(java.nio.file.Files.newBufferedReader(joinedFilePath, Charsets.UTF_8));
		assertEquals("abcdef", joinedFileContents);
	}
}
