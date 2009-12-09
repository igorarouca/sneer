package sneer.bricks.software.folderconfig;

import java.io.File;

import sneer.bricks.hardware.ram.ref.immutable.Immutable;
import sneer.foundation.brickness.Brick;

@Brick
public interface FolderConfig {

	Immutable<File> ownSrcFolder();
	Immutable<File> ownBinFolder();

	Immutable<File> srcFolder();
	Immutable<File> binFolder();

	Immutable<File> storageFolder();
	File storageFolderFor(Class<?> brick);

	Immutable<File> tmpFolder();
	File tmpFolderFor(Class<?> brick);

	Immutable<File> stageFolder();

}