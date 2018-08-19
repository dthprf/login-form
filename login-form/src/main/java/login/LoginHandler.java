package login;

import com.google.common.io.Resources;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import login.Data.PostgresUserDAO;
import login.Model.User;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.*;
import java.net.HttpCookie;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public class LoginHandler implements HttpHandler {

    private Map<String, String> sessionIDs = new HashMap<>();
    private PostgresUserDAO userDAO = new PostgresUserDAO();
    Integer sessionCounter = 0;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        final String GET_METHOD = "GET";
        final String POST_METHOD = "POST";
        String method = httpExchange.getRequestMethod();
        HttpCookie cookie = null;
        cookie = getCookie(httpExchange, cookie);

        if (method.equals(GET_METHOD)) {

            if(isSessionValid(sessionIDs, cookie)) {
                String login = sessionIDs.get(cookie.toString());
                User activeUser = userDAO.getUserByLogin(login);
                sendPersonalizedPage(httpExchange, activeUser);

            } else {
                sendLoginPage(httpExchange);
            }
        }

        if (method.equals(POST_METHOD)) {

            if (isSessionValid(sessionIDs, cookie)) {
                sessionIDs.remove(cookie.toString());
                sendLoginPage(httpExchange);

            } else {
                Map formData = getFormData(httpExchange);
                User accessingUser = userDAO.getUserData(
                        formData.get("login").toString(),
                        formData.get("password").toString().hashCode());
                User comparingUser = userDAO.getUserByLogin(formData.get("login").toString());

                try {
                    loginValidation(httpExchange, accessingUser, comparingUser, cookie);

                } catch (IOException e) {
                    System.out.println("An error occured.");
                }
            }
        }
    }

    private void loginValidation(HttpExchange httpExchange, User accesingUser, User userToCompare, HttpCookie cookie)
            throws IOException {

        if (accesingUser == null || userToCompare == null) {
            sendLoggingErrorPage(httpExchange);
        }

        if (accesingUser.equals(userToCompare)) {
            sessionIDs.put(cookie.toString(), accesingUser.getLogin());
            sendPersonalizedPage(httpExchange, accesingUser);

        } else {
            sendLoggingErrorPage(httpExchange);
        }
    }

    private Map<String, String> getFormData(HttpExchange httpExchange) throws IOException{
        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String formData = br.readLine();
        Map inputs = parseFormData(formData);

        return inputs;
    }

    private static Map<String, String> parseFormData(String formData) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        String[] pairs = formData.split("&");

        for(String pair : pairs){
            String[] keyValue = pair.split("=");
            String value = new URLDecoder().decode(keyValue[1], "UTF-8");
            map.put(keyValue[0], value);
        }

        return map;
    }

    private boolean isSessionValid(Map<String, String> sessionIDs, HttpCookie cookie) {
        if(sessionIDs.containsKey(cookie.toString())) {
            return true;
        } else {
            return false;
        }
    }

    private HttpCookie getCookie(HttpExchange httpExchange, HttpCookie cookie) {
        UUID sessionID = UUID.randomUUID();
        String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie");

        if (cookieStr != null) {
            cookie = HttpCookie.parse(cookieStr).get(0);

        } else {
            cookie = new HttpCookie("sessionId", String.valueOf(sessionID));
            httpExchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
        }

        return cookie;
    }

    private void sendPersonalizedPage(HttpExchange httpExchange, User activeUser) throws IOException {
        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/template.twig");
        JtwigModel model = JtwigModel.newModel();
        model.with("userLogin", activeUser.getLogin());
        String response = template.render(model);
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private void sendLoginPage(HttpExchange httpExchange) throws IOException {
        URL url = Resources.getResource("static/index.html");
        StaticHandler.sendFile(httpExchange, url);
    }

    private void sendLoggingErrorPage(HttpExchange httpExchange) throws IOException {
        URL url = Resources.getResource("static/error.html");
        StaticHandler.sendFile(httpExchange, url);
    }
}
