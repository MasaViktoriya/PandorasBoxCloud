module ru.masaviktoria.pandorasboxapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.codec;
    requires ru.masaviktoria.model;
    requires org.apache.commons.io;
    requires java.desktop;

    opens ru.masaviktoria.pandorasboxapplication to javafx.fxml;
    exports ru.masaviktoria.pandorasboxapplication;
}