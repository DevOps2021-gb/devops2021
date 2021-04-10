package model.dto;

public class MessagesPerUserDTO extends DTO {
    public String username; //getParam(":username", request).get();
    public Integer userId;
    public Object flash;
}
