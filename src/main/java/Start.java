import Account.AccountManager;
import FileHandling.DefaultsFileManager;
import Language.Language;
import FileHandling.SentencesDirManager;
import Menu.DownloadDefaultLanguageSentencesMenu;
import Menu.MainMenu;
import Menu.SetDefaultLanguageMenu;
import Migration.FullMigration;
import Terminal.Terminal;

import java.io.IOException;

import static Constants.Constants.VERSION;

public class Start {
    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].toLowerCase().equals("bidi")) {
            Terminal.activateBidiEmulation();
        }
        Terminal.println(
                "Welcome to LearnTatoeba! (Version " + VERSION + ")\n" +
                "If you ever want to go back to the previous menu or exit this program you can type 'back' or 'exit'."
        );
        FullMigration.migrateIfNecessary();
        continueToNextMenu();
    }
    
    private static void continueToNextMenu() throws IOException {
        AccountManager.loadAccounts();
        Language defaultLanguage = DefaultsFileManager.getDefaultLanguage();
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
