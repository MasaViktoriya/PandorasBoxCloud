module ru.masaviktoria.pandorasboxapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires lombok;
    requires io.netty.codec;
    requires ru.masaviktoria.model;

    opens ru.masaviktoria.pandorasboxapplication to javafx.fxml;
    exports ru.masaviktoria.pandorasboxapplication;
}