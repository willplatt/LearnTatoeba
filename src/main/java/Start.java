public class Start {
    public static void main(String[] args) {
        System.out.println(
                "Welcome to spaced repetition!\n" +
                "If you ever want to go back to the previous menu or exit this program you can type 'back' or 'exit'."
        );
        AccountManager.loadAccounts();
        new MainMenu().run();
    }
}
