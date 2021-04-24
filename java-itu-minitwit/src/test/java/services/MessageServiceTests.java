package services;

import repository.MessageRepository;
import repository.UserRepository;

import static org.mockito.Mockito.*;

class MessageServiceTests {

    //https://www.tutorialspoint.com/mockito/mockito_first_application.htm
    private IMessageService GetService() {
        return new MessageService(mock(MessageRepository.class), mock(UserRepository.class));
    }
}