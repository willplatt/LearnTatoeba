import java.io.IOException;
import java.util.List;

public class PracticeMenu extends Menu {
	private Account account;
	private String language;
	private Menu previousMenu;
	
	public PracticeMenu(Account account, String language, Menu previousMenu) {
		this.account = account;
		this.language = language;
		this.previousMenu = previousMenu;
	}
	
	@Override
	void run() {
		System.out.println("\nYou will now be presented with sentences in " + language + " until you type 'back' or 'exit'.");
		System.out.println("After reading a sentence, you can just press enter to move on to the next, or type something before pressing enter and you will be shown the translation.");
		SentenceChooser sentenceChooser = null;
		try {
			sentenceChooser = new SentenceChooser(account, language);
			doAnotherSentence(sentenceChooser);
		} catch (IOException e) {
			if (sentenceChooser != null) {
				sentenceChooser.close();
			}
			e.printStackTrace();
			previousMenu.run();
		}
	}
	
	private void doAnotherSentence(SentenceChooser sentenceChooser) throws IOException {
		String sentence = sentenceChooser.getNextSentence();
		if (sentence == null) {
			System.out.println("Wow! You've been through all of the sentences! Take a well-deserved break.");
			previousMenu.run();
		} else {
			List<String> translations = sentenceChooser.getNextTranslations();
			System.out.println("\n\t" + sentence);
			String nextSentenceOrTranslate = getNextLine();
			if (nextSentenceOrTranslate.toLowerCase().equals("exit")) {
				sentenceChooser.close();
				System.exit(0);
			} else if (nextSentenceOrTranslate.toLowerCase().equals("back")) {
				sentenceChooser.close();
				previousMenu.run();
			} else if (nextSentenceOrTranslate.equals("")) {
				doAnotherSentence(sentenceChooser);
			} else {
				for (String translation : translations) {
					System.out.println("\t" + translation);
				}
				String userInput = getNextLine();
				if (userInput.toLowerCase().equals("exit")) {
					sentenceChooser.close();
					System.exit(0);
				} else if (userInput.toLowerCase().equals("back")) {
					sentenceChooser.close();
					previousMenu.run();
				} else {
					doAnotherSentence(sentenceChooser);
				}
			}
		}
	}
}
