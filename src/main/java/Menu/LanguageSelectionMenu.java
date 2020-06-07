package Menu;

import Account.Account;
import Language.Language;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static Account.AccountManager.getLanguagesForAccount;

public class LanguageSelectionMenu extends Menu {
	private Account account;
	private Menu previousMenu;
	
	public LanguageSelectionMenu(Account account, Menu previousMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
	}
	
	@Override
	public void run() {
		try {
			List<Language> languages = getLanguagesForAccount(account);
			List<String> languageNames = languages.stream().map(language -> language.getName()).collect(Collectors.toList());
			if (languages.isEmpty()) {
				System.out.println("You haven't added any languages to practice with yet!");
				previousMenu.run();
			} else {
				System.out.println("\nChoose a language to practice:");
				giveUserAChoice(languageNames,
						previousMenu::run,
						userChoice -> {
							int languageIndex = Integer.parseInt(userChoice) - 1;
							Language language = languages.get(languageIndex);
							PracticeMenu practiceMenu = new PracticeMenu(account, language, this);
							IfNecessaryDownloadSentencesMenu ifNecessaryDownloadNativeSentencesMenu = new IfNecessaryDownloadSentencesMenu(
									account.getNativeLanguage(), this, practiceMenu
							);
							new IfNecessaryDownloadSentencesMenu(language, this, ifNecessaryDownloadNativeSentencesMenu).run();
						}
				);
			}
		} catch (IOException e) {
			e.printStackTrace();
			previousMenu.run();
		}
	}
}
