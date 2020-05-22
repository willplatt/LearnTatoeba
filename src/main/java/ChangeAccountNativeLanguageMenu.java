import java.io.IOException;
import java.util.List;

public class ChangeAccountNativeLanguageMenu extends Menu {
	private Account account;
	private Menu previousMenu;
	private Menu nextMenu;
	
	public ChangeAccountNativeLanguageMenu(Account account, Menu previousMenu, Menu nextMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
		this.nextMenu = nextMenu;
	}
	
	@Override
	void run() {
		String continueChanging = askUserAQuestion("\nThe current native language of this account is '" + account.getNativeLanguage() + "'. Would you like to change it?");
		continueChanging = continueChanging.toLowerCase();
		if (continueChanging.equals("exit")) {
			System.exit(0);
		} else if (continueChanging.equals("back")) {
			previousMenu.run();
		} else if (List.of("no", "n").contains(continueChanging)) {
			nextMenu.run();
		} else if (!List.of("yes", "y").contains(continueChanging)) {
			run();
		} else {
			getAndSetNewLanguage();
		}
	}
	
	private void getAndSetNewLanguage() {
		String newNativeLanguage = askUserAQuestion("Specify the new native language for this account:");
		if (newNativeLanguage.toLowerCase().equals("exit")) {
			System.exit(0);
		} else if (newNativeLanguage.toLowerCase().equals("back")) {
			previousMenu.run();
		} else {
			String canonicalLanguageName = LanguageCodeHandler.getCanonicalName(newNativeLanguage);
			if (canonicalLanguageName == null) {
				System.out.println("That language is not recognised. Make sure you typed it correctly.");
				getAndSetNewLanguage();
			} else {
				try {
					setLanguageAndDownloadFilesIfNecessary(canonicalLanguageName);
				} catch (IOException e) {
					System.err.println("Something went wrong:");
					e.printStackTrace();
					System.out.println("Native language not changed.");
					nextMenu.run();
				}
			}
		}
	}
	
	private void setLanguageAndDownloadFilesIfNecessary(String newNativeLanguage) throws IOException {
		if (!SentencesDirManager.hasFileForLanguage(newNativeLanguage)) {
			String downloadFile = askUserAQuestion("Looks like we need to download the " + newNativeLanguage + " sentences for you. Do you still want to continue?");
			downloadFile = downloadFile.toLowerCase();
			if (downloadFile.equals("exit")) {
				System.exit(0);
			} else if (downloadFile.equals("back")) {
				previousMenu.run();
			} else if (List.of("no", "n").contains(downloadFile)) {
				nextMenu.run();
			} else if (!List.of("yes", "y").contains(downloadFile)) {
				run();
			} else {
				System.out.println("Downloading and extracting...");
				SentencesDirManager.downloadFileForLanguage(newNativeLanguage);
				setNativeLanguageAndContinue(newNativeLanguage);
			}
		} else {
			setNativeLanguageAndContinue(newNativeLanguage);
		}
	}
	
	private void setNativeLanguageAndContinue(String newNativeLanguage) {
		AccountManager.setNativeLanguage(account, newNativeLanguage);
		System.out.println("Your account's native language has been changed!");
		nextMenu.run();
	}
}
