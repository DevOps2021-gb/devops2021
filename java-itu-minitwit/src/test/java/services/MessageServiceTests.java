package services;

import model.dto.DTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import repository.IMessageRepository;
import repository.IUserRepository;
import repository.MessageRepository;
import repository.UserRepository;
import utilities.*;
import view.IPresentationController;
import view.PresentationController;

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
    public void getMessagesGivenUnauthorizedDtoReturns403() {
        var dto = new DTO();
        dto.authorization = "unauthorized";

        String actual = (String) GetService().getMessages(dto);

        Assertions.assertEquals("You are not authorized to use this resource!", actual);
    }
}