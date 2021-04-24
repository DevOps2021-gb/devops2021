package services;

import model.dto.AddMessageDTO;
import model.dto.DTO;
import model.dto.MessagesPerUserDTO;
import repository.IMessageRepository;
import repository.IUserRepository;
import utilities.JSON;
import org.eclipse.jetty.http.HttpStatus;
import utilities.Responses;
import view.Presentation;

import static services.MetricsService.updateLatest;
import static utilities.Requests.*;
import static spark.Spark.halt;

public class MessageService implements IMessageService {

    private final IMessageRepository messageRepository;
    private final IUserRepository userRepository;

    public MessageService(IMessageRepository _messageRepository, IUserRepository _userRepository) {
        messageRepository = _messageRepository;
        userRepository = _userRepository;
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
        updateLatest(dto.latest);

        if (!isFromSimulator(dto.authorization)) {
            return Responses.notFromSimulatorResponse();
        }

        var tweets = messageRepository.publicTimeline().get();
        return JSON.tweetsToJSONResponse(tweets);
    }

    public Object messagesPerUser(MessagesPerUserDTO dto) {
        updateLatest(dto.latest);

        if (!isFromSimulator(dto.authorization)) {
            return Responses.notFromSimulatorResponse();
        }

        var userIdResult = userRepository.getUserId(dto.username);

        if (!userIdResult.isSuccess()) {
            Responses.setStatus(HttpStatus.NOT_FOUND_404);
            Responses.setType(JSON.APPLICATION_JSON);
            return Responses.respond404();
        } else {
            var tweets = messageRepository.getTweetsByUsername(dto.username).get();
            return JSON.tweetsToJSONResponse(tweets);
        }
    }
    /*
    Registers a new message for the user.
     */
    public void addMessage(AddMessageDTO dto) {
        updateLatest(dto.latest);

        if(dto.username == null){
            if (!isUserLoggedIn(dto.userId)) {
                halt(401, "You need to sign in to post a message");
            }
        }
        else {
            var userIdRes = userRepository.getUserId(dto.username);

            if (!userIdRes.isSuccess()) {
                Responses.setStatus(HttpStatus.NO_CONTENT_204);
                return;
            }

            dto.userId = userIdRes.get();
        }

        var rs = messageRepository.addMessage(dto.content, dto.userId);
        if (rs.isSuccess()){
            if (isFromSimulator(dto.authorization)) {
                Responses.setStatus(HttpStatus.NO_CONTENT_204);
            } else {
                putAttribute(FLASH, "Your message was recorded");
                Presentation.redirect("/");
            }
        } else {
            if (isFromSimulator(dto.authorization)) {
                Responses.setStatus(HttpStatus.FORBIDDEN_403);
            } else {
                Presentation.redirect("/");
            }
        }

    }
}
