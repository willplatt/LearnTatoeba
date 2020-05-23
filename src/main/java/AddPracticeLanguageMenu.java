import java.io.IOException;
import java.util.List;

public class AddPracticeLanguageMenu extends Menu {
	private Account account;
	private Menu previousMenu;
	private Menu nextMenu;
	
	public AddPracticeLanguageMenu(Account account, Menu previousMenu, Menu nextMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
		this.nextMenu = nextMenu;
	}
	
	@Override
	void run() {
		String newLanguage = askUserAQuestion("\nSpecify the new language you would like to practice:");
		if (newLanguage.toLowerCase().equals("exit")) {
			System.exit(0);
		} else if (newLanguage.toLowerCase().equals("back")) {
			previousMenu.run();
		} else {
			String canonicalLanguageName = LanguageCodeHandler.getCanonicalName(newLanguage);
			if (canonicalLanguageName == null) {
				System.out.println("That language is not recognised. Make sure you typed it correctly.");
				run();
			} else {
				try {
					setLanguageAndDownloadFilesIfNecessary(canonicalLanguageName);
				} catch (IOException e) {
					System.err.println("Something went wrong:");
					e.printStackTrace();
					System.out.println("Default language not set. Terminating.");
					System.exit(0);
				}
			}
		}
	}
	
	private void setLanguageAndDownloadFilesIfNecessary(String newLanguage) throws IOException {
		newLanguage = LanguageCodeHandler.getCanonicalName(newLanguage);
		if (!SentencesDirManager.hasFileForLanguage(newLanguage)) {
			String downloadFile = askUserAQuestion("Looks like we need to download the " + newLanguage + " sentences for you. Do you still want to continue?");
			downloadFile = downloadFile.toLowerCase();
			if (downloadFile.equals("exit")) {
				System.out.println("Language was not added.");
				System.exit(0);
			} else if (downloadFile.equals("back")) {
				System.out.println("Language was not added.");
				previousMenu.run();
			} else if (List.of("no", "n").contains(downloadFile)) {
				System.out.println("Language was not added.");
				nextMenu.run();
			} else if (!List.of("yes", "y").contains(downloadFile)) {
				setLanguageAndDownloadFilesIfNecessary(newLanguage);
			} else {
				System.out.println("Downloading and extracting...");
				SentencesDirManager.downloadFileForLanguage(newLanguage);
				System.out.println("Completed!");
				AccountManager.addPracticeLanguageToAccount(account, newLanguage);
				nextMenu.run();
			}
		} else {
			AccountManager.addPracticeLanguageToAccount(account, newLanguage);
			System.out.println("You can now practice " + newLanguage + "!");
			nextMenu.run();
		}
	}
}
