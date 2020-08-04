package learntatoeba.defaults;

import learntatoeba.MainMenu;
import learntatoeba.Menu;
import learntatoeba.language.Language;
import learntatoeba.Terminal;

import java.io.IOException;

import static learntatoeba.language.LanguageManager.getLanguage;
import static learntatoeba.SentencesDirManager.hasFileForLanguage;

public class SetDefaultLanguageMenu extends Menu {
	@Override
	public void run() {
		askUserAQuestion("\nSpecify the native language for accounts on this machine. The native language can be set independently for each account, but this will be the default:",
				() -> {
					Terminal.println("You can't go back from here!");
					run();
				},
				languageName -> {
					try {
						Language language = getLanguage(languageName);
						DefaultsFileManager.setDefaultLanguage(language);
						if (hasFileForLanguage(language)) {
							new MainMenu().run();
						} else {
							new DownloadDefaultLanguageSentencesMenu().run();
						}
					} catch (IllegalArgumentException e) {
						System.err.println(e.getMessage());
						run();
					} catch (IOException e) {
						System.err.println("Something went wrong:");
						e.printStackTrace();
						Terminal.println("Default language not set. Terminating.");
						System.exit(0);
					}
				}
		);
	}
}
