import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Server server = new Server(9999, 64);

//        // добавление хендлеров (обработчиков)
        server.addHandler("GET", "/messages", (request, responseStream) -> server.outGET(responseStream,
                "404", "Hello from GET"));
//        server.addHandler("GET", "/messages", ((request, responseStream) -> {
//            try {
//                server.responseNotValid(responseStream,"404", "Not found");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }));
        server.addHandler("POST", "/messages", (request, responseStream) -> server.outPOST(responseStream,
                "404", "Hello from POST"));

        server.serverStart();
    }
}
