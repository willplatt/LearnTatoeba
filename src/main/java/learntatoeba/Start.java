package learntatoeba;

import learntatoeba.account.AccountManager;
import learntatoeba.defaults.DefaultsFileManager;
import learntatoeba.defaults.DownloadDefaultLanguageSentencesMenu;
import learntatoeba.defaults.SetDefaultLanguageMenu;
import learntatoeba.language.Language;
import learntatoeba.migration.FullMigration;

import java.io.IOException;

import static learntatoeba.Constants.VERSION;

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
