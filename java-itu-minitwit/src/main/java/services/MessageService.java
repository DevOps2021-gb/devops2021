package services;

import model.dto.AddMessageDTO;
import model.dto.DTO;
import model.dto.MessagesPerUserDTO;
import repository.IMessageRepository;
import repository.IUserRepository;
import utilities.IJSONFormatter;
import utilities.IRequests;
import utilities.IResponses;
import utilities.JSONFormatter;
import org.eclipse.jetty.http.HttpStatus;
import view.IPresentationController;

import static spark.Spark.halt;

public class MessageService implements IMessageService {

    private final IMessageRepository messageRepository;
    private final IUserRepository userRepository;
    private final IPresentationController presentationController;
    private final IResponses responses;
    private final IJSONFormatter jsonFormatter;
    private final IRequests requests;
    private final IMetricsService metricsService;

    public MessageService(
            IMessageRepository messageRepository,
            IUserRepository userRepository,
            IPresentationController presentationController,
            IResponses responses,
            IJSONFormatter jsonFormatter,
            IRequests requests,
            IMetricsService metricsService) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.presentationController = presentationController;
        this.responses = responses;
        this.jsonFormatter = jsonFormatter;
        this.requests = requests;
        this.metricsService = metricsService;
    }

    // templates
    public static final String TIMELINE_HTML = "timeline.html";
    public static final String REGISTER_HTML = "register.html";
    public static final String LOGIN_HTML = "login.html";

    // context fields
    public static final String FLASH    = "flash";
    public static final String ERROR    = "error";
    public static final String USER_ID  = "userId";
    public static final String USER     = "user";
    public static final String USERNAME = "username";
    public static final String EMAIL    = "email";
    public static final String PASSWORD = "password";
    public static final String ENDPOINT = "endpoint";
    public static final String MESSAGES = "messages";
    public static final String TITLE    = "title";
    public static final String CONTENT  = "content";

    public Object getMessages(DTO dto) {
        metricsService.updateLatest(dto.latest);

        if (!requests.isFromSimulator(dto.authorization)) {
            return responses.notFromSimulatorResponse();
        }

        var tweets = messageRepository.publicTimeline().get();
        return jsonFormatter.tweetsToJSONResponse(tweets);
    }

    public Object messagesPerUser(MessagesPerUserDTO dto) {
        metricsService.updateLatest(dto.latest);

        if (!requests.isFromSimulator(dto.authorization)) {
            return responses.notFromSimulatorResponse();
        }

        var userIdResult = userRepository.getUserId(dto.username);

        if (!userIdResult.isSuccess()) {
            responses.setStatus(HttpStatus.NOT_FOUND_404);
            responses.setType(JSONFormatter.APPLICATION_JSON);
            return responses.respond404();
        } else {
            var tweets = messageRepository.getTweetsByUsername(dto.username).get();
            return jsonFormatter.tweetsToJSONResponse(tweets);
        }
    }
    /*
    Registers a new message for the user.
     */
    public void addMessage(AddMessageDTO dto) {
        metricsService.updateLatest(dto.latest);

        if(dto.username == null){
            if (!requests.isUserLoggedIn(dto.userId)) {
                halt(401, "You need to sign in to post a message");
            }
        }
        else {
            var userIdRes = userRepository.getUserId(dto.username);

            if (!userIdRes.isSuccess()) {
                responses.setStatus(HttpStatus.NO_CONTENT_204);
                return;
            }

            dto.userId = userIdRes.get();
        }

        var rs = messageRepository.addMessage(dto.content, dto.userId);
        if (rs.isSuccess()){
            if (requests.isFromSimulator(dto.authorization)) {
                responses.setStatus(HttpStatus.NO_CONTENT_204);
            } else {
                requests.putAttribute(FLASH, "Your message was recorded");
                presentationController.redirect("/");
            }
        } else {
            if (requests.isFromSimulator(dto.authorization)) {
                responses.setStatus(HttpStatus.FORBIDDEN_403);
            } else {
                presentationController.redirect("/");
            }
        }

    }
}
