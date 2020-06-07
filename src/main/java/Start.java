import Account.AccountManager;
import Language.Language;
import Language.SentencesDirManager;
import Menu.DownloadDefaultLanguageSentencesMenu;
import Menu.MainMenu;
import Menu.SetDefaultLanguageMenu;

import java.io.IOException;

public class Start {
    public static void main(String[] args) throws IOException {
        System.out.println(
                "Welcome to Learn Tatoeba!\n" +
                "If you ever want to go back to the previous menu or exit this program you can type 'back' or 'exit'."
        );
        AccountManager.loadAccounts();
        Language defaultLanguage = AccountManager.getDefaultLanguage();
        if (defaultLanguage != null) {
            if (SentencesDirManager.hasFileForLanguage(defaultLanguage)) {
                new MainMenu().run();
            } else {
                new DownloadDefaultLanguageSentencesMenu().run();
            }
        } else {
            new SetDefaultLanguageMenu().run();
        }
    }
}
