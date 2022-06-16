package ru.masaviktoria.pandorasboxserver;

import lombok.Data;
import ru.masaviktoria.pandorasboxmodel.BoxCommand;

import java.nio.file.Path;

@Data
public class HandlingResult {
        public BoxCommand command;
        public Path newCurrentDir;
        public String user;
/*        private enum StateField {
            CURRENTDIR, USER;
            StateField(){}
        }
        private HashMap<StateField, Object> updatedFields;*/

        public  HandlingResult(BoxCommand command){
            this.command = command;
        }

        public HandlingResult(BoxCommand command, Path newCurrentDir){
            this.command = command;
            this.newCurrentDir = newCurrentDir;
        }

        public  HandlingResult(BoxCommand command, Path newCurrentDir, String user) {
            this.command = command;
            this.newCurrentDir = newCurrentDir;
            this.user = user;
        }

    public HandlingResult() {}
}
