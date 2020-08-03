package Menu;

import Migration.FullMigration;
import Terminal.Terminal;

import static Constants.Constants.VERSION;

public class MigrationMenu extends Menu {
	@Override
	public void run() {
		askUserAYesNoQuestion("\nIt looks like you have data from LearnTatoeba version " + FullMigration.lastUsedVersion + ". To continue, we'll have to migrate your data to version " + VERSION + ".\n" +
						"Do you want to continue? It is recommended you make a copy of the accounts directory first in case something goes wrong.",
				() -> {
					Terminal.println("You can't go back from here!");
					run();
				},
				() -> {
					Terminal.println("Okay! Come back when you're ready to migrate.");
					System.exit(0);
				},
				() -> {
					boolean migrationSuccessful = FullMigration.migrate();
					if (migrationSuccessful) {
						Terminal.println("Migration complete.");
					} else {
						Terminal.println("Something went wrong during migration. Terminating program.");
						System.exit(0);
					}
				}
		);
	}
}
