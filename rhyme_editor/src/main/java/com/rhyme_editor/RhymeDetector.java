package com.rhyme_editor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * RhymeDetector - Enhanced version with improved phonetic rhyme detection
 * 
 * Analyzes text and groups words that rhyme based on their phonetic endings.
 * Uses vowel-consonant patterns for more accurate rhyme matching.
 */
public class RhymeDetector {

    private static final int MIN_WORD_LENGTH = 3;
    private static final Pattern VOWELS = Pattern.compile("[aeiou]");

    /**
     * Analyzes text and groups words that rhyme.
     *
     * @param text Input text.
     * @return Map of rhyme endings to lists of words that rhyme.
     */
    public Map<String, List<String>> findRhymes(String text) {
        Map<String, List<String>> rhymeGroups = new HashMap<>();
        String[] words = cleanWords(text);
        
        // Group words by their phonetic endings
        Map<String, List<String>> endingMap = new HashMap<>();
        
        for (String word : words) {
            if (word.length() < MIN_WORD_LENGTH) continue;
            
            String phoneticEnding = getPhoneticEnding(word);
            if (phoneticEnding == null || phoneticEnding.length() < 2) continue;
            
            endingMap.putIfAbsent(phoneticEnding, new ArrayList<>());
            
            // Avoid duplicates
            if (!endingMap.get(phoneticEnding).contains(word)) {
                endingMap.get(phoneticEnding).add(word);
            }
        }
        
        // Only keep groups with 2+ words (actual rhymes)
        for (Map.Entry<String, List<String>> entry : endingMap.entrySet()) {
            if (entry.getValue().size() >= 2) {
                rhymeGroups.put(entry.getKey(), entry.getValue());
            }
        }
        
        // Filter out overly common patterns
        filterCommonPatterns(rhymeGroups);
        
        return rhymeGroups;
    }

    /**
     * Extracts phonetic ending for rhyme matching
     * Focuses on the last vowel sound + following consonants
     */
    private String getPhoneticEnding(String word) {
        if (word == null || word.length() < 2) return null;
        
        word = word.toLowerCase();
        
        // Find the last vowel in the word
        int lastVowelIndex = -1;
        for (int i = word.length() - 1; i >= 0; i--) {
            if (isVowel(word.charAt(i))) {
                lastVowelIndex = i;
                break;
            }
        }
        
        // If no vowel found or vowel is first char, use last 3 chars
        if (lastVowelIndex <= 0) {
            return word.length() >= 3 ? word.substring(word.length() - 3) : word;
        }
        
        // Return from last vowel to end (captures rhyme sound)
        String ending = word.substring(lastVowelIndex);
        
        // If ending is too short, include one more char before
        if (ending.length() < 2 && lastVowelIndex > 0) {
            ending = word.substring(lastVowelIndex - 1);
        }
        
        return ending;
    }
    
    private boolean isVowel(char c) {
        return "aeiou".indexOf(Character.toLowerCase(c)) != -1;
    }

    /**
     * Removes overly common patterns that don't represent meaningful rhymes
     */
    private void filterCommonPatterns(Map<String, List<String>> rhymeGroups) {
        // Remove groups that are too large (likely false positives)
        rhymeGroups.entrySet().removeIf(entry -> entry.getValue().size() > 15);
        
        // Filter single character endings
        rhymeGroups.entrySet().removeIf(entry -> entry.getKey().length() < 2);
        
        // Remove common suffixes that aren't real rhymes
        Set<String> commonSuffixes = new HashSet<>(Arrays.asList(
            "e", "s", "ed", "er", "ly", "ing", "ion", "tion"
        ));
        
        rhymeGroups.entrySet().removeIf(entry -> 
            commonSuffixes.contains(entry.getKey()) && entry.getValue().size() > 8
        );
    }

    /**
     * Cleans and tokenizes the input text
     */
    private String[] cleanWords(String sentence) {
        if (sentence == null || sentence.trim().isEmpty()) {
            return new String[0];
        }
        
        // Remove punctuation but preserve apostrophes in contractions
        String cleaned = sentence.replaceAll("[^a-zA-Z'\\s]", "").toLowerCase();
        
        // Split and filter empty strings
        return Arrays.stream(cleaned.split("\\s+"))
                     .filter(word -> !word.isEmpty() && word.length() >= MIN_WORD_LENGTH)
                     .distinct() // Remove duplicates from the same position
                     .toArray(String[]::new);
    }
    
    /**
     * Advanced phonetic matching using simplified Soundex-like algorithm
     * Returns true if two words likely rhyme based on phonetic similarity
     */
    public boolean doWordsRhyme(String word1, String word2) {
        if (word1 == null || word2 == null) return false;
        if (word1.equals(word2)) return false; // Same word doesn't count
        
        String ending1 = getPhoneticEnding(word1.toLowerCase());
        String ending2 = getPhoneticEnding(word2.toLowerCase());
        
        if (ending1 == null || ending2 == null) return false;
        
        // Check if endings match or are very similar
        return ending1.equals(ending2) || 
               areSimilarEndings(ending1, ending2);
    }
    
    /**
     * Checks if two endings are phonetically similar
     */
    private boolean areSimilarEndings(String end1, String end2) {
        // Allow one character difference for near-rhymes
        if (Math.abs(end1.length() - end2.length()) > 1) return false;
        
        int minLen = Math.min(end1.length(), end2.length());
        if (minLen < 2) return false;
        
        // Check if last 2 characters match
        String suffix1 = end1.substring(Math.max(0, end1.length() - 2));
        String suffix2 = end2.substring(Math.max(0, end2.length() - 2));
        
        return suffix1.equals(suffix2);
    }
}