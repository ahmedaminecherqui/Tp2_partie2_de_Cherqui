package ma.emsi.cherqui.tp2_partie2_de_cherqui.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.cherqui.tp2_partie2_de_cherqui.exception.RequeteException;
import ma.emsi.cherqui.tp2_partie2_de_cherqui.llm.JsonUtilPourGemini;
import ma.emsi.cherqui.tp2_partie2_de_cherqui.llm.LlmInteraction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Named
@ViewScoped
public class Bb implements Serializable {

    private boolean debug;

    private String roleSysteme;
    private boolean roleSystemeChangeable = true;

    private List<SelectItem> listeRolesSysteme;

    private String question;
    private String reponse;

    private StringBuilder conversation = new StringBuilder();

    // Champs manquants dans ton code
    private String texteRequeteJson;
    private String texteReponseJson;

    // Injection du contexte JSF
    @Inject
    private FacesContext facesContext;

    // Injection du JsonUtilPourGemini (nécessaire !)
    @Inject
    private JsonUtilPourGemini jsonUtil;

    public Bb() {
        this.setDebug(true);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void toggleDebug() {
        this.debug = !this.debug;
    }

    public boolean isDebug() {
        return this.debug;
    }

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

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

    // === RÔLES SYSTÈME ===
    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {

            this.listeRolesSysteme = new ArrayList<>();

            String role = """
                    You are a helpful assistant. You help the user to find the information they need.
                    If the user type a question, you answer it.
                    """;
            listeRolesSysteme.add(new SelectItem(role, "Assistant"));

            role = """
                    You are an interpreter. You translate from English to French and from French to English.
                    If the user type a French text, you translate it into English.
                    If the user type an English text, you translate it into French.
                    If the text contains only one to three words, give some examples of usage.
                    """;
            listeRolesSysteme.add(new SelectItem(role, "Traducteur Anglais-Français"));

            role = """
                    Your are a travel guide. If the user types a country or town,
                    you tell them the main places to visit and the average price of a meal.
                    """;
            listeRolesSysteme.add(new SelectItem(role, "Guide touristique"));



            role = """
                    you are a famous commedian that has performed in all countries in the world,
                    you tell the best jokes in the native language of the contry you are performing in,
                    when a user asks about a joke or to be entertained,you come up with some good 3 jokes,
                    if the user specifies a country,you give him your best 3 jokes in that country and in its native language.
                    """;

            listeRolesSysteme.add(new SelectItem(role, "Commedian touristique"));
        }
        return this.listeRolesSysteme;
    }

    // === ENVOI DE LA QUESTION ===
    public String envoyer() {

        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        try {
            // On transmet le rôle système à JsonUtil
            jsonUtil.setSystemRole(this.roleSysteme);

            LlmInteraction interaction = jsonUtil.envoyerRequete(question);

            this.reponse = interaction.reponseExtraite();
            this.texteRequeteJson = interaction.questionJson();
            this.texteReponseJson = interaction.reponseJson();

        } catch (Exception | RequeteException e) {
            FacesMessage message =
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Problème de connexion avec l'API du LLM",
                            "Problème de connexion avec l'API du LLM : " + e.getMessage());
            facesContext.addMessage(null, message);
            return null;
        }

        // Première réponse → ajouter le rôle système
        if (this.conversation.isEmpty()) {
            this.reponse += "\n" + roleSysteme.toUpperCase(Locale.FRENCH) + "\n";
            this.roleSystemeChangeable = false;
        }

        afficherConversation();
        return null;
    }

    public String nouveauChat() {
        return "index";
    }

    private void afficherConversation() {
        this.conversation
                .append("== User:\n").append(question)
                .append("\n== Serveur:\n").append(reponse)
                .append("\n");
    }

    // Pour affichage du JSON dans la page (si debug=true)
    public String getTexteRequeteJson() {
        return texteRequeteJson;
    }

    public String getTexteReponseJson() {
        return texteReponseJson;
    }
}

