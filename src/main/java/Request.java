public class Request {
    private final String requestMethod;
    private final String requestPath;

    public Request(String requestMethod, String requestPath) {
        this.requestMethod = requestMethod;
        this.requestPath = requestPath;
    }

    public String getMethod() {
        return requestMethod;
    }

    public String getPath() {
        return requestPath;
    }
}
