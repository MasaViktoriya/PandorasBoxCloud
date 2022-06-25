package ru.masaviktoria.pandorasboxapplication;

import org.apache.commons.io.FilenameUtils;
import ru.masaviktoria.pandorasboxmodel.FileContainer;
import ru.masaviktoria.pandorasboxmodel.FileListMappingInfo;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileService {

    public FileService() {
    }

    protected void saveFileFromContainer(FileContainer fileContainer, String dir) {
        try {
            Path current = Path.of(dir).resolve(fileContainer.getFileName());
            Files.write(current, fileContainer.getFileData());
            System.out.println("Received file: " + fileContainer.getFileName());
        } catch (IOException e) {
            System.out.println("File saving unsuccessful");
            e.printStackTrace();
        }
    }

    protected FileContainer uploadFile(String selectedFile, String dir) throws IOException {
        Path path = Path.of(dir).resolve(selectedFile);
        return new FileContainer(path);
    }

    protected boolean createNewLocalDirectory(String newDirectoryName, String dir) {
        if (!Files.exists(Path.of(dir).resolve(newDirectoryName))) {
            try {
                Files.createDirectory(Path.of(dir).resolve(newDirectoryName));

            } catch (IOException e) {
                System.out.println("Directory creation error");
                e.printStackTrace();
            }
        } else {
            return false;
        }
        return true;
    }

    protected boolean renameLocalFileOrDirectory(String newName, String oldName, String dir) {
        oldName = dir + "/" + oldName;
        newName = dir + "/" + newName + "." + FilenameUtils.getExtension(oldName);
        if (!Files.exists(Path.of(newName))) {
            Path source = Path.of(oldName);
            try {
                Files.move(source, source.resolveSibling(newName));
            } catch (IOException e) {
                System.out.println("File rename failed");
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    protected boolean deleteLocalFileOrDirectory(String itemToDelete, String dir) {
        itemToDelete = dir + "/" + itemToDelete;
        File file = new File(itemToDelete);
        if (Files.exists(Path.of(itemToDelete))) {
            Desktop.getDesktop().moveToTrash(file);
            System.out.println("File " + itemToDelete + " was moved to Recycle Bin");
            return true;
        }else {
            return false;
        }
    }

    protected String getSelectedFilePath(String selectedFile, String  dir) {
        Path selectedPath = Path.of(dir).resolve(selectedFile).toAbsolutePath().normalize();
        if (!Files.isDirectory(selectedPath)) {
            return String.valueOf(selectedPath);
        } else{
            return "";
        }
    }

    protected String checkLocalDirectory(String selectedItem, String dir) {
        Path selectedPath = Path.of(dir).resolve(selectedItem);
        if (Files.isDirectory(selectedPath)) {
            return selectedPath.toString();
        }else{
            return "";
        }
    }

    protected String getParentDirIfPossible(String dir) {
        if (!dir.equals(ClientConstants.LOCALROOTDIRECTORY)) {
            return Path.of(dir).getParent().toString();
        }else{
            return "";
        }
    }

    protected String getCurrentDirectory(String dir) {
        return  Path.of(dir).normalize().toAbsolutePath().toString();
    }

    protected List<FileListMappingInfo> getFileList(String dir) throws IOException {
        return Files.list(Path.of(dir))
                .map(FileListMappingInfo::new)
                .filter(FileListMappingInfo::isNotSystem)
                .filter(FileListMappingInfo::isNotHidden)
                .toList();
    }
}