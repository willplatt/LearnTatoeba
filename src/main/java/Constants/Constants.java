package Constants;

import Language.BlacklistDuration;

import java.io.File;

public class Constants {
	public static final String VERSION = "0.1.0dev";
	public static final File INSTALL_DIR = new File(String.valueOf(Thread.currentThread().getContextClassLoader().getResource(".")).substring(5));
	public static final BlacklistDuration DEFAULT_AUTOBLACKLIST_DURATION = new BlacklistDuration("0");
	public static final double DEFAULT_RECURRENCE_PROBABILITY = 0.1;
	public static final int DEFAULT_SESSION_LENGTH = 50;
}
