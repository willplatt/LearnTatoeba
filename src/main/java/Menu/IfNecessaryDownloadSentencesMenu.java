package Menu;

import java.io.IOException;

import static Language.SentencesDirManager.downloadFileForLanguage;
import static Language.SentencesDirManager.hasFileForLanguage;

public class IfNecessaryDownloadSentencesMenu extends Menu {
	private String language;
	private Menu previousMenu;
	private Menu nextMenu;
	
	public IfNecessaryDownloadSentencesMenu(String language, Menu previousMenu, Menu nextMenu) {
		this.language = language;
		this.previousMenu = previousMenu;
		this.nextMenu = nextMenu;
	}
	
	@Override
	public void run() {
		try {
			if (hasFileForLanguage(language)) {
				nextMenu.run();
			} else {
				askToDownloadAndContinue();
			}
		} catch (IOException e) {
			System.out.println("Something went wrong:");
			e.printStackTrace();
			System.out.println("Returning to the previous menu.");
			previousMenu.run();
		}
	}
	
	private void askToDownloadAndContinue() {
		askUserAYesNoQuestion("\nIt looks like you don't have the sentences for " + language + ". If you wish to continue, they will be downloaded now. The links file will also be downloaded if you do not have it. Continue?",
				previousMenu::run,
				() -> {
					try {
						System.out.println("Downloading and extracting...");
						downloadFileForLanguage(language);
						System.out.println("Completed!");
						nextMenu.run();
					} catch (IOException e) {
						System.out.println("Something went wrong:");
						e.printStackTrace();
						System.out.println("Returning to the previous menu.");
						previousMenu.run();
					}
				}
		);
	}
}
