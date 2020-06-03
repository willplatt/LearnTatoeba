import java.io.IOException;
import java.util.List;

public class DownloadDefaultLanguageSentencesMenu extends Menu {
	@Override
	void run() {
		String downloadFile = askUserAQuestion("\nLooks like we need to download the " + AccountManager.getDefaultLanguage() + " sentences and the Tatoeba links file for you. Do you want to continue?");
		downloadFile = downloadFile.toLowerCase();
		if (downloadFile.equals("exit")) {
			System.exit(0);
		} else if (downloadFile.equals("back")) {
			System.out.println("You can't go back from here!");
			run();
		} else if (List.of("no", "n").contains(downloadFile)) {
			System.out.println("Okay! Come back when you're ready to download.");
			System.exit(0);
		} else if (List.of("yes", "y").contains(downloadFile)) {
			tryToDownloadAndExtractFiles();
		} else {
			System.out.println("Please type yes or no.");
			run();
		}
	}
	
	private void tryToDownloadAndExtractFiles() {
		System.out.println("Downloading and extracting...");
		try {
			SentencesDirManager.downloadSentenceLinks();
			SentencesDirManager.downloadFileForLanguage(AccountManager.getDefaultLanguage());
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
