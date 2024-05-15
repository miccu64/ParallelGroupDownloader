package common;

import common.exceptions.ConfigurationException;
import common.models.UdpcastConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UdpcastConfigurationTests {
    @Test
    public void shouldWorkWithEmptyArgs() {
        // Arrange
        String[] args = new String[]{};

        // Act / Assert
        Assertions.assertDoesNotThrow(() -> new UdpcastConfiguration(args));
    }

    @Test
    public void shouldThrowWhenNotKnownArg() {
        // Arrange
        String[] args = new String[]{"-test", "test"};

        // Act / Assert
        Assertions.assertThrowsExactly(ConfigurationException.class, () -> new UdpcastConfiguration(args));
    }

    @Test
    public void shouldThrowWhenKeyHaveNotValue() {
        // Arrange
        String[] args = new String[]{"-interface"};

        // Act / Assert
        Assertions.assertThrowsExactly(ConfigurationException.class, () -> new UdpcastConfiguration(args));
    }

    @Test
    public void shouldThrowForImproperPortbases() {
        // Arrange
        String[] ports = new String[]{"-1", "0", "1", "1023", "65536", "a", "", "2222.2"};

        // Act / Assert
        for (String port : ports) {
            String[] args = new String[]{"-portbase", port};
            Assertions.assertThrows(ConfigurationException.class, () -> new UdpcastConfiguration(args));
        }
    }

    @Test
    public void shouldWorkForProperPortbases() {
        // Arrange
        String[] ports = new String[]{"1024", "9000", "65535"};

        // Act / Assert
        for (String port : ports) {
            String[] args = new String[]{"-portbase", port};
            Assertions.assertDoesNotThrow(() -> new UdpcastConfiguration(args));
        }
    }

    @Test
    public void shouldAcceptTestInterface() {
        // Arrange
        String[] args = new String[]{"-interface", "test"};

        // Act / Assert
        Assertions.assertDoesNotThrow(() -> new UdpcastConfiguration(args));
    }

    @Test
    public void shouldThrowWhenInterfaceIsEmpty() {
        // Arrange
        String[] args = new String[]{"-interface", ""};

        // Act / Assert
        Assertions.assertThrowsExactly(ConfigurationException.class, () -> new UdpcastConfiguration(args));
    }

    @Test
    public void shouldWorkForProperDelays() {
        // Arrange
        String[] delays = new String[]{"0", "1", "29"};

        // Act / Assert
        for (String delay : delays) {
            String[] args = new String[]{"-url", "test", "-delay", delay};
            Assertions.assertDoesNotThrow(() -> new UdpcastConfiguration(args));
        }
    }

    @Test
    public void shouldThrowForImproperDelays() {
        // Arrange
        String[] delays = new String[]{"-1", "30", "", "a", "1.1"};

        // Act / Assert
        for (String delay : delays) {
            String[] args = new String[]{"-url", "test", "-delay", delay};
            Assertions.assertThrowsExactly(ConfigurationException.class, () -> new UdpcastConfiguration(args));
        }
    }

    @Test
    public void shouldThrowForDelayWhenUrlIsNotGiven() {
        // Arrange
        String[] args = new String[]{"-delay", "11"};

        // Act / Assert
        Assertions.assertThrowsExactly(ConfigurationException.class, () -> new UdpcastConfiguration(args));
    }

    @Test
    public void shouldAcceptTestUrl() {
        // Arrange
        String[] args = new String[]{"-url", "test"};

        // Act / Assert
        Assertions.assertDoesNotThrow(() -> new UdpcastConfiguration(args));
    }

    @Test
    public void shouldThrowWhenUrlIsEmpty() {
        // Arrange
        String[] args = new String[]{"-url", ""};

        // Act / Assert
        Assertions.assertThrowsExactly(ConfigurationException.class, () -> new UdpcastConfiguration(args));
    }

    @Test
    public void shouldAcceptDirectory() {
        // Arrange
        String[] args = new String[]{"-directory", "test"};

        // Act / Assert
        Assertions.assertDoesNotThrow(() -> new UdpcastConfiguration(args));
    }

    @Test
    public void shouldDetectHelp() throws ConfigurationException {
        // Arrange
        String[] args = new String[]{"-help"};

        // Act
        UdpcastConfiguration udpcastConfiguration = new UdpcastConfiguration(args);

        // Assert
        Assertions.assertTrue(udpcastConfiguration.isHelpInvoked());
    }
}
