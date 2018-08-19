package login;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class App {

    public static void main(String[] args) throws Exception {

//        User testUser = udao.getUserData(3016385, "bart@gmail.com");
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/login", new LoginHandler());
        server.createContext("/static", new StaticHandler());
        server.setExecutor(null);
        server.start();
    }
}
