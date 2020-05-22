import java.io.IOException;

public class SetDefaultLanguageMenu extends Menu {
	@Override
	void run() {
		String defaultLanguage = askUserAQuestion("\nSpecify the native language for accounts on this machine. The native language can be set independently for each account, but this will be the default:");
		if (defaultLanguage.toLowerCase().equals("exit")) {
			System.exit(0);
		} else if (defaultLanguage.toLowerCase().equals("back")) {
			System.out.println("You can't go back from here!");
			run();
		} else {
			try {
				AccountManager.setDefaultLanguage(defaultLanguage);
				if (SentencesDirManager.hasFileForLanguage(defaultLanguage)) {
					new MainMenu().run();
				} else {
					new DownloadDefaultLanguageSentencesMenu().run();
				}
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
				run();
			} catch (IOException e) {
				System.err.println("Something went wrong:");
				e.printStackTrace();
				System.out.println("Default language not set. Terminating.");
				System.exit(0);
			}
		}
	}
}
