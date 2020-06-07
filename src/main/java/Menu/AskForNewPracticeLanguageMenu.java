package Menu;

import Account.Account;
import Language.Language;

import java.io.IOException;

import static Language.LanguageManager.getLanguage;

public class AskForNewPracticeLanguageMenu extends Menu {
	private Account account;
	private Menu previousMenu;
	private Menu nextMenu;
	
	public AskForNewPracticeLanguageMenu(Account account, Menu previousMenu, Menu nextMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
		this.nextMenu = nextMenu;
	}
	
	@Override
	public void run() {
		askUserAQuestion("\nSpecify the new language you would like to practice:",
				previousMenu::run,
				languageName -> {
					try {
						Language newLanguage = getLanguage(languageName);
						AddPracticeLanguageMenu addPracticeLanguageMenu = new AddPracticeLanguageMenu(account, newLanguage, nextMenu);
						new IfNecessaryDownloadSentencesMenu(newLanguage, this, addPracticeLanguageMenu).run();
					} catch (IllegalArgumentException e) {
						System.err.println(e.getMessage());
						run();
					} catch (IOException e) {
						e.printStackTrace();
						previousMenu.run();
					}
				}
		);
	}
}
