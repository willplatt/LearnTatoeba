package learntatoeba.migration;

import learntatoeba.Menu;
import learntatoeba.Terminal;

import static learntatoeba.Constants.VERSION;

public class MigrationMenu extends Menu {
	@Override
	public void run() {
		askUserAYesNoQuestion("\nIt looks like you have data from LearnTatoeba version " + FullMigration.lastUsedVersion + ". To continue, we'll have to migrate your data to version " + VERSION + ".\n" +
						"Do you want to continue? A backup of your data will be made in case the migration fails.",
				() -> {
					Terminal.println("You can't go back from here!");
					run();
				},
				() -> {
					Terminal.println("Okay! Come back when you're ready to migrate.");
					System.exit(0);
				},
				() -> {
					boolean migrationSuccessful = FullMigration.backupAndMigrate();
					if (!migrationSuccessful) {
						Terminal.println("Something went wrong during migration. Terminating program.");
						System.exit(0);
					}
				}
		);
	}
}
