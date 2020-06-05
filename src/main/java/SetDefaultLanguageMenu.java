import java.io.IOException;

public class SetDefaultLanguageMenu extends Menu {
	@Override
	void run() {
		askUserAQuestion("\nSpecify the native language for accounts on this machine. The native language can be set independently for each account, but this will be the default:",
				() -> {
					System.out.println("You can't go back from here!");
					run();
				},
				defaultLanguage -> {
					String canonicalLanguageName = LanguageCodeHandler.getCanonicalName(defaultLanguage);
					if (canonicalLanguageName == null) {
						System.out.println("That language is not recognised. Make sure you typed it correctly.");
						run();
					} else {
						try {
							AccountManager.setDefaultLanguage(canonicalLanguageName);
							if (SentencesDirManager.hasFileForLanguage(canonicalLanguageName)) {
								new MainMenu().run();
							} else {
								new DownloadDefaultLanguageSentencesMenu().run();
							}
						} catch (IOException e) {
							System.err.println("Something went wrong:");
							e.printStackTrace();
							System.out.println("Default language not set. Terminating.");
							System.exit(0);
						}
					}
				}
		);
	}
}
