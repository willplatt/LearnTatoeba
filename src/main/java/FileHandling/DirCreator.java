package FileHandling;

import java.io.File;

public class DirCreator {
	public static boolean createDirIfNecessary(File dir) {
		if (dir.exists() && dir.isDirectory()) {
			return true;
		}
		return dir.mkdir();
	}
}
