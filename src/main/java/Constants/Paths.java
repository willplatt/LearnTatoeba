package Constants;

import java.io.File;

public class Paths {
	public static final File INSTALL_DIR = new File(String.valueOf(Thread.currentThread().getContextClassLoader().getResource(".")).substring(5));
}
