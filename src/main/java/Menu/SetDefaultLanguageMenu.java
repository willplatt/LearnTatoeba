package Menu;

import FileHandling.DefaultsFileManager;
import Language.Language;
import Terminal.Terminal;

import java.io.IOException;

import static Language.LanguageManager.getLanguage;
import static FileHandling.SentencesDirManager.hasFileForLanguage;

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
