package sneer.bricks.hardwaresharing.files.map.visitors.impl;

import static sneer.foundation.environments.Environments.my;
import sneer.bricks.hardwaresharing.files.map.FileMap;
import sneer.bricks.hardwaresharing.files.map.visitors.FolderStructureVisitor;
import sneer.bricks.hardwaresharing.files.protocol.FileOrFolder;
import sneer.bricks.hardwaresharing.files.protocol.FolderContents;
import sneer.bricks.pulp.crypto.Sneer1024;

class GuidedTour {
	
	private final FolderStructureVisitor _visitor;

	GuidedTour(Sneer1024 startingPoint, FolderStructureVisitor visitor) {
		_visitor = visitor;
		showContents(startingPoint);
	}


	private void showContents(Sneer1024 hashOfContents) {
		Object contents = my(FileMap.class).getContents(hashOfContents);
		if (contents == null) throw new IllegalStateException("Contents not found in " + FileMap.class.getSimpleName());
		
		if (contents instanceof FolderContents){
			showFolder((FolderContents)contents);
			return;
		}
		
		if (contents instanceof byte[]){
			showFile((byte[])contents);
			return;
		}
		
		throw new IllegalStateException("Can't show contents for type " + contents.getClass());
	}

	private void showFile(byte[] contents) {
		_visitor.visitFileContents(contents);
	}
	

	private void showFolder(FolderContents folderContents) {
		_visitor.enterFolder();
			
		for (FileOrFolder fileOrFolder : folderContents.contents)
			showFileOrFolder(fileOrFolder);

		_visitor.leaveFolder();
	}


	private void showFileOrFolder(FileOrFolder fileOrFolder) {
		if (shouldVisit(fileOrFolder))
			showContents(fileOrFolder.hashOfContents);
	}


	private boolean shouldVisit(FileOrFolder fileOrFolder) {
		return _visitor.visitFileOrFolder(
			fileOrFolder.name,
			fileOrFolder.lastModified,
			fileOrFolder.hashOfContents);
	}

}