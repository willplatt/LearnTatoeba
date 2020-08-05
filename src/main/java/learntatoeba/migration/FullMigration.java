package learntatoeba.migration;

import learntatoeba.Terminal;
import learntatoeba.account.AccountDirManager;

import java.io.File;
import java.util.List;

import static learntatoeba.Constants.INSTALL_DIR;
import static learntatoeba.defaults.DefaultsFileManager.readVersion;
import static learntatoeba.migration.MigrationTo0_1_0dev.DEFAULT_LANGUAGE_FILE;

public class FullMigration {
	private static final List<String> PREVIOUS_VERSIONS = List.of("0.1.0");
	private static final File ACCOUNTS_DIR_BACKUP = new File(INSTALL_DIR, "accounts-backup");
	
	public static String lastUsedVersion;
	
	public static void migrateIfNecessary() {
		lastUsedVersion = getLastUsedVersion();
		if (lastUsedVersion != null && PREVIOUS_VERSIONS.contains(lastUsedVersion)) {
			new MigrationMenu().run();
		}
	}
	
	public static boolean backupAndMigrate() {
		Terminal.println("Backing up accounts directory to \"" + ACCOUNTS_DIR_BACKUP.getPath() + "\".");
		boolean backupSuccessful = AccountDirManager.backupAccountsDir(ACCOUNTS_DIR_BACKUP);
		if (!backupSuccessful) {
			System.err.println("Could not copy accounts directory to \"" + ACCOUNTS_DIR_BACKUP.getPath() + "\".");
			return false;
		}
		Terminal.println("Migrating data.");
		boolean migrationSuccessful = migrate();
		if (migrationSuccessful) {
			Terminal.println("Migration successful. Once you have ensured things are working correctly, you may delete the backup directory \"" + ACCOUNTS_DIR_BACKUP.getPath() + "\".");
			return true;
		} else {
			System.err.println("Migration unsuccessful. Restoring backup from \"" + ACCOUNTS_DIR_BACKUP.getPath() + "\".");
			boolean restorationSuccessful = AccountDirManager.restoreBackupAccountsDir(ACCOUNTS_DIR_BACKUP);
			if (!restorationSuccessful) {
				System.err.println("Restoring backup failed.");
			} else {
				Terminal.println("Accounts directory restored from backup. Please fix the issues and try migrating again.");
			}
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
	
	private static boolean migrate() {
		switch (lastUsedVersion) {
			case "0.1.0":
				return MigrationTo0_1_0dev.migrate();
			default:
				return false;
		}
	}
}
