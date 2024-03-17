package common.packet;

public enum CommandType {
    FindOthers,
    ResponseToFindOthers,
    DownloadStart,
    DownloadAbort,
    NextFilePart,
    Success
}
