package Constants;

import java.io.File;

public class Constants {
	public static final File INSTALL_DIR = new File(String.valueOf(Thread.currentThread().getContextClassLoader().getResource(".")).substring(5));
	public static final int DEFAULT_AUTOBLACKLIST_DURATION = 0;
}