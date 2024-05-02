import java.util.regex.*;
import java.io.*;
import java.util.*;
import java.time.*;
import java.util.concurrent.TimeUnit;

/**
 * class to represent the Eliza engine
 */
public class ElizaEngine {
    private static List<List<String>> script = new ArrayList<List<String>>(); // initialise an array list of array lists
                                                                              // to store the specified script
    private static HashMap<String, String> preSubs = new HashMap<String, String>();
    private static HashMap<String, String> postSubs = new HashMap<String, String>();

    /**
     * main method for the program
     * @param args
     */
    public static void main(String[] args) {
        String phrase = "";
        if (args.length == 0) { // ensure a command line parameter is specified
            System.out.println("You must provide a script file as a parameter");
            return;
        }
        //read in the 3 relevant text files
        readInScript(args[0]); // pass in the command line parameter
        readInRules("PreSubstitute.txt", preSubs);
        readInRules("PostSubstitute.txt", postSubs);
        
        boolean running = true;
        String input = "";
        System.out.println("• " + script.get(0).get(0)); // prints the welcome message

        while (running) {
            input = getUserInput();
            try {
                TimeUnit.SECONDS.sleep(1); // wait to create more realism. Used this to help:https://stackoverflow.com/questions/47717633/how-to-use-timeunit-in-java
            } catch (InterruptedException e) {
                System.out.println("Interrupted exception" + e.getMessage());
            }
            
            // System.out.println(preSubstituteInput(input));
            if (script.get(script.size() - 2).contains(input)) { // checks for a quit message
                System.out.println("• " + script.get(1).get(0)); // prints the final message
                running = false;
            }
            else{
                phrase = createOutput(substituteInput(input, preSubs)); //passes the user input into the method after performing the presubstitutions

                if (phrase.equals("")) {
                    //prints a random response from the set responses when no keywords have been found
                    System.out.println("• " + script.get(script.size() - 1).get(generateRandomNumber(0, script.get(script.size() - 1).size())));
                }
            }

        }
    }

    /**
     * reads in and stores the specified script
     * @param scriptFile
     */
    public static void readInScript(String scriptFile) {
        try {
            List<String> responses = new ArrayList<String>();
            BufferedReader reader = new BufferedReader(new FileReader(scriptFile));
            while (reader.ready()) {
                String line = reader.readLine();
                if (!(">>").equals(line)) { //denotes the next set of responses
                    responses.add(line); //adds the line to an arraylist
                }

                else {
                    script.add(new ArrayList<String>(responses)); //adds the arraylist to a larger arraylist
                    responses.clear(); //clears the arraylist ready for the next set of responses
                }
            }
            reader.close();
        }

        catch (FileNotFoundException e) {
            System.out.println("File not found:" + e.getMessage());
        }

        catch (IOException e) {
            System.out.println("IO Exception: " + e.getMessage());
        }
    }
    
    /**
     * receives the input from the user
     * @return
     */
    public static String getUserInput() {
        System.out.print("> "); //prints a symbol to denote the user input (for aesthetics purposes)
        Scanner scanner = new Scanner(System.in);
        return (scanner.nextLine().toLowerCase());
    }

    /**
     * reads in the preSub and postSub rules
     * @param filepath
     * @param map
     */
    public static void readInRules(String filepath, HashMap<String, String> map) {
        try {
            File file = new File(filepath);
            Scanner scan = new Scanner(file);
            while (scan.hasNextLine()) { //loops for each line in the file
                String temp = scan.nextLine();
                String[] keyPair = temp.split(","); //creates a keypair using comma as a delimeter. Used to help: https://stackoverflow.com/questions/31153753/split-string-into-key-value-pairs
                map.put(keyPair[0], keyPair[1]);
            }
        }

        catch (FileNotFoundException e) {
            System.out.println("File not found:" + e.getMessage());
        }

    }

    /**
     * changes the relevant words in the sentence depending on the provided rules (pre/post subs)
     * @param input
     * @param map
     * @return
     */
    public static String substituteInput(String input, HashMap<String, String> map) {

        input = input.toLowerCase(); //converts input to lowercase to check against the map
        ArrayList<String> tokenizedString = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(input); // used to help:
                                                                // https://www.geeksforgeeks.org/stringtokenizer-class-in-java/
        while (tokenizer.hasMoreTokens()) {
            tokenizedString.add(tokenizer.nextToken()); //splits the sentence into individual words
        }

        for (int i = 0; i < tokenizedString.size(); i++) {
            boolean swapped = false;
            for (Map.Entry<String, String> set : map.entrySet()) {
                if (tokenizedString.get(i).equals(set.getKey()) && swapped == false) { //ensures a word is not substitueted twice
                    tokenizedString.set(i, set.getValue()); //substitutes the word
                    swapped = true;
                }
            }
        }
        String output = " ";
        for (String word : tokenizedString) {
            output = output + word + " "; //rebuilds the sentence
        }
        output = (output + "\b"); //deletes the extra space on the end of the sentence
        return output;

    }

    /**
     * matches and prints a relevant output
     * @param sentence
     * @return
     */
    public static String createOutput(String sentence) {
        String phrase = "";
        String response = "";
        boolean found = false;

        for (List<String> list : script) {
            if (found == true) {
                break; //stop looping if the keyword is found
            } 
            
            else {
                Pattern p = Pattern.compile(".*" + "\\b" + list.get(0) + "\\b" + ".*"); //regex represents a certain string that appears with a space either side anywhere in the sentence
                Matcher m = p.matcher(sentence);
                boolean matches = m.matches();

                if (matches == true) { //checks to see if the sentence contains keywords/phrases
                    found = true;
                    response = list.get(generateRandomNumber(1, list.size())); //picks a random response in the relevant array list
                    String[] sentenceParts = sentence.split(list.get(0)); // split string either side of the key phrase to get the context

                    if (sentenceParts.length > 1) { //ensures that a null element is not accessed
                        phrase = substituteInput(sentenceParts[1], postSubs); //apply post substitutuions
                        response = response.replace("*", phrase);
                        System.out.println("• " + response); // bullet point denoting output
                    } else {
                        phrase = substituteInput(sentence, postSubs);
                        response = response.replace("*", phrase);
                        System.out.println("• " + response);
                    }
                }
            }
        }
        return phrase;
    }

    /**
     * returns a random number in the specified range
     * @param lowerLimit
     * @param upperLimit
     * @return
     */
    public static int generateRandomNumber(int lowerLimit, int upperLimit) {
        Random rand = new Random();
        int random = rand.nextInt(lowerLimit, upperLimit); //used to help: https://www.educative.io/answers/how-to-generate-random-numbers-in-java
        return random;
    }
}
