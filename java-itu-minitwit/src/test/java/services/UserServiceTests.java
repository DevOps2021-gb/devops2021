package services;

import errorhandling.Success;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import repository.FollowerRepository;
import repository.IFollowerRepository;
import repository.IUserRepository;
import repository.UserRepository;
import utilities.IRequests;
import utilities.IResponses;
import utilities.Requests;
import utilities.Responses;
import view.IPresentationController;
import view.PresentationController;

import static org.mockito.Mockito.*;

class UserServiceTests {

    IUserRepository userRepository = mock(UserRepository.class);
    IFollowerRepository followerRepository = mock(FollowerRepository.class);
    IPresentationController presentationController = mock(PresentationController.class);
    IResponses responses = mock(Responses.class);
    IRequests requests = mock(Requests.class);
    IMetricsService metricsService = mock(MetricsService.class);

    private IUserService GetService() {
        return new UserService(followerRepository, userRepository, presentationController, responses, requests, metricsService);
    }

    @Test
    void validateUserCredentialsGivenAlreadyExistingUserReturnsUserAlreadyExists() {
        when(userRepository.getUserId(any())).thenReturn(new Success<>(123));

        var result = GetService().validateUserCredentials("user1", "test@test.dk", "q123", "q123");
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("The username is already taken",result.getFailureMessage());
    }

    @Test
    void validateUserCredentialsGivenNoUsernameReturnsMissingUsername() {
        var error = GetService().validateUserCredentials("", "q123", null, null);
        Assertions.assertFalse(error.isSuccess());
        Assertions.assertEquals("You have to enter a username", error.getFailureMessage());
    }

    @Test
    void validateUserCredentialsGivenNoPasswordReturnsMissingPassword() {
        var error = GetService().validateUserCredentials("user2", "test@test.dk", "", null);
        Assertions.assertFalse(error.isSuccess());
        Assertions.assertEquals("You have to enter a password", error.getFailureMessage());
    }

    @Test
    void validateUserCredentialsGivenNonMatchingPasswordsReturnsNonMatchingPasswords() {
        var error = GetService().validateUserCredentials("user2", "test@test.dk", "2", "1");
        Assertions.assertFalse(error.isSuccess());
        Assertions.assertEquals("The two passwords do not match", error.getFailureMessage());
    }

    @Test
    void validateUserCredentialsGivenInvalidEmailReturnsInvalidEmail() {
        var error = GetService().validateUserCredentials("user2", "1", null, "bad email");
        Assertions.assertFalse(error.isSuccess());
        Assertions.assertEquals("You have to enter a valid email address", error.getFailureMessage());
    }
}
