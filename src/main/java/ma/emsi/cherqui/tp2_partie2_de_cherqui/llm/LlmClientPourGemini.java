package ma.emsi.cherqui.tp2_partie2_de_cherqui.llm;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.Dependent;

import java.io.Serializable;
import java.time.Duration;

/**
 * Gère l'interface avec l'API de Gemini via LangChain4j.
 * De portée @Dependent : l'instance sera supprimée quand le backing bean sera supprimé.
 */
@Dependent
public class LlmClientPourGemini implements Serializable {

    /**
     * Interface définissant les interactions avec le LLM.
     * LangChain4j fournira automatiquement l'implémentation (proxy).
     */
    public interface Assistant {
        String chat(String prompt);
    }

    private String systemRole;
    private Assistant assistant;
    private ChatMemory chatMemory;
    private ChatModel chatModel;

    public LlmClientPourGemini() {
        // 1. Récupère la clé secrète depuis une variable d'environnement
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "La clé API Gemini n'est pas définie dans la variable d'environnement GEMINI_API_KEY"
            );
        }

        // 2. Crée le modèle de chat Gemini
        this.chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.0-flash-latest")
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .build();

        // 3. Crée la mémoire (garde jusqu'à 10 messages)
        this.chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // 4. Crée l'assistant (service IA)
        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .chatMemory(chatMemory)
                .build();
    }

    /**
     * Définit le rôle système.
     * Vide la mémoire et ajoute un SystemMessage pour que le LLM prenne en compte le nouveau rôle.
     *
     * @param systemRole le rôle système (ex: "You are a helpful assistant")
     */
    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;
        // Vide la mémoire pour un contexte propre
        this.chatMemory.clear();
        // Ajoute le message système
        if (systemRole != null && !systemRole.isBlank()) {
            this.chatMemory.add(SystemMessage.from(systemRole));
        }
    }

    public String getSystemRole() {
        return systemRole;
    }

    /**
     * Envoie une question au LLM et retourne la réponse.
     *
     * @param question la question posée par l'utilisateur
     * @return la réponse du LLM
     */
    public String poserQuestion(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("La question ne peut pas être vide");
        }
        // LangChain4j gère automatiquement l'historique via chatMemory
        return assistant.chat(question);
    }

    /**
     * Réinitialise la conversation (vide la mémoire).
     */
    public void reinitialiserConversation() {
        this.chatMemory.clear();
        this.systemRole = null;
    }
}