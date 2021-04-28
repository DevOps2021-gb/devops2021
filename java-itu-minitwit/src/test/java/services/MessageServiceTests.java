package services;

import errorhandling.Failure;
import errorhandling.Success;
import model.dto.DTO;
import model.dto.MessagesPerUserDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import repository.IMessageRepository;
import repository.IUserRepository;
import repository.MessageRepository;
import repository.UserRepository;
import utilities.*;
import view.IPresentationController;
import view.PresentationController;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

class MessageServiceTests {
    IMessageRepository messageRepository = mock(MessageRepository.class);
    IUserRepository userRepository = mock(UserRepository.class);
    IPresentationController presentationController = mock(PresentationController.class);
    IResponses responses = mock(Responses.class);
    IJSONFormatter jsonFormatter = mock(JSONFormatter.class);
    IRequests requests = mock(Requests.class);
    IMetricsService metricsService = mock(MetricsService.class);

    private IMessageService GetService() {
        return new MessageService(messageRepository, userRepository, presentationController, responses, jsonFormatter, requests, metricsService);
    }

    @Test
    public void getMessagesGivenUnauthorizedDtoReturnsUnauthorized() {
        var dto = new DTO();
        dto.authorization = "unauthorized";

        var expected = "You are not authorized to use this resource!";

        when(responses.notFromSimulatorResponse()).thenReturn(expected);

        String actual = (String) GetService().getMessages(dto);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void getMessagesGivenAuthorizedDtoReturnsTweets() {
        var dto = new DTO();
        dto.authorization = "authorized";

        when(messageRepository.publicTimeline()).thenReturn(new Success<>(new ArrayList<>()));
        when(requests.isFromSimulator(dto.authorization)).thenReturn(true);

        GetService().getMessages(dto);

        verify(jsonFormatter, times(1)).tweetsToJSONResponse(any());
    }

    @Test
    public void messagesPerUserGivenRequestNotFromSimulatorReturnsUnauthorized() {
        var dto = new MessagesPerUserDTO();
        dto.authorization = "unauthorized";

        var expected = "You are not authorized to use this resource!";

        when(responses.notFromSimulatorResponse()).thenReturn(expected);

        String actual = (String) GetService().messagesPerUser(dto);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void messagesPerUserGivenExistingUserReturnsTweets() {
        var dto = new MessagesPerUserDTO();
        dto.authorization = "authorized";
        dto.username = "abc";

        when(requests.isFromSimulator(dto.authorization)).thenReturn(true);
        when(userRepository.getUserId(dto.username)).thenReturn(new Success<>(1));
        when(messageRepository.getTweetsByUsername(any(String.class))).thenReturn(new Success<>(new ArrayList<>()));

        GetService().messagesPerUser(dto);

        verify(jsonFormatter, times(1)).tweetsToJSONResponse(any());
    }

    @Test
    public void messagesPerUserGivenNonExistingUserReturnsNotFound() {
        var dto = new MessagesPerUserDTO();
        dto.authorization = "authorized";
        dto.username = "abc";

        when(requests.isFromSimulator(dto.authorization)).thenReturn(true);
        when(userRepository.getUserId(dto.username)).thenReturn(new Failure<>(new Exception()));

        GetService().messagesPerUser(dto);

        verify(responses, times(1)).respond404();
    }
}