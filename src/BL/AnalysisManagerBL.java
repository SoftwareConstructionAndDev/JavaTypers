package BL;
import java.util.*;
public class AnalysisManagerBL {

    public Map<String, Map<String, Map<String, Double>>> analyzeIndividualFiles(List<String> allFiles) {
        Map<String, Map<String, Map<String, Double>>> individualResults = new LinkedHashMap<>();
    
        for (String content : allFiles) {
            // Create a map to hold the analysis results for this individual file
            Map<String, Double> tfidf = calculateTFIDF(content, allFiles);
            Map<String, Double> pmi = calculatePMI(content, allFiles);
            Map<String, Double> pkl = calculatePKL(content);
    
            // Create a map for the file's overall analysis results
            Map<String, Map<String, Double>> fileResults = new LinkedHashMap<>();
            fileResults.put("TF-IDF", tfidf);
            fileResults.put("PMI", pmi);
            fileResults.put("PKL", pkl);
    
            // Put the file's results into the individualResults map, using content as the key
            individualResults.put(content, fileResults);
        }
    
        return individualResults;
    }
    
    
    public Map<String, Map<String, Double>> analyzeAcrossAllFiles(List<String> allFiles) {
        String combinedContent = String.join(" ", allFiles);

        Map<String, Double> tfidf = calculateTFIDF(combinedContent, allFiles);
        Map<String, Double> pmi = calculatePMI(combinedContent, allFiles);
        Map<String, Double> pkl = calculatePKL(combinedContent);

        Map<String, Map<String, Double>> overallResults = new LinkedHashMap<>();
        overallResults.put("TF-IDF", tfidf);
        overallResults.put("PMI", pmi);
        overallResults.put("PKL", pkl);

        return overallResults;
    }
    public Map<String, Double> calculateTFIDF(String content, List<String> allFiles) {
        Map<String, Integer> termFrequency = calculateTermFrequency(content);
        Map<String, Double> tfidfScores = new HashMap<>();
        int totalDocs = allFiles.size();

        for (String term : termFrequency.keySet()) {
            int docCount = (int) allFiles.stream().filter(file -> file.contains(term)).count();
            double tf = termFrequency.get(term) / (double) content.split("\\s+").length;
            double idf = Math.log((double) totalDocs / (docCount + 1));
            tfidfScores.put(term, tf * idf);
        }
        return tfidfScores;
    }

    public Map<String, Double> calculatePMI(String content, List<String> allFiles) {
        Map<String, Integer> termFrequency = calculateTermFrequency(content);
        Map<String, Double> pmiScores = new HashMap<>();
        int totalWords = content.split("\\s+").length;

        for (String term1 : termFrequency.keySet()) {
            for (String term2 : termFrequency.keySet()) {
                if (!term1.equals(term2)) {
                    double pXY = calculateJointProbability(term1, term2, allFiles);
                    double pX = termFrequency.get(term1) / (double) totalWords;
                    double pY = termFrequency.get(term2) / (double) totalWords;

                    if (pX > 0 && pY > 0 && pXY > 0) {
                        pmiScores.put(term1 + " | " + term2, Math.log(pXY / (pX * pY)));
                    }
                }
            }
        }
        return pmiScores;
    }
    private Map<String, Integer> calculateTermFrequency(String content) {
        Map<String, Integer> frequency = new HashMap<>();
        String[] words = content.split("\\s+");
        for (String word : words) {
            frequency.put(word, frequency.getOrDefault(word, 0) + 1);
        }
        return frequency;
    }

    private double calculateJointProbability(String term1, String term2, List<String> allFiles) {
        int count = 0;
        for (String file : allFiles) {
            if (file.contains(term1) && file.contains(term2)) {
                count++;
            }
        }
        return count / (double) allFiles.size();
    }

    // PKL Calculation (Pointwise Mutual Information - Latent)
    public Map<String, Double> calculatePKL(String content) {
        // Tokenize the content
        List<String> words = tokenize(content);
        
        // Create a map for word pairs
        Map<String, Map<String, Integer>> coOccurrenceMap = new HashMap<>();
        Map<String, Integer> wordCountMap = new HashMap<>();
        int totalWordCount = 0;
        
        // Build the word count and co-occurrence map
        for (int i = 0; i < words.size(); i++) {
            String word1 = words.get(i);
            wordCountMap.put(word1, wordCountMap.getOrDefault(word1, 0) + 1);
            totalWordCount++;

            for (int j = i + 1; j < words.size(); j++) {
                String word2 = words.get(j);
                coOccurrenceMap.putIfAbsent(word1, new HashMap<>());
                coOccurrenceMap.get(word1).put(word2, coOccurrenceMap.get(word1).getOrDefault(word2, 0) + 1);
            }
        }

        // Calculate PKL (Pointwise Mutual Information - Latent) between word pairs
        Map<String, Double> pklResults = new HashMap<>();
        for (Map.Entry<String, Map<String, Integer>> entry : coOccurrenceMap.entrySet()) {
            String word1 = entry.getKey();
            Map<String, Integer> coOccurringWords = entry.getValue();

            for (Map.Entry<String, Integer> coOccurEntry : coOccurringWords.entrySet()) {
                String word2 = coOccurEntry.getKey();
                int coOccurrenceCount = coOccurEntry.getValue();

                // Calculate the individual probabilities
                double pWord1 = (double) wordCountMap.get(word1) / totalWordCount;
                double pWord2 = (double) wordCountMap.get(word2) / totalWordCount;
                double pWord1AndWord2 = (double) coOccurrenceCount / totalWordCount;

                // Calculate PKL using the formula
                double pkl = Math.log(pWord1AndWord2 / (pWord1 * pWord2));

                // Save the PKL result in the map
                pklResults.put(word1 + "|" + word2, pkl);
            }
        }

        return pklResults;
    }

    // Helper method to tokenize the content (this could be improved with stemming, stop words removal, etc.)
    private List<String> tokenize(String content) {
        String[] words = content.split("\\s+");
        return Arrays.asList(words);
    }

 
}



