package com.avishkar.sentiword;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

public class SentiWordNet {

	private Map<String, Double> dictionary;
	StanfordCoreNLP pipeline;
	private static final String pathToSWN = "src/com/avishkar/sentiword/SentiWordNet_3.0.0_20130122.txt";

	public static void main(String[] args) throws IOException {
		SentiWordNet sentiwordnet = new SentiWordNet();
		String sentece = "Due to urgent maintenance works, the BESCOM's online services, http://www.bescom.org  portal will not be available from 22nd June 2018, 9:00 PM to 25th June 2018, 6:00 AM requesting to kindly co-operate.\r\n";
		System.out.println(sentiwordnet.evaluate(sentece));
	}

	public SentiWordNet() throws IOException {

		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER,
		// parsing, and coreference resolution
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		pipeline = new StanfordCoreNLP(props);

		// This is our main dictionary representation
		dictionary = new HashMap<String, Double>();

		// From String to list of doubles.
		HashMap<String, HashMap<Integer, Double>> tempDictionary = new HashMap<String, HashMap<Integer, Double>>();

		BufferedReader csv = null;
		try {
			csv = new BufferedReader(new FileReader(pathToSWN));
			int lineNumber = 0;

			String line;
			while ((line = csv.readLine()) != null) {
				lineNumber++;

				// If it's a comment, skip this line.
				if (!line.trim().startsWith("#")) {
					// We use tab separation
					String[] data = line.split("\t");
					String wordTypeMarker = data[0];

					// Example line:
					// POS ID PosS NegS SynsetTerm#sensenumber Desc
					// a 00009618 0.5 0.25 spartan#4 austere#3 ascetical#2
					// ascetic#2 practicing great self-denial;...etc

					// Is it a valid line? Otherwise, through exception.
					if (data.length != 6) {
						throw new IllegalArgumentException("Incorrect tabulation format in file, line: " + lineNumber);
					}

					// Calculate synset score as score = PosS - NegS
					Double synsetScore = Double.parseDouble(data[2]) - Double.parseDouble(data[3]);

					// Get all Synset terms
					String[] synTermsSplit = data[4].split(" ");

					// Go through all terms of current synset.
					for (String synTermSplit : synTermsSplit) {
						// Get synterm and synterm rank
						String[] synTermAndRank = synTermSplit.split("#");
						String synTerm = synTermAndRank[0] + "#" + wordTypeMarker;

						int synTermRank = Integer.parseInt(synTermAndRank[1]);
						// What we get here is a map of the type:
						// term -> {score of synset#1, score of synset#2...}

						// Add map to term if it doesn't have one
						if (!tempDictionary.containsKey(synTerm)) {
							tempDictionary.put(synTerm, new HashMap<Integer, Double>());
						}

						// Add synset link to synterm
						tempDictionary.get(synTerm).put(synTermRank, synsetScore);
					}
				}
			}

			// Go through all the terms.
			for (Map.Entry<String, HashMap<Integer, Double>> entry : tempDictionary.entrySet()) {
				String word = entry.getKey();
				Map<Integer, Double> synSetScoreMap = entry.getValue();

				// Calculate weighted average. Weigh the synsets according to
				// their rank.
				// Score= 1/2*first + 1/3*second + 1/4*third ..... etc.
				// Sum = 1/1 + 1/2 + 1/3 ...
				double score = 0.0;
				double sum = 0.0;
				for (Map.Entry<Integer, Double> setScore : synSetScoreMap.entrySet()) {
					score += setScore.getValue() / (double) setScore.getKey();
					sum += 1.0 / (double) setScore.getKey();
				}
				score /= sum;

				dictionary.put(word, score);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (csv != null) {
				csv.close();
			}
		}
	}

	public SentiTweetData evaluate(String tweet) {
		SentiTweetData data = new SentiTweetData();
		if(tweet == null || tweet.isEmpty())
			return data;
		ArrayList<Double> sentiScore = new ArrayList<>();
		// create an empty Annotation just with the given text
		Annotation document = new Annotation(tweet);

		// run all Annotators on this text
		pipeline.annotate(document);

		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(CoreAnnotations.TextAnnotation.class);
				// this is the POS tag of the token
				String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
				// this is the NER label of the token
				String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
//				System.out.println(String.format("Print: word: [%s] PoS: [%s] NamedEntity: [%s]", word, pos, ne));
				data.getNamedEntities().add(ne);
				if (!isSkipWord(word) && getWordnetPOS(token) != null) {
					Double score = extract(word.toLowerCase(), getWordnetPOS(token));
					if (score != null)
						sentiScore.add(score);
				}
			}
		}
		data.setScore(calculateAverage(sentiScore));
		return data;
	}

	public double extract(String word, String pos) {
		String key = word + "#" + pos;
//		System.out.println(word + "#" + pos);
		if (dictionary.containsKey(key)) {
			return dictionary.get(word + "#" + pos);
		} else
			return 0;
	}

	private static String getWordnetPOS(CoreLabel word) {

		String stanfordPOS = word.getString(PartOfSpeechAnnotation.class);

		if (stanfordPOS.startsWith("N"))
			return "n";
		if (stanfordPOS.startsWith("J"))
			return "a";
		if (stanfordPOS.startsWith("V"))
			return "v";
		if (stanfordPOS.startsWith("R"))
			return "r";
		return null;

	}

	private boolean isSkipWord(String word) {
		if (word.contains("#") || word.contains("#"))
			return true;
		return false;
	}

	public static double calculateAverage(List<Double> list) {
		Double sum = 0.0;
		if (!list.isEmpty()) {
			for (Double mark : list) {
				sum += mark;
			}
			return sum / list.size();
		}
		return sum;
	}

}