package sneer.bricks.software.bricks.finder.tests;

import static basis.environments.Environments.my;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.junit.Test;

import basis.testsupport.AssertUtils;

import sneer.bricks.software.bricks.finder.BrickFinder;
import sneer.bricks.software.code.compilers.java.tests.JarUtils;
import sneer.bricks.software.folderconfig.FolderConfig;
import sneer.bricks.software.folderconfig.testsupport.BrickTestBase;

public class BrickFinderTest extends BrickTestBase {

	private final BrickFinder _subject = my(BrickFinder.class);
	
	@Test
	public void findBricks() throws IOException {
		File testDir = JarUtils.fileFor(getClass()).getParentFile();
		my(FolderConfig.class).ownBinFolder().set(testDir);
		my(FolderConfig.class).binFolder().set(testDir);
		
		Collection<String> bricks = _subject.findBricks();

		AssertUtils.assertContents(bricks,
			sneer.bricks.software.bricks.finder.tests.fixtures.brick1.BrickWithoutNature.class.getName(),
			sneer.bricks.software.bricks.finder.tests.fixtures.brick2.BrickWithNature.class.getName(),
			sneer.bricks.software.bricks.finder.tests.fixtures.nature.SomeNature.class.getName()
		);
	}
	
}
