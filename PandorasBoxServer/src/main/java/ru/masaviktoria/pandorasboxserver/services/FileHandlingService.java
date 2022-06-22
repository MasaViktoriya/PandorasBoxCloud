package ru.masaviktoria.pandorasboxserver.services;

import ru.masaviktoria.pandorasboxmodel.FileContainer;
import ru.masaviktoria.pandorasboxmodel.FileList;
import ru.masaviktoria.pandorasboxmodel.FileRequest;
import ru.masaviktoria.pandorasboxserver.ProcessingResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileHandlingService {

    public static ProcessingResult fileRequestHandle(FileRequest fileRequest, Path currentDir) throws IOException {
        System.out.println("File " + fileRequest.getName() + " sent to user");
        return new ProcessingResult(new FileContainer(currentDir.resolve(fileRequest.getName())));
    }

    //todo: допилить информацию о файле в базу
    public static ProcessingResult fileContainerHandle(FileContainer fileContainer, Path currentDir, String user) throws IOException {
        Files.write(currentDir.resolve(fileContainer.getFileName()), fileContainer.getFileData());
        //fileContainer.setFileOwner(user);
        //fileContainer.setFileServerPath(currentDir.resolve(fileContainer.getFileName()).toString());
        //SQLService.insertToFileInformation(fileContainer);
        System.out.println("File " + fileContainer.getFileName() + " saved on server");
        return new ProcessingResult(new FileList(currentDir));
    }
}
