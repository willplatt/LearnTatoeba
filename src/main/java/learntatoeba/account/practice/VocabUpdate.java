package learntatoeba.account.practice;

import java.util.Set;

public class VocabUpdate {
	private static final Set<String> VALID_STATUSES = Set.of("0", "1", "2", "3", "4", "5", "8", "9");
	
	private final String phrase;
	private final int status;
	private final String sentenceFragment;
	private final String translation;
	private final String romanization;
	
	public VocabUpdate(String phrase, String status, String sentenceFragment, String translation, String romanization) {
		if (phrase.contains("\t") || !VALID_STATUSES.contains(status) ||
				(translation != null && translation.contains("\t")) ||
				(romanization != null && romanization.contains("\t")) ||
				((translation != null || romanization != null) && status.equals("0"))) {
			throw new IllegalArgumentException("Illegal constructor parameters for VocabUpdate");
		}
		this.phrase = phrase;
		this.status = Integer.parseInt(status);
		this.sentenceFragment = sentenceFragment;
		this.translation = translation;
		this.romanization = romanization;
	}
	
	public String getPhrase() {
		return phrase;
	}
	
	public int getStatus() {
		return status;
	}
	
	public String getSentenceFragment() {
		return sentenceFragment;
	}
	
	public String getTranslation() {
		return translation;
	}
	
	public String getRomanization() {
		return romanization;
	}
	
	public VocabUpdate mergeWithNewerUpdate(VocabUpdate newUpdate) {
		String newPhrase = newUpdate.getPhrase();
		String newStatus = String.valueOf(newUpdate.getStatus());
		String newSentenceFragment = newUpdate.getSentenceFragment() == null ? sentenceFragment : newUpdate.getSentenceFragment();
		String newTranslation = newUpdate.getTranslation() == null ? translation : newUpdate.getTranslation();
		String newRomanization = newUpdate.getRomanization() == null ? romanization : newUpdate.getRomanization();
		return new VocabUpdate(newPhrase, newStatus, newSentenceFragment, newTranslation, newRomanization);
	}
}
