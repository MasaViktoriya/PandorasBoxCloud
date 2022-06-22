package ru.masaviktoria.pandorasboxserver;

import lombok.Data;
import ru.masaviktoria.pandorasboxmodel.BoxCommand;

import java.nio.file.Path;

@Data
public class ProcessingResult {
        public BoxCommand command;
        public Path newCurrentDir;
        public String user;

        public ProcessingResult(BoxCommand command){
            this.command = command;
        }

        public ProcessingResult(BoxCommand command, Path newCurrentDir){
            this.command = command;
            this.newCurrentDir = newCurrentDir;
        }

        public ProcessingResult(BoxCommand command, Path newCurrentDir, String user) {
            this.command = command;
            this.newCurrentDir = newCurrentDir;
            this.user = user;
        }

    public ProcessingResult() {}
}
