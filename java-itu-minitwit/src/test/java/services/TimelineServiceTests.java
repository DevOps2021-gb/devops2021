package services;

import errorhandling.Success;
import model.User;
import model.dto.PublicTimelineDTO;
import model.dto.TimelineDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import repository.*;
import utilities.IRequests;
import utilities.Requests;
import view.IPresentationController;
import view.PresentationController;

import java.util.ArrayList;
import java.util.HashMap;

import static org.mockito.Mockito.*;

public class TimelineServiceTests {
    IUserRepository userRepository = mock(UserRepository.class);
    IFollowerRepository followerRepository = mock(FollowerRepository.class);
    IMessageRepository messageRepository = mock(MessageRepository.class);
    IPresentationController presentationController = mock(PresentationController.class);
    IRequests requests = mock(Requests.class);
    IMetricsService metricsService = mock(MetricsService.class);

    private ITimelineService GetService() {
        return new TimelineService(followerRepository, messageRepository, userRepository, presentationController, requests, metricsService);
    }

    @Test
    public void publicTimelineGivenDtoWithUserLoggedInCallsUserRepository() {

        //Arrange
        var dto = new PublicTimelineDTO();
        dto.latest = "1";
        dto.loggedInUser = 12;

        User user = new User();
        user.setUsername("username");

        when(messageRepository.publicTimeline()).thenReturn(new Success<>(new ArrayList<>()));
        when(userRepository.getUserById(dto.loggedInUser)).thenReturn(new Success<>(user));

        //Act
        GetService().publicTimeline(dto);

        //Assert
        verify(userRepository, times(1)).getUserById(dto.loggedInUser);
    }

    @Test
    public void publicTimelineGivenDtoWithoutUserLoggedInDoesNotCallUserRepository() {
        //Arrange
        var dto = new PublicTimelineDTO();
        dto.latest = "1";
        dto.loggedInUser = null;
        dto.flash = "flashmsg";

        when(messageRepository.publicTimeline()).thenReturn(new Success<>(new ArrayList<>()));

        //Act
        GetService().publicTimeline(dto);

        //Assert
        verify(userRepository, never()).getUserById(any(Integer.class));
    }

    @Test
    public void timelineGivenDtoWithNoUseridReturnsEmptyString() {
        //Arrange
        var dto = new TimelineDTO();
        dto.latest = "1";
        dto.userId = null;

        //Act
        var actual = (String) GetService().timeline(dto);

        //Assert
        Assertions.assertEquals("", actual);
    }

    @Test
    public void timelineGivenDtoWithLoggedInUserShowsTweets() {
        //Arrange
        var dto = new TimelineDTO();
        dto.latest = "1";
        dto.userId = 12;

        User user = new User();
        user.setUsername("username");
        user.id = 12;


        when(userRepository.getUserById(dto.userId)).thenReturn(new Success<>(user));
        when(messageRepository.getPersonalTweetsById(dto.userId)).thenReturn(new Success<>(new ArrayList<>()));

        //Act
        GetService().timeline(dto);

        //Assert
        verify(presentationController, times(1)).renderTemplate(any(String.class), any(HashMap.class));
    }
}
