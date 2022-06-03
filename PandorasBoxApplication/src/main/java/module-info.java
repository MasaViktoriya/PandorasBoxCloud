module ru.masaviktoria.pandorasboxapplication {
    requires javafx.controls;
    requires javafx.fxml;


    opens ru.masaviktoria.pandorasboxapplication to javafx.fxml;
    exports ru.masaviktoria.pandorasboxapplication;
}