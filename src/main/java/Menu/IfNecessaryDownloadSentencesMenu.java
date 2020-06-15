package Menu;

import Language.Language;
import Terminal.Terminal;

import java.io.IOException;

import static Language.SentencesDirManager.downloadFileForLanguage;
import static Language.SentencesDirManager.hasFileForLanguage;

public class IfNecessaryDownloadSentencesMenu extends Menu {
	private Language language;
	private Menu previousMenu;
	private Menu nextMenu;
	
	public IfNecessaryDownloadSentencesMenu(Language language, Menu previousMenu, Menu nextMenu) {
		this.language = language;
		this.previousMenu = previousMenu;
		this.nextMenu = nextMenu;
	}
	
	@Override
	public void run() {
		if (hasFileForLanguage(language)) {
			nextMenu.run();
		} else {
			askToDownloadAndContinue();
		}
	}
	
	private void askToDownloadAndContinue() {
		askUserAYesNoQuestion("\nIt looks like you don't have the sentences for " + language.getName() + ". If you wish to continue, they will be downloaded now. The links file will also be downloaded if you do not have it. Continue?",
				previousMenu::run,
				() -> {
					try {
						Terminal.println("Downloading and extracting...");
						downloadFileForLanguage(language);
						Terminal.println("Completed!");
						nextMenu.run();
					} catch (IOException e) {
						Terminal.println("Something went wrong:");
						e.printStackTrace();
						Terminal.println("Returning to the previous menu.");
						previousMenu.run();
					}
				}
		);
	}
}
