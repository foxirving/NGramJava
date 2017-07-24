import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * This class generates a bigram by reading in a file to a string. It then
 * creates nodes for each word of the string, stores them in a
 * hashmap<theStringName, theNode>. Each node contains a parallel list that
 * keeps tracks of pointers to other nodes that proceed the current node and the
 * number of times it has occurred.
 * 
 * Probability is calculated by dividing the number of times the next node
 * occurs divided by the total number of times all children nodes occur.
 * 
 * @author Amy Irving
 * @version July 5, 2017
 *
 */
public class Bigram {

	/**
	 * Static strings of filename,.
	 */
	public static String FILE_NAME_ONE = "doyle-27.txt";
	public static String FILE_NAME_TWO = "doyle-case-27.txt";

	public static String OUT_FILE_NAME_ONE = "bigram_probs.txt";
	public static String OUT_FILE_NAME_TWO = "bigram_eval.txt";

	/**
	 * Start Symbol and end symbol in our bigram.
	 */
	public static String START_SYMBOL = "<START>";
	public static String END_SYMBOL = "<END>";

	/**
	 * Number of lines written to a file.
	 */
	public static int NUMBER_OF_LINES = 100;

	/**
	 * Hashmap that stores nodes and their string name.
	 */
	private static HashMap<String, Node> myHashMap;

	public static void main(final String[] args) {
		// Initialize Hashmap.
		myHashMap = new HashMap<String, Node>();
		final Node startSymbol = new Node(START_SYMBOL);
		myHashMap.put(START_SYMBOL, startSymbol);

		startProbalities();
		startEvaluation();

	}

	/**
	 * Method that runs all other methods to find and write all probabilities of
	 * words found in the file.
	 */
	public static void startProbalities() {
		// Read in File to String
		final String str = readFile(FILE_NAME_ONE, false);
		// Build graph using each word in string.
		buildGraph(str);
		// Calculates probability and write to file.
		writeProbabilities(OUT_FILE_NAME_ONE);
	}

	/**
	 * Method that runs all other methods to find and write all joint
	 * probabilities and the perplexity of each line found in a file.
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
	 * Iterates though string, gets each word, and build node graph.
	 * 
	 * @param theString
	 *            - the contents of a file converted to a string.
	 */
	private static void buildGraph(final String theString) {
		Node currentNode = myHashMap.get(START_SYMBOL);
		for (final String word : theString.split(" ")) {
			// Checks for empty string
			if (!"".equals(word.trim())) {
				processNextString(currentNode, word);
				currentNode = myHashMap.get(word);
			}
		}
		// adds end symbol
		processNextString(currentNode, END_SYMBOL);
	}

	/**
	 * Given the current node and the next string, adds to nodes list of
	 * strings.
	 * 
	 * @param theCurrentNode
	 *            - the current node being processed
	 * @param theNextString
	 *            - of the word that follows the current node.
	 */
	private static void processNextString(final Node theCurrentNode, final String theNextString) {
		/*
		 * if the hashmap contains the node, we give the current node a pointer
		 * to it. else we create a new node, add it to hashmap, and gives its
		 * pointer to the current node.
		 */
		if (myHashMap.containsKey(theNextString)) {
			theCurrentNode.processNextNode(myHashMap.get(theNextString));
		} else {
			final Node node = new Node(theNextString);
			myHashMap.put(theNextString, node);
			theCurrentNode.processNextNode(node);
		}
	}

	/**
	 * Iterates through each node in hashmap, calculates the probability of each
	 * and writes it to a file.
	 * 
	 * @param theOutfileName
	 *            - name of the file being written out.
	 */
	private static void writeProbabilities(final String theOutfileName) {
		final List<Node> nodeList = new ArrayList<Node>(myHashMap.values());
		List<String> probList = new ArrayList<String>();

		// Gets probability for all nodes.
		for (final Node node : nodeList) {
			probList.addAll(node.calculateAllProbability());
		}
		// shuffles results.
		Collections.shuffle(probList);
		// Gets the first 100 lines of the list.
		probList = probList.subList(0, NUMBER_OF_LINES);
		// writes all probabilities to file.
		writeFile(probList, OUT_FILE_NAME_ONE);
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
		final String prevWord = START_SYMBOL;
		for (final String word : string) {
			if (!myHashMap.get(prevWord).isChild(word)){
				lineProbability = 0.0;
				break;
			} 
			lineProbability *= 	myHashMap.get(prevWord).getProbability(word);	
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
	

	/**
	 * Node that contains the string of the word, two parallel arrays to keep
	 * track of proceeding words and the number of times they occur.
	 * 
	 * @author Amy Irving
	 *
	 */
	static class Node {

		/**
		 * The word associated with the node.
		 */
		private final String myStringName;

		/**
		 * These are parallel Arrays to keep track of children nodes and their
		 * number of occurrences.
		 */
		private final List<Node> myNodeList;
		private final List<Integer> myIntList;

		/**
		 * Node constructor.
		 * 
		 * @param myStringName
		 *            - The word associated with the node.
		 */
		public Node(final String myStringName) {
			this.myStringName = myStringName;

			// Parallel Arrays to keep track of all children nodes.
			myNodeList = new ArrayList<Node>();
			myIntList = new ArrayList<Integer>();
		}

		/**
		 * Gets the probability of a single child node appearing after
		 * our current node (this).
		 * 
		 * @param word -  the name of the node we're finding the probability for.
		 * @return the probability that the node will apear after our current node (this).
		 */
		public Double getProbability(final String word) {
			return (double) myIntList.get(getNodeIndex(word)) / getTotalOccurrences();
		}

		/**
		 * If node exists, increment counter. Else add the new node.
		 * 
		 * @param theNextNode
		 *            - the node following our current node (this).
		 */
		public void processNextNode(final Node theNextNode) {
			// if it does not contain the node, add it.
			if (!myNodeList.contains(theNextNode)) {
				addNode(theNextNode);
			}
			// if the node exists, increment count.
			else {
				incrementCount(theNextNode);
			}
		}

		/**
		 * Adds node to myNodeArray
		 * 
		 * @param theNextNode
		 *            - the node following our current node (this).
		 */
		public void addNode(final Node theNextNode) {
			myNodeList.add(theNextNode);
			myIntList.add(1);
		}

		/**
		 * Increments count for node in myIndexArray.
		 * 
		 * @param theNextNode
		 *            - the node following our current node (this).
		 */
		public void incrementCount(final Node theNextNode) {
			final int index = myNodeList.indexOf(theNextNode);
			myIntList.set(index, myIntList.get(index) + 1);
		}

		/**
		 * Gets the total number of out pointers by iterating through
		 * myIntArray.
		 * 
		 * @return the total number of occurrences in myIntList.
		 */
		public int getTotalOccurrences() {
			int total = 0;
			for (final Integer integer : myIntList) {
				total += integer;
			}
			return total;
		}

		/**
		 * Calculates probability by dividing each number of occurrences for the
		 * child node, divided by the total number of times all children occur.
		 * 
		 * @return String containing all probabilities calculated fot this node.
		 */
		public List<String> calculateAllProbability() {
			final List<String> probList = new ArrayList<String>();
			final double total = getTotalOccurrences();
			int index = 0;
			for (final Node node : myNodeList) {
				final double prob = (double) myIntList.get(index) / total;
				probList.add("P(" + node.myStringName + "|" + myStringName + ") = " + prob);
				index += 1;
			}
			return probList;
		}

		/**
		 * @return myStringName
		 */
		public String getMyStringName() {
			return myStringName;
		}

		/**
		 * @return Get the number of children nodes stored in myNodeArray.
		 */
		public int getTotalChildren() {
			return myNodeList.size();
		}
		
		/**
		 * Checks if the name of the node matches any of the children nodes
		 * stored in myNodeArray.
		 * 
		 * @param theChildName - name of the node we're trying to find.
		 * @return true or false if the node is in myNodeArray.
		 */
		public boolean isChild(final String theChildName){
			for (final Node node : myNodeList) {
				if (theChildName.equals(node.getMyStringName())){
					return true;
				}
			}
			return false;
		}
		
		/**
		 * Gets the index of a node given its name.
		 * 
		 * @param theNodeName - name of the node we're searching for.
		 * @return the index of the node in myNodeArray.
		 */
		public int getNodeIndex(final String theNodeName){
			int i = -1;
			for (i = 0; i < myNodeList.size(); i++) {
				if (theNodeName.equals(myNodeList.get(i).getMyStringName())) {
					break;
				}
			}
			return i;
		}

		/**
		 * Automatically Prints out the name of this node, its total number of
		 * occurrences, the name of each child node and their number of
		 * occurrences.
		 */
		public void print() {
			System.out.println("String :" + myStringName + ", total : " + getTotalOccurrences());
			int index = 0;
			for (final Node node : myNodeList) {
				System.out.println("    [Child  :" + node.getMyStringName() + " count : " + myIntList.get(index));
				index += 1;
			}
		}

	} // End node class
}