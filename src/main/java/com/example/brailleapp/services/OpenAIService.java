package com.example.brailleapp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * OpenAI API service for text enhancement and processing
 * Provides AI-powered text improvement, summarization, and translation
 */
public class OpenAIService {
    
    private static final Logger logger = Logger.getLogger(OpenAIService.class.getName());
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    
    private String apiKey;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    
    public OpenAIService() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClients.createDefault();
    }
    
    /**
     * Set the OpenAI API key
     * @param apiKey OpenAI API key
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        logger.info("OpenAI API key set successfully");
    }
    
    /**
     * Check if API key is set
     * @return true if API key is configured
     */
    public boolean isApiKeySet() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
    
    /**
     * Enhance text using OpenAI API
     * @param text Input text to enhance
     * @return Enhanced text
     * @throws IOException if API call fails
     */
    public String enhanceText(String text) throws IOException {
        if (!isApiKeySet()) {
            throw new IllegalStateException("OpenAI API key not set");
        }
        
        String prompt = "Please improve the following text for clarity, grammar, and readability while maintaining its original meaning:\n\n" + text;
        return callOpenAI(prompt);
    }
    
    /**
     * Summarize text using OpenAI API
     * @param text Input text to summarize
     * @return Summarized text
     * @throws IOException if API call fails
     */
    public String summarizeText(String text) throws IOException {
        if (!isApiKeySet()) {
            throw new IllegalStateException("OpenAI API key not set");
        }
        
        String prompt = "Please provide a concise summary of the following text:\n\n" + text;
        return callOpenAI(prompt);
    }
    
    /**
     * Translate text using OpenAI API
     * @param text Input text to translate
     * @param targetLanguage Target language for translation
     * @return Translated text
     * @throws IOException if API call fails
     */
    public String translateText(String text, String targetLanguage) throws IOException {
        if (!isApiKeySet()) {
            throw new IllegalStateException("OpenAI API key not set");
        }
        
        String prompt = "Please translate the following text to " + targetLanguage + ":\n\n" + text;
        return callOpenAI(prompt);
    }
    
    /**
     * Extract key points from text using OpenAI API
     * @param text Input text to analyze
     * @return Key points extracted from text
     * @throws IOException if API call fails
     */
    public String extractKeyPoints(String text) throws IOException {
        if (!isApiKeySet()) {
            throw new IllegalStateException("OpenAI API key not set");
        }
        
        String prompt = "Please extract the key points from the following text in a bulleted list:\n\n" + text;
        return callOpenAI(prompt);
    }
    
    /**
     * Make text more accessible for Braille conversion
     * @param text Input text to optimize
     * @return Text optimized for Braille conversion
     * @throws IOException if API call fails
     */
    public String optimizeForBraille(String text) throws IOException {
        if (!isApiKeySet()) {
            throw new IllegalStateException("OpenAI API key not set");
        }
        
        String prompt = "Please optimize the following text for Braille conversion by improving clarity, " +
                       "removing unnecessary punctuation, and ensuring proper sentence structure:\n\n" + text;
        return callOpenAI(prompt);
    }
    
    /**
     * Call OpenAI API with the given prompt
     * @param prompt The prompt to send to OpenAI
     * @return Response from OpenAI
     * @throws IOException if API call fails
     */
    private String callOpenAI(String prompt) throws IOException {
        try {
            // Create request payload
            String requestBody = createRequestBody(prompt);
            
            // Create HTTP request
            HttpPost httpPost = new HttpPost(OPENAI_API_URL);
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
            
            // Execute request
            // Note: execute method is deprecated but still functional
            // Using try-catch to handle potential deprecation issues
            CloseableHttpResponse response = null;
            try {
                response = httpClient.execute(httpPost);
                int statusCode = response.getCode();
                
                if (statusCode == 200) {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                    return parseResponse(responseBody);
                } else {
                    String errorBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                    throw new IOException("OpenAI API error (Status: " + statusCode + "): " + errorBody);
                }
            } finally {
                if (response != null) {
                    try {
                        response.close();
                    } catch (IOException e) {
                        logger.warning("Error closing HTTP response: " + e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            logger.severe("Error calling OpenAI API: " + e.getMessage());
            throw new IOException("Failed to call OpenAI API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create request body for OpenAI API
     * @param prompt The prompt to include
     * @return JSON request body
     * @throws IOException if JSON creation fails
     */
    private String createRequestBody(String prompt) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("{");
        requestBody.append("\"model\": \"gpt-3.5-turbo\",");
        requestBody.append("\"messages\": [");
        requestBody.append("{\"role\": \"user\", \"content\": \"").append(escapeJson(prompt)).append("\"}");
        requestBody.append("],");
        requestBody.append("\"max_tokens\": 2000,");
        requestBody.append("\"temperature\": 0.7");
        requestBody.append("}");
        
        return requestBody.toString();
    }
    
    /**
     * Parse OpenAI API response
     * @param responseBody JSON response from OpenAI
     * @return Extracted text content
     * @throws IOException if JSON parsing fails
     */
    private String parseResponse(String responseBody) throws IOException {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode choices = rootNode.get("choices");
            
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.get("message");
                
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null) {
                        return content.asText();
                    }
                }
            }
            
            throw new IOException("Invalid response format from OpenAI API");
            
        } catch (Exception e) {
            logger.severe("Error parsing OpenAI response: " + e.getMessage());
            throw new IOException("Failed to parse OpenAI response: " + e.getMessage(), e);
        }
    }
    
    /**
     * Escape special characters in JSON string
     * @param text Text to escape
     * @return Escaped text
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Test the OpenAI API connection
     * @return true if API is working
     * @throws IOException if API test fails
     */
    public boolean testConnection() throws IOException {
        if (!isApiKeySet()) {
            throw new IllegalStateException("OpenAI API key not set");
        }
        
        try {
            String testPrompt = "Hello, please respond with 'API connection successful'";
            String response = callOpenAI(testPrompt);
            return response != null && !response.trim().isEmpty();
        } catch (Exception e) {
            logger.warning("OpenAI API connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get API usage information (if available in response)
     * @return API usage info or null if not available
     */
    public String getUsageInfo() {
        // This would require storing the last response and parsing usage data
        // For now, return a placeholder
        return "Usage information not available";
    }
    
    /**
     * Close the HTTP client
     */
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.warning("Error closing HTTP client: " + e.getMessage());
        }
    }
}
