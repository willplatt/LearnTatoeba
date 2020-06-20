package Menu;

import Account.AccountManager;
import Terminal.Terminal;

import java.io.IOException;

import static Language.SentencesDirManager.downloadFileForLanguage;
import static Language.SentencesDirManager.downloadSentenceLinks;

public class DownloadDefaultLanguageSentencesMenu extends Menu {
	@Override
	public void run() {
		askUserAYesNoQuestion("\nLooks like we need to download the " + AccountManager.getDefaultLanguage().getName() + " sentences and (if you don't already have it) the Tatoeba links file for you. Do you want to continue?",
				() -> {
					Terminal.println("You can't go back from here!");
					run();
				},
				() -> {
					Terminal.println("Okay! Come back when you're ready to download.");
					System.exit(0);
				},
				this::tryToDownloadAndExtractFiles
		);
	}
	
	private void tryToDownloadAndExtractFiles() {
		Terminal.println("Downloading and extracting...");
		try {
			downloadFileForLanguage(AccountManager.getDefaultLanguage());
			Terminal.println("Completed!");
			new MainMenu().run();
		} catch (IOException e) {
			System.err.println("Something went wrong:");
			e.printStackTrace();
			Terminal.println("Terminating.");
			System.exit(0);
		}
	}
}
