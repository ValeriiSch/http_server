import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private final int portNumber;
    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, Map<String, Handler>> handlers;


    public Server(int socketNumber, int threadPoolSize) {
        this.portNumber = socketNumber;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.handlers = new ConcurrentHashMap<>();
    }

    public void serverStart() throws InterruptedException {
        try (final var serverSocket = new ServerSocket(portNumber)) {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                executorService.execute(() -> serverProcessing(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serverProcessing(Socket serverSocket) {
        try (final var in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
             final var out = new BufferedOutputStream(serverSocket.getOutputStream())) {
            // read only request line for simplicity //строка запроса только для чтения, для простоты
            // must be in form GET /path HTTP/1.1 //должен быть в формате GET /путь HTTP/1.1
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                serverSocket.close();
                return;
            }

            String method = parts[0];
            final String path = parts[1];
            Request request = new Request(method, path);

            if (request == null || !handlers.containsKey(request.getMethod())) {
                responseNotValid(out, "404", "Not found");
            }

            Map<String, Handler> handlerMap = handlers.get(request.getMethod());
            if (handlerMap.containsKey(request.getPath())) {
                Handler handler = handlerMap.get(request.getPath());
                handler.handle(request, out);
            } else {
                if (!validPaths.contains(request.getPath())) {
                    responseNotValid(out, "404", "Not found");
                } else {
                    defaultHandler(out, path);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void defaultHandler(BufferedOutputStream out, String path) throws IOException {
        final var filePath = Path.of(".", "public", path);
        final var mimeType = Files.probeContentType(filePath);

        // special case for classic
        if (path.equals("/classic.html")) {
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.write(content);
            out.flush();
            return;
        }

        final var length = Files.size(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }

    protected void responseNotValid(BufferedOutputStream out, String responseCode, String responseStatus) throws IOException {
        out.write((
                "HTTP/1.1 " + responseCode + " " + responseStatus + "\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    protected void addHandler(String method, String path, Handler handler) {
        if (!handlers.containsKey(method)) {
            handlers.put(method, new HashMap<>());
        }
        handlers.get(method).put(path, handler);
    }

    protected void outGET(BufferedOutputStream out, String responseCode, String responseStatus) throws IOException {
        out.write((
                "HTTP/1.1 " + responseCode + " " + responseStatus + "\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    protected void outPOST(BufferedOutputStream out, String responseCode, String responseStatus) throws IOException {
        out.write((
                "HTTP/1.1 " + responseCode + " " + responseStatus + "\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

}






