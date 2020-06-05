package Menu;

import Account.AccountManager;

import java.io.IOException;

import static Language.SentencesDirManager.downloadFileForLanguage;
import static Language.SentencesDirManager.downloadSentenceLinks;

public class DownloadDefaultLanguageSentencesMenu extends Menu {
	@Override
	public void run() {
		askUserAYesNoQuestion("\nLooks like we need to download the " + AccountManager.getDefaultLanguage() + " sentences and the Tatoeba links file for you. Do you want to continue?",
				() -> {
					System.out.println("You can't go back from here!");
					run();
				},
				() -> {
					System.out.println("Okay! Come back when you're ready to download.");
					System.exit(0);
				},
				this::tryToDownloadAndExtractFiles
		);
	}
	
	private void tryToDownloadAndExtractFiles() {
		System.out.println("Downloading and extracting...");
		try {
			downloadSentenceLinks();
			downloadFileForLanguage(AccountManager.getDefaultLanguage());
			System.out.println("Completed!");
			new MainMenu().run();
		} catch (IOException e) {
			System.err.println("Something went wrong:");
			e.printStackTrace();
			System.out.println("Terminating.");
			System.exit(0);
		}
	}
}
