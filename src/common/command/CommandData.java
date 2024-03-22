package common.command;

import common.DownloadException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandData {
    private static final String separator = "&&&";
    private static final String kvSeparator = "=";

    private final Map<String, String> data = new HashMap<>();
    private final CommandType commandType;

    public CommandType getCommandType() {
        return commandType;
    }

    public CommandData(CommandType commandType, Map<String, String> data) {
        this.commandType = commandType;
        this.data.put("CommandType", String.valueOf(commandType));

        if (data != null) {
            this.data.putAll(data);
        }
    }

    public CommandData(String dataString) throws DownloadException {
        if (!dataString.startsWith(separator) || !dataString.endsWith(separator)) {
            throw new DownloadException(new Exception(), "Wrong message data");
        }

        dataString = dataString.substring(0, dataString.length() - separator.length()).substring(separator.length());

        for (String keyValueJoined : dataString.split(separator)) {
            String[] keyValueSplit = keyValueJoined.split(kvSeparator, 2);
            data.put(keyValueSplit[0], keyValueSplit[1]);
        }

        commandType = CommandType.valueOf(data.get("CommandType"));
    }

    @Override
    public String toString() {
        return separator + data.entrySet().stream()
                .map(e -> e.getKey() + kvSeparator + e.getValue())
                .collect(Collectors.joining(separator)) + separator;
    }
}
