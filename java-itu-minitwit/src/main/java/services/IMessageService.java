package services;

import model.dto.AddMessageDTO;
import model.dto.DTO;
import model.dto.MessagesPerUserDTO;

public interface IMessageService {
    Object getMessages(DTO dto);
    Object messagesPerUser(MessagesPerUserDTO dto);
    void addMessage(AddMessageDTO dto);
}
