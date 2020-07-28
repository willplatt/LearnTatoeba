# LearnTatoeba

Learn a language using full sentences. Choose from over 300 languages and start reading sentences based on your vocabulary and the words you’re learning.

## How it works

LearnTatoeba (LT) is a commandline application with access to millions of sentences with translations from [Tatoeba](https://tatoeba.org/).
LT keeps track of the words you know and the words you’re learning to provide you with just the right sentences to keep you on your toes and learning.

It’s great for absolute beginners or anyone who’s not yet ready to read articles and books.
Whatever your native language, Tatoeba will have translations for it.

In reading and trying to understand these sentences you’ll be introduced to new words in the context of more familiar words.
This should help you infer the meanings of new words, and then you can look at the sentence translation to see if you were right.

LT uses a status from 1 to 5 to keep track of how well you know a word.
When you come across a new word, you can add it to your vocabulary with a status of 1.
If you come across that word after several minutes and remember its meaning, bump the status up to 2.
If you see it again the next day and still remember the meaning, why not increase it to 3?
Updating the status not only helps you see your progress, but it also helps LT choose the right sentences for where you’re at.

**Extra features:**

* Assign statuses to phrases involving multiple words
* Practice your languages offline
* Share your LT vocabulary with the [FLTR](https://fltr.sourceforge.io/) reading tool

## Running the application

Download one of the `.zip` files and extract it anywhere.
To run the program, simply run the `.jar` file from a terminal with Java 11 or newer:
```
java -jar LearnTatoeba.jar
```

### Using it with FLTR

LT can also work alongside the [FLTR](https://fltr.sourceforge.io/) reading tool.
This means if you update your vocabulary through either program, the other program will also see the updates.
Just set the vocab directory for your LT account to your FLTR data directory.

Also, when you add a language through FLTR, if you want it to be recognized in LT, you have to give it a name LT recognizes.
The preferred names for languages recognized by LT are listed in the first column of [languages.tsv](languages.tsv).
(Some alternative names and spellings are also recognized.)

## Fixing common issues

### Character printing issues

If non-Latin characters aren’t showing correctly, configure your terminal with an appropriate font and [code page](https://en.wikipedia.org/wiki/Code_page) for those characters.
For example, the Windows Hebrew code page is code page 1255, so if I want to practice Hebrew with a Windows terminal I set the code page with the command `chcp 1255`, and then I run LT.


If you are still having issues, try adding the Java `-Dfile.encoding` [argument](https://docs.oracle.com/en/java/javase/11/intl/supported-encodings.html) like in these examples:
```
java -Dfile.encoding=UTF-8 -jar LearnTatoeba.jar
java -Dfile.encoding=windows-1255 -jar LearnTatoeba.jar
```
If you’re still having difficulty, use a terminal with better multilingual support, such as [mlterm](https://sourceforge.net/projects/mlterm/).

### Right-to-left and bi-directional text

For the best experience, use a terminal supporting bi-directional text, such as [mlterm](https://sourceforge.net/projects/mlterm/).

If your terminal doesn’t support bi-directional text, you can still fix the sentence printing by run the `.jar` file with the `bidi` argument:
```
java -jar LearnTatoeba.jar bidi
```

If a right-to-left language is still displaying left-to-right then see [the first known issue](#known-issues) below.

## Known issues

Tatoeba provides sentences in over 300 languages, the vast majority of which I know very little about.
As a consequence, in some languages the sentences are not going to be handled correctly.
If you notice such an issue, I would be grateful if you point it out to me and what the desired behavior should be.
In the meantime, here are two issues you can try to fix yourself:

1) If the language is right-to-left but it’s still coming out left-to-right, even with the `bidi` argument, then find the line for that language in [languages.tsv](languages.tsv) and add `rtl` to the end of the line (in other words, in the 5th column).

2) If the language uses non-Latin characters and words do not have dashes `---` underneath them that means the characters are not getting recognized as word characters.
This can be fixed by adding (or changing) an appropriate regular expression in the 4th column of `languages.tsv` on the line for that language.
When the column is left blank then the default is implied: `\\-'a-zA-ZÀ-ÖØ-ö\u00F8-\u01BF\u01C4-\u024F\u0370-\u052F\u1E00-\u1FFF`.

## Background

### Why create LearnTatoeba?

LT is based on the [input hypothesis](https://en.wikipedia.org/wiki/Input_hypothesis): “learners progress in their knowledge of the language when they comprehend language input that is slightly more advanced than their current level.”

[Extensive reading](https://en.wikipedia.org/wiki/Extensive_reading) (ER) is a great way to get lots of this comprehensible input, but what if you’re not at the right level to be reading books and articles yet?
Unless you have a fluent speaker willing to spend lots of time giving you comprehensible input from the very basics, you don’t have much choice but to go the traditional route of memorizing words and learning grammar rules.

Not anymore!

Starting with much smaller texts—sentences—is the logical stepping stone on the way to ER.
So that’s exactly what LT gives you.

### The role of inference

In ER, the reader tries to infer the meanings of new words and grammatical constructs from their context.
These inferences are integral to the ER learning process, and distinguish it from many traditional methods which would have the reader learn word definitions and grammar rules before trying to read a text that employs them.

Single sentences do not have much context, so it is more difficult to infer meanings, especially in short sentences.
To help the reader along, LT offers the reader a translation if they need it.
However, LT does not provide word meanings or word-for-word translations.
This means it is still up to the reader to infer word meanings from the broader context of the sentence and its meaning.

There is perhaps a better approach that maintains the appropriate amount of inference, but for now this will have to suffice.
I welcome any development contributions or advice on how to improve LT.

## Future plans

LT is primarily a proof of concept at the moment.
It sounded great in theory, but does it actually help you learn a language?
I intend to test that for myself by using it for a while to help me learn Lojban.
I picked Lojban simply because it’s a language I’m interested in and has a decent number of sentences on Tatoeba (about 15,000).
If anything, one would expect LT to be more effective for languages with more sentences than Lojban.

I expect while using LT for Lojban I will be prompted to change the method of sentence selection and other small details.
It might also give ideas for extra features.
If you use LT I would be interested to hear what your experiences and ideas are too.
Even if you only used it once, I would like to know why you didn’t use it more.

If LT seems like a useful tool with advantages over other freely available learning methods, then the following are some natural next steps to take:

* Add a GUI
* Add automated tests
* Add Tatoeba audio and text-to-speech
* Improve the speed of sentence selection (currently limited by the way Tatoeba organizes its download files)
* Use sentences outside of Tatoeba (for example, biblical languages can be learnt with Bible verses and their translations)
* Let readers report bad sentences/translations
* In addition to sentences, suggest conversations, paragraphs, and full texts based on the reader’s vocabulary

If things advance so far, maybe there could even be a mobile app and a browser extension.

## License

I have chosen to license this project under the [MIT license](LICENSE.txt) to give the most freedom for people to modify and redistribute this software.

## Disclaimer

Sentences from [Tatoeba](https://tatoeba.org) are released under the [CC-BY 2.0 FR](https://creativecommons.org/licenses/by/2.0/fr/) license.
This project is not endorsed by or affiliated with Tatoeba.
I and this project hold no responsibility for content from Tatoeba, whether sentences, translations, or information connected with these, such as language or author.

Be aware that members of the general public can contribute to Tatoeba without proof of language proficiency.
Tatoeba does not guarantee the quality of its sentences.
If you find mistakes or take issue with content from Tatoeba, you may be able to fix these problems through Tatoeba's website.