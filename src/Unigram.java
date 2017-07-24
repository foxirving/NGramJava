import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * Reads in a string, uses a Hashmap to store the word and the number of times
 * it occurs, then calculates and writes the probabilities to a file.
 * 
 * It then evaluates the perplexity of each line of the next file, and writes out 
 * the perplexity of the first 100 lines.
 * 
 * @author Amy Irving
 * @version July 5, 2017
 *
 */

public class Unigram {

	/**
	 * Static strings of filename.
	 */
	public static String FILE_NAME_ONE = "doyle-27.txt";
	public static String FILE_NAME_TWO = "doyle-case-27.txt";

	public static String OUT_FILE_NAME_ONE = "unigram_probs.txt";
	public static String OUT_FILE_NAME_TWO = "unigram_eval.txt";

	/**
	 * Number of lines written to a file.
	 */
	public static int NUMBER_OF_LINES = 100;

	/**
	 * Hashmap that stores a string word and their occurrences.
	 */
	private static HashMap<String, Double> myHashMap;


	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		myHashMap = new HashMap<String, Double>();

		startProbalities();
		startEvaluation();

	}

	/**
	 * Method that runs all other methods to find and write all probabilities
	 * of words found in the file.
	 */
	public static void startProbalities() {
		final String str = readFile(FILE_NAME_ONE, false);
		countOccurrences(str);
		writeProbabilities(OUT_FILE_NAME_ONE);
	}

	/**
	 * Method that runs all other methods to find and write all joint probabilities
	 * and the perplexity of each line found in a file.
	 */
	public static void startEvaluation() {
		final List<Double> perplexities = evaluateStringPerplexities(readFile(FILE_NAME_TWO, true));
		writeFile(perplexities, OUT_FILE_NAME_TWO);
	}

	/**
	 * Reads in a text file and returns it as a string.
	 * 
	 * @param theFileName
	 *            - file to become string.
	 * @param isFormated
	 *            - Will the returned string contain line breaks.
	 * @return the string created from reading theFileName.
	 */
	private static String readFile(final String theFileName, final boolean isFormated) {
		String str = "";
		try (Scanner sc = new Scanner(new File(theFileName));) {
			// "\Z" means "end of string"
			str = sc.useDelimiter("\\Z").next().toLowerCase().trim();
			// "\r" and "\n" are line breaks in linux and windows respectively.
			if (!isFormated) {
				str = str.replaceAll("\\r", " ").replaceAll("\\n", " ");
				str = str.replaceAll("\\s+", " ");
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * Iterates though the given string and counts occurrence of each word.
	 * Puts the data in a hashmap<TheWord, theNumberOfOccurences>.
	 * 
	 * @param theString - the string being evaluated.
	 */
	private static void countOccurrences(final String theString) {
		for (final String word : theString.split(" ")) {
			// Checks for empty string
			if (!"".equals(word.trim())) {
				if (myHashMap.containsKey(word)) {
					final double newValue = myHashMap.get(word).intValue() + 1;
					myHashMap.put(word, newValue);
				} else {
					myHashMap.put(word, 1.0);
				}
			}
		}
	}

	/**
	 * Iterates through each string in list, calculates the probability of each
	 * and writes it to a file.
	 * 
	 * @param theOutfileName
	 *            - name of the file being written out.
	 */
	private static void writeProbabilities(final String theOutfileName) {
		final List<String> myOutputList = new ArrayList<String>();
		
		final Iterator<Entry<String, Double>> it = myHashMap.entrySet().iterator();
		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			final
			Map.Entry pair = it.next();
			final double prob = (double) pair.getValue() / myHashMap.size();
			myOutputList.add("P(" + pair.getKey() + ") = " + prob);
		}
		writeFile(myOutputList, theOutfileName);
	}
	
	/**
	 * Writes any list to a file. Converts all object types into string before writing it.
	 * 
	 * @param theStringToWrite - string being written to file.
	 * @param theOutFileName - name of the file being written out.
	 */
	private static void writeFile(final List<?> theStringToWrite, final String theOutFileName){
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(theOutFileName), "utf-8"))) {
			for (final Object string : theStringToWrite) {
				writer.write(string.toString());
				writer.newLine();
			}
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads each line of a string, finds the joint probabilities of each line, then calculates the
	 * perplexity of each line.
	 * 
	 * @param theString - being evaluated.
	 * @return A list of doubles representing the perplexity of each line of theString.
	 */
	private static List<Double> evaluateStringPerplexities(final String theString) {
		// Creates list, separates each line, and removes empty strings.
		final List<String> result = new ArrayList<String>();
		result.addAll(Arrays.asList(theString.split(System.lineSeparator(), theString.length())));
		while(result.contains("")) {
			result.remove("");
		}
		final List<Double> perplexities = new ArrayList<Double>();
		
		// Calculate each line perplexity and add to list.
		String line = "";
		for (int i = 0; i < NUMBER_OF_LINES; i++) {
			line = result.get(i);
			if (!"".equals(line)) {
				final double jointProb = calculateLineProbability(line);
				// how many words are in the line.
				final int linelength = line.split(" ").length;
				perplexities.add(calculateLinePerplexity(linelength, jointProb));
			}
		}
		return perplexities;
	}

	/**
	 * Calculates the joint probability of the sequence of words in the line given.
	 * 
	 * @param theLine - being evaluated.
	 * @return the joint probability of the sequence of words in that line.
	 */
	private static double calculateLineProbability(final String theLine) {
		final String[] string = theLine.split(" ");
		Double lineProbability = 1.0;
		for (final String word : string) {
			if (null == myHashMap.get(word)) {
				lineProbability = 0.0;
				break;
			}
			lineProbability *= myHashMap.get(word);
		}
		return lineProbability;
	}
	
	/**
	 * Calculates the perplexity given the number of words in a line and the joint 
	 * probability of that sequence of words.
	 * 
	 * @param theLineLength - number of words in that line.
	 * @param theLineProbability - joint probability of that sequence of words.
	 * @return
	 */
	private static double calculateLinePerplexity(final int theLineLength, final double theLineProbability) {
		double perplexity = 0.0;
		// calculate perplexity = 1/(pow(joint_prob, 1.0/sent_len))		
		perplexity = 1.0 / (Math.pow(theLineProbability, (1.0/theLineLength)));
		return perplexity;
	}
}
