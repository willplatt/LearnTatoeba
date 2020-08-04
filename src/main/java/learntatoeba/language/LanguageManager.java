package learntatoeba.language;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static learntatoeba.Constants.INSTALL_DIR;
import static java.nio.charset.StandardCharsets.UTF_8;

public class LanguageManager {
	private static final File LANGUAGES_FILE = new File(INSTALL_DIR, "languages.tsv");
	
	public static Language getLanguage(String languageName) throws IOException {
		String canonicalLanguageName = getCanonicalName(languageName);
		if (canonicalLanguageName == null) {
			canonicalLanguageName = languageName;
		}
		try (BufferedReader propertiesReader = Files.newBufferedReader(LANGUAGES_FILE.toPath(), UTF_8)) {
			String line;
			while ((line = propertiesReader.readLine()) != null) {
				int indexOfFirstTab = line.indexOf('\t');
				if (line.substring(0, indexOfFirstTab).equals(canonicalLanguageName)) {
					return new Language(line);
				}
			}
		}
		throw new IllegalArgumentException("Language '" + languageName + "' is not recognised.");
	}
	
	private static String getCanonicalName(String languageName) {
		switch(languageName.toLowerCase()) {
			case "abkhaz":
			case "abkhazian":
				return "Abkhaz";
			case "adyghe":
				return "Adyghe";
			case "afrihili":
				return "Afrihili";
			case "afrikaans":
				return "Afrikaans";
			case "ainu":
				return "Ainu";
			case "aklanon":
				return "Aklanon";
			case "albanian":
				return "Albanian";
			case "algerian arabic":
			case "algerian":
				return "Algerian Arabic";
			case "amharic":
				return "Amharic";
			case "ancient greek":
				return "Ancient Greek";
			case "arabic":
				return "Arabic";
			case "aragonese":
				return "Aragonese";
			case "armenian":
				return "Armenian";
			case "assamese":
				return "Assamese";
			case "assyrian neo\u002daramaic":
			case "assyrian aramaic":
			case "assyrian":
				return "Assyrian Neo\u002dAramaic";
			case "asturian":
				return "Asturian";
			case "awadhi":
				return "Awadhi";
			case "aymara":
				return "Aymara";
			case "azerbaijani":
				return "Azerbaijani";
			case "balinese":
				return "Balinese";
			case "bambara":
				return "Bambara";
			case "banjar":
				return "Banjar";
			case "bashkir":
				return "Bashkir";
			case "basque":
				return "Basque";
			case "bavarian":
				return "Bavarian";
			case "baybayanon":
				return "Baybayanon";
			case "belarusian":
				return "Belarusian";
			case "bengali":
				return "Bengali";
			case "berber":
				return "Berber";
			case "bhojpuri":
				return "Bhojpuri";
			case "bislama":
				return "Bislama";
			case "bodo":
				return "Bodo";
			case "bosnian":
				return "Bosnian";
			case "breton":
				return "Breton";
			case "brithenig":
				return "Brithenig";
			case "bulgarian":
				return "Bulgarian";
			case "burmese":
				return "Burmese";
			case "buryat":
			case "buriat":
				return "Buryat";
			case "cantonese":
			case "yue chinese":
				return "Cantonese";
			case "catalan":
			case "valencian":
				return "Catalan";
			case "cayuga":
				return "Cayuga";
			case "cebuano":
				return "Cebuano";
			case "central bikol":
				return "Central Bikol";
			case "central dusun":
				return "Central Dusun";
			case "central huasteca nahuatl":
				return "Central Huasteca Nahuatl";
			case "central mnong":
				return "Central Mnong";
			case "chagatai":
				return "Chagatai";
			case "chamorro":
				return "Chamorro";
			case "chavacano":
				return "Chavacano";
			case "chechen":
				return "Chechen";
			case "cherokee":
				return "Cherokee";
			case "chinese pidgin english":
				return "Chinese Pidgin English";
			case "chinyanja":
			case "chichewa":
			case "chewa":
			case "nyanja":
				return "Chinyanja";
			case "choctaw":
				return "Choctaw";
			case "chukchi":
				return "Chukchi";
			case "chuvash":
				return "Chuvash";
			case "coastal kadazan":
				return "Coastal Kadazan";
			case "cornish":
				return "Cornish";
			case "corsican":
				return "Corsican";
			case "crimean tatar":
			case "crimean":
				return "Crimean Tatar";
			case "croatian":
				return "Croatian";
			case "cuyonon":
				return "Cuyonon";
			case "cycl":
				return "CycL";
			case "czech":
				return "Czech";
			case "danish":
				return "Danish";
			case "dhivehi":
			case "divehi":
			case "maldivian":
				return "Dhivehi";
			case "dungan":
				return "Dungan";
			case "dutch":
			case "flemish":
				return "Dutch";
			case "dutton world speedwords":
			case "dutton speedwords":
				return "Dutton World Speedwords";
			case "egyptian arabic":
			case "colloquial egyptian":
			case "masri":
				return "Egyptian Arabic";
			case "emilian":
				return "Emilian";
			case "english":
				return "English";
			case "erromintxela":
				return "Erromintxela";
			case "erzya":
				return "Erzya";
			case "esperanto":
				return "Esperanto";
			case "estonian":
				return "Estonian";
			case "evenki":
				return "Evenki";
			case "ewe":
				return "Ewe";
			case "extremaduran":
				return "Extremaduran";
			case "faroese":
				return "Faroese";
			case "fiji hindi":
				return "Fiji Hindi";
			case "fijian":
				return "Fijian";
			case "finnish":
				return "Finnish";
			case "french":
				return "French";
			case "frisian":
				return "Frisian";
			case "friulian":
				return "Friulian";
			case "ga":
				return "Ga";
			case "gagauz":
				return "Gagauz";
			case "galician":
				return "Galician";
			case "gan chinese":
				return "Gan Chinese";
			case "garhwali":
				return "Garhwali";
			case "georgian":
				return "Georgian";
			case "german":
				return "German";
			case "gheg albanian":
				return "Gheg Albanian";
			case "gilbertese":
				return "Gilbertese";
			case "gothic":
				return "Gothic";
			case "greek":
				return "Greek";
			case "greenlandic":
			case "kalaallisut":
				return "Greenlandic";
			case "gronings":
				return "Gronings";
			case "guadeloupean creole french":
				return "Guadeloupean Creole French";
			case "guarani":
				return "Guarani";
			case "guerrero nahuatl":
				return "Guerrero Nahuatl";
			case "gujarati":
				return "Gujarati";
			case "gulf arabic":
				return "Gulf Arabic";
			case "haitian creole":
			case "haitian":
				return "Haitian Creole";
			case "hakka chinese":
				return "Hakka Chinese";
			case "hausa":
				return "Hausa";
			case "hawaiian":
				return "Hawaiian";
			case "hebrew":
				return "Hebrew";
			case "hiligaynon":
				return "Hiligaynon";
			case "hill mari":
			case "western mari":
				return "Hill Mari";
			case "hindi":
				return "Hindi";
			case "hmong daw \u0028white\u0029":
			case "hmong daw":
			case "white hmong":
				return "Hmong Daw \u0028White\u0029";
			case "hmong njua \u0028green\u0029":
			case "hmong njua":
			case "green hmong":
			case "blue hmong":
				return "Hmong Njua \u0028Green\u0029";
			case "ho":
				return "Ho";
			case "hungarian":
				return "Hungarian";
			case "hunsrik":
				return "Hunsrik";
			case "iban":
				return "Iban";
			case "icelandic":
				return "Icelandic";
			case "ido":
				return "Ido";
			case "igbo":
				return "Igbo";
			case "ilocano":
				return "Ilocano";
			case "indonesian":
				return "Indonesian";
			case "ingrian":
				return "Ingrian";
			case "interlingua":
				return "Interlingua";
			case "interlingue":
			case "occidental":
				return "Interlingue";
			case "inuktitut":
				return "Inuktitut";
			case "iraqi arabic":
			case "mesopotamian arabic":
				return "Iraqi Arabic";
			case "irish":
				return "Irish";
			case "isan":
				return "Isan";
			case "italian":
				return "Italian";
			case "jamaican patois":
				return "Jamaican Patois";
			case "japanese":
				return "Japanese";
			case "javanese":
				return "Javanese";
			case "jewish babylonian aramaic":
				return "Jewish Babylonian Aramaic";
			case "jewish palestinian aramaic":
				return "Jewish Palestinian Aramaic";
			case "jin chinese":
				return "Jin Chinese";
			case "juhuri \u0028judeo\u002dtat\u0029":
			case "juhuri":
			case "judeo-tat":
				return "Juhuri \u0028Judeo\u002dTat\u0029";
			case "k\u0027iche\u0027":
			case "qatzijob'al":
			case "quiché":
			case "quiche":
				return "K\u0027iche\u0027";
			case "kabyle":
				return "Kabyle";
			case "kalmyk":
				return "Kalmyk";
			case "kamba":
				return "Kamba";
			case "kannada":
				return "Kannada";
			case "kapampangan":
				return "Kapampangan";
			case "karachay\u002dbalkar":
				return "Karachay\u002dBalkar";
			case "karakalpak":
				return "Karakalpak";
			case "karelian":
				return "Karelian";
			case "kashmiri":
				return "Kashmiri";
			case "kashubian":
				return "Kashubian";
			case "kazakh":
				return "Kazakh";
			case "kekchi \u0028q\u0027eqchi\u0027\u0029":
			case "kekchi":
			case "q'eqchi'":
				return "Kekchi \u0028Q\u0027eqchi\u0027\u0029";
			case "keningau murut":
			case "central murut":
				return "Keningau Murut";
			case "khakas":
				return "Khakas";
			case "khasi":
				return "Khasi";
			case "khmer":
				return "Khmer";
			case "kinyarwanda":
				return "Kinyarwanda";
			case "kirundi":
				return "Kirundi";
			case "klingon":
				return "Klingon";
			case "k\u00f6lsch":
			case "kolsch":
			case "koelsch":
			case "colognian":
				return "K\u00f6lsch";
			case "komi\u002dpermyak":
				return "Komi\u002dPermyak";
			case "komi\u002dzyrian":
				return "Komi\u002dZyrian";
			case "konkani \u0028goan\u0029":
			case "konkani":
			case "goan konkani":
				return "Konkani \u0028Goan\u0029";
			case "korean":
				return "Korean";
			case "kotava":
				return "Kotava";
			case "kumyk":
				return "Kumyk";
			case "kurdish":
				return "Kurdish";
			case "kven finnish":
				return "Kven Finnish";
			case "kyrgyz":
			case "kirghiz":
				return "Kyrgyz";
			case "l\u00e1adan":
			case "laaden":
				return "L\u00e1adan";
			case "ladin":
				return "Ladin";
			case "ladino":
				return "Ladino";
			case "lakota":
				return "Lakota";
			case "lao":
				return "Lao";
			case "latgalian":
				return "Latgalian";
			case "latin":
				return "Latin";
			case "latvian":
				return "Latvian";
			case "laz":
				return "Laz";
			case "ligurian":
				return "Ligurian";
			case "lingala":
				return "Lingala";
			case "lingua franca nova":
				return "Lingua Franca Nova";
			case "literary chinese":
				return "Literary Chinese";
			case "lithuanian":
				return "Lithuanian";
			case "livonian":
				return "Livonian";
			case "lojban":
				return "Lojban";
			case "lombard":
				return "Lombard";
			case "louisiana creole":
				return "Louisiana Creole";
			case "low german \u0028low saxon\u0029":
			case "low german":
			case "low saxon":
				return "Low German \u0028Low Saxon\u0029";
			case "lower sorbian":
				return "Lower Sorbian";
			case "luganda":
				return "Luganda";
			case "luxembourgish":
				return "Luxembourgish";
			case "macedonian":
				return "Macedonian";
			case "madurese":
				return "Madurese";
			case "maithili":
				return "Maithili";
			case "malagasy":
				return "Malagasy";
			case "malay":
				return "Malay";
			case "malay \u0028vernacular\u0029":
			case "vernacular malay":
			case "malay vernacular":
				return "Malay \u0028Vernacular\u0029";
			case "malayalam":
				return "Malayalam";
			case "maltese":
				return "Maltese";
			case "mambae":
				return "Mambae";
			case "manchu":
				return "Manchu";
			case "mandarin chinese":
			case "mandarin":
				return "Mandarin Chinese";
			case "manx":
				return "Manx";
			case "maori":
				return "Maori";
			case "marathi":
				return "Marathi";
			case "marshallese":
				return "Marshallese";
			case "meadow mari":
			case "eastern mari":
				return "Meadow Mari";
			case "mi\u0027kmaq":
			case "miꞌgmaq":
			case "micmac":
			case "lnu":
			case "miꞌkmaw":
			case "miꞌgmaw":
				return "Mi\u0027kmaq";
			case "middle english":
				return "Middle English";
			case "middle french":
				return "Middle French";
			case "min nan chinese":
				return "Min Nan Chinese";
			case "minangkabau":
				return "Minangkabau";
			case "mingrelian":
				return "Mingrelian";
			case "mirandese":
				return "Mirandese";
			case "mohawk":
				return "Mohawk";
			case "moksha":
				return "Moksha";
			case "mon":
				return "Mon";
			case "mongolian":
				return "Mongolian";
			case "morisyen":
				return "Morisyen";
			case "moroccan arabic":
				return "Moroccan Arabic";
			case "naga \u0028tangshang\u0029":
			case "tangsa":
			case "tangsa naga":
			case "tangshang naga":
				return "Naga \u0028Tangshang\u0029";
			case "nahuatl":
				return "Nahuatl";
			case "nauruan":
				return "Nauruan";
			case "navajo":
				return "Navajo";
			case "nepali":
				return "Nepali";
			case "newari":
				return "Newari";
			case "ngeq":
				return "Ngeq";
			case "nigerian fulfulde":
				return "Nigerian Fulfulde";
			case "niuean":
				return "Niuean";
			case "nogai":
				return "Nogai";
			case "north frisian":
				return "North Frisian";
			case "north levantine arabic":
				return "North Levantine Arabic";
			case "north moluccan malay":
				return "North Moluccan Malay";
			case "northern sami":
				return "Northern Sami";
			case "norwegian bokm\u00e5l":
			case "norwegian bokmal":
			case "bokmal":
				return "Norwegian Bokm\u00e5l";
			case "norwegian nynorsk":
			case "nynorsk":
				return "Norwegian Nynorsk";
			case "novial":
				return "Novial";
			case "nyungar":
				return "Nyungar";
			case "occitan":
				return "Occitan";
			case "odia \u0028oriya\u0029":
			case "odia":
			case "oriya":
				return "Odia \u0028Oriya\u0029";
			case "ojibwe":
				return "Ojibwe";
			case "okinawan":
				return "Okinawan";
			case "old aramaic":
				return "Old Aramaic";
			case "old east slavic":
				return "Old East Slavic";
			case "old english":
				return "Old English";
			case "old french":
				return "Old French";
			case "old norse":
				return "Old Norse";
			case "old prussian":
				return "Old Prussian";
			case "old saxon":
				return "Old Saxon";
			case "old spanish":
				return "Old Spanish";
			case "old tupi":
				return "Old Tupi";
			case "old turkish":
				return "Old Turkish";
			case "orizaba nahuatl":
				return "Orizaba Nahuatl";
			case "ossetian":
			case "ossetic":
				return "Ossetian";
			case "ottoman turkish":
				return "Ottoman Turkish";
			case "palatine german":
				return "Palatine German";
			case "palauan":
				return "Palauan";
			case "pangasinan":
				return "Pangasinan";
			case "papiamento":
				return "Papiamento";
			case "pashto":
			case "pushto":
				return "Pashto";
			case "pennsylvania german":
				return "Pennsylvania German";
			case "persian":
				return "Persian";
			case "phoenician":
				return "Phoenician";
			case "picard":
				return "Picard";
			case "piedmontese":
				return "Piedmontese";
			case "pipil":
				return "Pipil";
			case "polish":
				return "Polish";
			case "portuguese":
				return "Portuguese";
			case "pulaar":
				return "Pulaar";
			case "punjabi \u0028eastern\u0029":
			case "punjabi eastern":
			case "eastern punjabi":
				return "Punjabi \u0028Eastern\u0029";
			case "punjabi \u0028western\u0029":
			case "punjabi western":
			case "western punjabi":
				return "Punjabi \u0028Western\u0029";
			case "quechua":
				return "Quechua";
			case "quenya":
				return "Quenya";
			case "rapa nui":
				return "Rapa Nui";
			case "romani":
				return "Romani";
			case "romanian":
			case "moldavian":
			case "moldovan":
				return "Romanian";
			case "romansh":
				return "Romansh";
			case "russian":
				return "Russian";
			case "rusyn":
				return "Rusyn";
			case "samoan":
				return "Samoan";
			case "samogitian":
				return "Samogitian";
			case "sango":
				return "Sango";
			case "sanskrit":
				return "Sanskrit";
			case "sardinian":
				return "Sardinian";
			case "saterland frisian":
				return "Saterland Frisian";
			case "scots":
				return "Scots";
			case "scottish gaelic":
			case "gaelic":
				return "Scottish Gaelic";
			case "serbian":
				return "Serbian";
			case "setswana":
				return "Setswana";
			case "seychellois creole":
				return "Seychellois Creole";
			case "shanghainese":
			case "wu chinese":
				return "Shanghainese";
			case "shona":
				return "Shona";
			case "shuswap":
				return "Shuswap";
			case "sicilian":
				return "Sicilian";
			case "sindarin":
				return "Sindarin";
			case "sindhi":
				return "Sindhi";
			case "sinhala":
			case "sinhalese":
				return "Sinhala";
			case "slovak":
				return "Slovak";
			case "slovenian":
				return "Slovenian";
			case "somali":
				return "Somali";
			case "southern altai":
				return "Southern Altai";
			case "southern sami":
				return "Southern Sami";
			case "southern sotho":
				return "Southern Sotho";
			case "spanish":
				return "Spanish";
			case "sumerian":
				return "Sumerian";
			case "sundanese":
				return "Sundanese";
			case "swabian":
				return "Swabian";
			case "swahili":
				return "Swahili";
			case "swazi":
			case "swati":
			case "siswati":
				return "Swazi";
			case "swedish":
				return "Swedish";
			case "swiss german":
				return "Swiss German";
			case "syriac":
				return "Syriac";
			case "tachawit":
				return "Tachawit";
			case "tagal murut":
				return "Tagal Murut";
			case "tagalog":
				return "Tagalog";
			case "tahaggart tamahaq":
				return "Tahaggart Tamahaq";
			case "tahitian":
				return "Tahitian";
			case "tajik":
				return "Tajik";
			case "talossan":
				return "Talossan";
			case "talysh":
				return "Talysh";
			case "tamil":
				return "Tamil";
			case "tarifit":
				return "Tarifit";
			case "tatar":
				return "Tatar";
			case "telugu":
				return "Telugu";
			case "temuan":
				return "Temuan";
			case "tetun":
				return "Tetun";
			case "thai":
				return "Thai";
			case "tibetan":
				return "Tibetan";
			case "tigre":
				return "Tigre";
			case "tigrinya":
				return "Tigrinya";
			case "tok pisin":
				return "Tok Pisin";
			case "tokelauan":
				return "Tokelauan";
			case "toki pona":
				return "Toki Pona";
			case "tonga \u0028zambezi\u0029":
			case "zambezi":
			case "chitonga":
				return "Tonga \u0028Zambezi\u0029";
			case "tongan":
				return "Tongan";
			case "tsonga":
				return "Tsonga";
			case "turkish":
				return "Turkish";
			case "turkmen":
				return "Turkmen";
			case "tuvaluan":
				return "Tuvaluan";
			case "tuvinian":
				return "Tuvinian";
			case "uab meto":
				return "Uab Meto";
			case "udmurt":
				return "Udmurt";
			case "ukrainian":
				return "Ukrainian";
			case "umbundu":
				return "Umbundu";
			case "upper sorbian":
				return "Upper Sorbian";
			case "urdu":
				return "Urdu";
			case "urhobo":
				return "Urhobo";
			case "uyghur":
				return "Uyghur";
			case "uzbek":
				return "Uzbek";
			case "venetian":
				return "Venetian";
			case "veps":
				return "Veps";
			case "vietnamese":
				return "Vietnamese";
			case "volap\u00fck":
			case "volapuk":
			case "volapuek":
				return "Volap\u00fck";
			case "v\u00f5ro":
			case "voro":
				return "V\u00f5ro";
			case "walloon":
				return "Walloon";
			case "waray":
				return "Waray";
			case "welsh":
				return "Welsh";
			case "wolof":
				return "Wolof";
			case "xhosa":
				return "Xhosa";
			case "xiang chinese":
				return "Xiang Chinese";
			case "yakut":
				return "Yakut";
			case "yiddish":
				return "Yiddish";
			case "yoruba":
				return "Yoruba";
			case "zaza":
				return "Zaza";
			case "zulu":
				return "Zulu";
			default:
				return null;
		}
	}
}
