package ma.emsi.cherqui.tp2_partie2_de_cherqui.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.cherqui.tp2_partie2_de_cherqui.llm.LlmClientPourGemini;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class Bb implements Serializable {

    private String roleSysteme;
    private boolean roleSystemeChangeable = true;
    private List<SelectItem> listeRolesSysteme;
    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();

    @Inject
    private FacesContext facesContext;

    @Inject
    private LlmClientPourGemini llmClient;

    // === GETTERS / SETTERS ===
    public String getRoleSysteme() {
        return roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    // === RÔLES SYSTÈME ===

    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = new ArrayList<>();

            String role = """
                    You are a helpful assistant. You help the user to find the information they need.
                    If the user types a question, you answer it.
                    """;
            listeRolesSysteme.add(new SelectItem(role, "Assistant"));

            role = """
                    You are an interpreter. You translate from English to French and from French to English.
                    If the user types a French text, you translate it into English.
                    If the user types an English text, you translate it into French.
                    If the text contains only one to three words, give some examples of usage.
                    """;
            listeRolesSysteme.add(new SelectItem(role, "Traducteur Anglais-Français"));

            role = """
                    You are a travel guide. If the user types a country or town,
                    you tell them the main places to visit and the average price of a meal.
                    """;
            listeRolesSysteme.add(new SelectItem(role, "Guide touristique"));

            role = """
                    You are a famous comedian that has performed in all countries in the world.
                    You tell the best jokes in the native language of the country you are performing in.
                    When a user asks about a joke or to be entertained, you come up with 3 good jokes.
                    If the user specifies a country, you give them your best 3 jokes in that country and in its native language.
                    """;
            listeRolesSysteme.add(new SelectItem(role, "Comedian touristique"));
        }
        return this.listeRolesSysteme;
    }

    // === ENVOI DE LA QUESTION ===

    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Texte question vide",
                    "Il manque le texte de la question"
            );
            facesContext.addMessage(null, message);
            return null;
        }

        try {
            // Première question : définir le rôle système
            if (this.conversation.isEmpty() && this.roleSysteme != null && !this.roleSysteme.isBlank()) {
                llmClient.setSystemRole(this.roleSysteme);
                this.roleSystemeChangeable = false;
            }

            // Envoie la question au LLM via LangChain4j
            this.reponse = llmClient.poserQuestion(question);

            // Affiche la conversation
            afficherConversation();

        } catch (Exception e) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Problème de connexion avec l'API du LLM",
                    "Erreur : " + e.getMessage()
            );
            facesContext.addMessage(null, message);
            e.printStackTrace();
            return null;
        }

        return null;
    }

    public String nouveauChat() {
        this.conversation = new StringBuilder();
        this.question = null;
        this.reponse = null;
        this.roleSystemeChangeable = true;
        llmClient.reinitialiserConversation();
        return "index?faces-redirect=true"; // Recharge la page
    }

    private void afficherConversation() {
        this.conversation
                .append("== User:\n").append(question)
                .append("\n== Serveur:\n").append(reponse)
                .append("\n");
    }
}

