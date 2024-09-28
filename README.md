# ParallelGroupDownloader

The work presents the ParallelGroupDownloader tool created in Java and is used to copy files within a computer room. It was designed as an overlay for the open-source UDPCast application in order to expand the program's functionality, make it simpler and automate its use. The main assumptions of the project are to achieve the highest possible download speed using the full potential of the network and devices, reliability and auto-configuration.

## Expectations

Before implementing the program, detailed expectations regarding the functionality of the solution were collected. First, they served as a pointer to find an existing application that would have certain features, but when it turned out that such a program did not exist, these requirements became the basis for the ParallelGroupDownloader project.
Requirements specification:
- code written in Java without additional dependencies and libraries to ensure greater portability,
- using the UDPCast program for fast data transfer within one network,
- auto-configuration to minimize the need for user interaction,
- selection of operating parameters based on the current system configuration,
- automatic detection of whether the machine on which the program is running can download the file,
- support for downloading files located on a local disk or in a network location,
- maximum resistance to failures,
- Linux support,
- clear status after completion of work by handling Unix signaling of program success - the return value indicating success is zero, another status means error,
- in case of a download error, the downloaded data will be deleted to free up space,
- ensuring error-free file transfer,
- configuration of parameters from the command line,
- reporting of transfer progress at the user's request,
- highest possible use of the local network,
- generally understood operational effectiveness.

## Tech stack
- Java 8
- JUnit 5
- UDPCast

## Requirements
The application needs:
- Linux or Windows operating system,
- installed Java runtime environment version 8 or higher,
- in the case of Linux, make sure to have the GLIBC library version 2.34 or higher (built-in system library),
- a network card that supports multicast.

### How application works
The program was created in the client-server model, which results from the operation of the UDPCast tool used. Detailed operation diagram common to both sides of the model:
1. Reading the parameters transferred when starting the program. 
   1. If a URL is passed, the application runs in server mode, otherwise it becomes a client.
   2. If an incorrect command is passed or a help command is used, the operating instructions are displayed and the program closes with a success code.
2. Extract UDPCast to the default system temporary directory and try to run it to select the appropriate version. If none of them work, the program exits with an error.
3. (Server only) Validate the URL, start downloading the file and get information about it.
   1. If it is possible to write directly to RAM, part of the source file is directed there, otherwise it is saved to the indicated location.
4. Receiving/sending a startup file with information and processing it.
   1. Check whether there is enough free space to save data in the specified location.
5. Transfer of subsequent parts of the correct file from the server to clients.
   1. The file fragment is downloaded to RAM if possible, otherwise directly to disk.
   2. After downloading, the fragment is passed to a separate thread, which calculates the checksum, saves it and then combines the current part of the file with the target file, and then removes the unnecessary part.
6. (Server only) Upload the final file containing checksums and program termination.
7. (Customer only) Checking each received file as soon as it is acquired until it is recognized as the final file.  
   1. Comparison of calculated and downloaded checksums.
   2. End of program.

### Building
In order to build one needs to run build.sh script. This script works only on Linux OS.
