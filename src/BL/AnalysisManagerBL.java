package BL;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class AnalysisManagerBL {

    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    // Method to calculate TF-IDF for individual files
    public Map<String, Map<String, Double>> analyzeIndividualTFIDF(List<String> allFiles) {
        Map<String, Map<String, Double>> individualTFIDFResults = new HashMap<>();

        // Compute term frequencies for each file
        List<Map<String, Integer>> termFrequencies = allFiles.stream()
            .map(this::calculateTermFrequency)
            .collect(Collectors.toList());

        // Compute TF-IDF for each file
        for (int i = 0; i < allFiles.size(); i++) {
            String content = allFiles.get(i);
            Map<String, Integer> termFrequency = termFrequencies.get(i);
            Map<String, Double> tfidf = calculateTFIDF(content, allFiles, termFrequency, termFrequencies);
            individualTFIDFResults.put(content, tfidf);
        }

        return individualTFIDFResults;
    }

    // Method to calculate TF-IDF for all files combined
public Map<String, Double> analyzeCombinedTFIDF(List<String> allFiles) {
    String combinedContent = String.join(" ", allFiles);  // Combine all files' content
    Map<String, Integer> combinedTermFrequency = calculateTermFrequency(combinedContent);  // Term frequencies across all files
    // Ensure termFrequencies list is populated before calling calculateTFIDF
    List<Map<String, Integer>> termFrequencies = allFiles.stream()
        .map(this::calculateTermFrequency)
        .collect(Collectors.toList());
    return calculateTFIDF(combinedContent, allFiles, combinedTermFrequency, termFrequencies);  // Compute TF-IDF across all files
}


    // Method to calculate PMI for individual files
    public Map<String, Map<String, Double>> analyzeIndividualPMI(List<String> allFiles) {
        Map<String, Map<String, Double>> individualPMIResults = new HashMap<>();

        // Compute term frequencies for each file
        List<Map<String, Integer>> termFrequencies = allFiles.stream()
            .map(this::calculateTermFrequency)
            .collect(Collectors.toList());

        // Compute PMI for each file
        for (int i = 0; i < allFiles.size(); i++) {
            String content = allFiles.get(i);
            Map<String, Integer> termFrequency = termFrequencies.get(i);
            Map<String, Double> pmi = calculatePMI(content, allFiles, termFrequency, termFrequencies);
            individualPMIResults.put(content, pmi);
        }

        return individualPMIResults;
    }

    // Method to calculate PMI for all files combined
    public Map<String, Double> analyzeCombinedPMI(List<String> allFiles) {
        String combinedContent = String.join(" ", allFiles);  // Combine all files' content
        Map<String, Integer> combinedTermFrequency = calculateTermFrequency(combinedContent);  // Term frequencies across all files
        return calculatePMI(combinedContent, allFiles, combinedTermFrequency, null);  // Compute PMI across all files
    }

    // Method to calculate PKL for individual files
    public Map<String, Map<String, Double>> analyzeIndividualPKL(List<String> allFiles) {
        Map<String, Map<String, Double>> individualPKLResults = new HashMap<>();

        // Compute PKL for each file
        for (String content : allFiles) {
            Map<String, Double> pkl = calculatePKL(content);
            individualPKLResults.put(content, pkl);
        }

        return individualPKLResults;
    }

    // Method to calculate PKL for all files combined
    public Map<String, Double> analyzeCombinedPKL(List<String> allFiles) {
        String combinedContent = String.join(" ", allFiles);  // Combine all files' content
        return calculatePKL(combinedContent);  // Calculate PKL across all files
    }

    // Helper method to calculate term frequency for a single file
    private Map<String, Integer> calculateTermFrequency(String content) {
        Map<String, Integer> termFrequency = new HashMap<>();
        String[] terms = content.split("\\s+");
        for (String term : terms) {
            term = term.toLowerCase();  // Convert to lowercase for case-insensitivity
            termFrequency.put(term, termFrequency.getOrDefault(term, 0) + 1);
        }
        return termFrequency;
    }

    // Helper method to calculate TF-IDF
    private Map<String, Double> calculateTFIDF(String content, List<String> allFiles, Map<String, Integer> termFrequency, List<Map<String, Integer>> termFrequencies) {
        Map<String, Double> tfidf = new HashMap<>();
        // List<String> termFrequencies = new ArrayList<>();
        int totalFiles = allFiles.size();

        // Calculate TF (Term Frequency)
        for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
            String term = entry.getKey();
            int termCount = entry.getValue();
            double tf = (double) termCount / content.split("\\s+").length;  // Total terms in the document
            tfidf.put(term, tf);
        }

        // Calculate IDF (Inverse Document Frequency)
        for (String term : termFrequency.keySet()) {
            int documentCount = 0;
            for (Map<String, Integer> fileTF : termFrequencies) {
                if (fileTF.containsKey(term)) {
                    documentCount++;
                }
            }
            // Use smoothing to prevent log(0)
            double idf = Math.log((totalFiles + 1) / (documentCount + 1.0));  // Smoothing
            double tfidfValue = tfidf.get(term) * idf;  // Multiply TF with IDF
            tfidf.put(term, tfidfValue);
        }

        return tfidf;
    }

    // Helper method to calculate PMI (Pointwise Mutual Information)
    private Map<String, Double> calculatePMI(String content, List<String> allFiles, Map<String, Integer> termFrequency, List<Map<String, Integer>> termFrequencies) {
        Map<String, Double> pmi = new HashMap<>();
        int totalFiles = allFiles.size();

        // Calculate PMI based on term co-occurrences
        for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
            String term = entry.getKey();
            double p_x = (double) entry.getValue() / content.split("\\s+").length;  // Probability of the term in this file
            int coOccurrenceCount = 0;

            // Calculate co-occurrence with other terms
            for (String otherTerm : termFrequency.keySet()) {
                if (!term.equals(otherTerm)) {
                    for (String file : allFiles) {
                        if (file.contains(term) && file.contains(otherTerm)) {
                            coOccurrenceCount++;
                        }
                    }
                    double p_x_y = (double) coOccurrenceCount / totalFiles;  // Co-occurrence probability
                    double pmiScore = Math.log(p_x_y / (p_x * (double) entry.getValue() / totalFiles));  // PMI calculation
                    pmi.put(term + "_" + otherTerm, pmiScore);
                }
            }
        }

        return pmi;
    }

    // Helper method to calculate PKL (Pairwise Co-occurrence)
    private Map<String, Double> calculatePKL(String content) {
        Map<String, Double> pkl = new HashMap<>();

        // For simplicity, calculate PKL as the frequency of term pairs in the document
        String[] terms = content.split("\\s+");
        for (int i = 0; i < terms.length - 1; i++) {
            String pair = terms[i] + "_" + terms[i + 1];
            pkl.put(pair, pkl.getOrDefault(pair, 0.0) + 1);
        }
        return pkl;
    }
}
