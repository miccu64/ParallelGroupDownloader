package common;

import common.exceptions.ConfigurationException;
import common.exceptions.DownloadException;
import common.models.UdpcastConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0", "1", "1023", "65536", "a", "", "2222.2"})
    public void shouldThrowForImproperPortbases(String port) {
        // Arrange
        String[] args = new String[]{"-portbase", port};

        // Act / Assert
        Assertions.assertThrows(ConfigurationException.class, () -> new UdpcastConfiguration(args));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1024", "9000", "65535"})
    public void shouldWorkForProperPortbases(String port) {
        // Arrange
        String[] args = new String[]{"-portbase", port};

        // Act / Assert
        Assertions.assertDoesNotThrow(() -> new UdpcastConfiguration(args));
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

    @ParameterizedTest
    @ValueSource(strings = {"0", "1", "29"})
    public void shouldWorkForProperDelays(String delay) {
        // Arrange
        String[] args = new String[]{"-url", "test", "-delay", delay};

        // Act / Assert
        Assertions.assertDoesNotThrow(() -> new UdpcastConfiguration(args));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "30", "", "a", "1.1"})
    public void shouldThrowForImproperDelays(String delay) {
        // Arrange
        String[] args = new String[]{"-url", "test", "-delay", delay};

        // Act / Assert
        Assertions.assertThrowsExactly(ConfigurationException.class, () -> new UdpcastConfiguration(args));
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
    public void shouldAcceptFileName() {
        // Arrange
        String[] args = new String[]{"-filename", "test"};

        // Act / Assert
        Assertions.assertDoesNotThrow(() -> new UdpcastConfiguration(args));
    }

    @Test
    public void shouldAcceptBlockSizeWhenUrlIsGiven() {
        // Arrange
        String[] args = new String[]{"-url", "test", "-blocksize", "1"};

        // Act / Assert
        Assertions.assertDoesNotThrow(() -> new UdpcastConfiguration(args));
    }

    @ParameterizedTest
    @ValueSource(ints = {-11, -1, 0})
    public void shouldThrowOnImproperBlockSizes(int number) {
        // Arrange
        String[] args = new String[]{"-blocksize", String.valueOf(number)};

        // Act / Assert
        Assertions.assertThrowsExactly(ConfigurationException.class, () -> new UdpcastConfiguration(args));
    }

    @Test
    public void shouldAcceptBlockSizeWhenUrlIsNotGiven() {
        // Arrange
        String[] args = new String[]{"-blocksize", "1"};

        // Act / Assert
        Assertions.assertThrowsExactly(ConfigurationException.class, () -> new UdpcastConfiguration(args));
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
