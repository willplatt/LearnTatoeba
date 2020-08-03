package Migration;

import Menu.MigrationMenu;

import java.util.List;

import static Account.AccountManager.readVersion;
import static Migration.MigrationTo0_1_0dev.DEFAULT_LANGUAGE_FILE;

public class FullMigration {
	private static final List<String> PREVIOUS_VERSIONS = List.of("0.1.0");
	
	public static String lastUsedVersion;
	
	public static void migrateIfNecessary() {
		lastUsedVersion = getLastUsedVersion();
		if (lastUsedVersion != null && PREVIOUS_VERSIONS.contains(lastUsedVersion)) {
			new MigrationMenu().run();
		}
	}
	
	public static boolean migrate() {
		switch (lastUsedVersion) {
			case "0.1.0":
				return MigrationTo0_1_0dev.migrate();
			default:
				return false;
		}
	}
	
	private static String getLastUsedVersion() {
		String versionFromDefaultsFile = readVersion();
		if (versionFromDefaultsFile == null && DEFAULT_LANGUAGE_FILE.exists() && DEFAULT_LANGUAGE_FILE.isFile()) {
			return "0.1.0";
		} else {
			return versionFromDefaultsFile;
		}
	}
}
