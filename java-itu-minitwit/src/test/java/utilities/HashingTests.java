package utilities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import services.ILogService;
import services.LogService;

import static org.mockito.Mockito.mock;

class HashingTests {

    ILogService logService = mock(LogService.class);

    Hashing getHashing() {
        return new Hashing(logService);
    }

    @Test
    void getGravatarUrlGivenEmailReturnsFormattedEmail() {
        var actual = getHashing().getGravatarUrl("test@email.dk");
        var expected = "http://www.gravatar.com/avatar/4963475569435a6b5263344c6850654c345265594f7a334c697973506a766f2f4a415236374b716634744d3d?d=identicon&s=50";
        Assertions.assertEquals(expected, actual);
    }
}
