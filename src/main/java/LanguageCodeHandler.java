import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class LanguageCodeHandler {
	private static final File LANGUAGE_CODES_FILE = new File("language_codes.tsv");
	
	public static String getCodeForLanguage(String language) throws IOException {
		BufferedReader codesReader = Files.newBufferedReader(LANGUAGE_CODES_FILE.toPath());
		String line;
		while ((line = codesReader.readLine()) != null) {
			int indexOfTab = line.indexOf('\t');
			if (language.toLowerCase().equals(line.substring(indexOfTab + 1).toLowerCase())) {
				codesReader.close();
				return line.substring(0, indexOfTab);
			}
		}
		codesReader.close();
		throw new IllegalArgumentException("Language '" + language + "' does not have a Tatoeba language code");
	}
}
