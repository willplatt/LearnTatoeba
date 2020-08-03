package FileHandling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.NoSuchElementException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KeyValueFileManager {
	public static String readValueFromFile(File file, String desiredKey) {
		if (file.exists() && file.isFile()) {
			try {
				List<String> lines = Files.readAllLines(file.toPath(), UTF_8);
				for (String line : lines) {
					String[] keyAndValue = line.split("\t");
					if (keyAndValue[0].equals(desiredKey)) {
						return keyAndValue[1];
					}
				}
				throw new NoSuchElementException("File " + file.getPath() + " has no entry with key '" + desiredKey + "'.");
			} catch (IOException | IndexOutOfBoundsException | NoSuchElementException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
