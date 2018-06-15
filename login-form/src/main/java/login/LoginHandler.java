package login;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import login.Data.PostgresUserDAO;
import login.Data.UserDAO;
import login.Model.User;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.*;
import java.net.HttpCookie;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public class LoginHandler implements HttpHandler {

    private Map<String, Integer> sessionsUsers = new HashMap<>();
    private int sessionCounter = 0;
    private UserDAO userDAO = new PostgresUserDAO();

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        final String GET_METHOD = "GET";
        final String POST_METHOD = "POST";
        String method = httpExchange.getRequestMethod();
        HttpCookie cookie;

        if (method.equals(GET_METHOD)) { ;
            String sessionCookie = httpExchange.getRequestHeaders().getFirst("Cookie");
            System.out.println("Start get method");

            if (sessionCookie != null) {
                System.out.println("Cookie found");
                cookie = HttpCookie.parse(sessionCookie).get(0);

                if (sessionsUsers.containsKey(cookie.getValue())) {
                    System.out.println("Cookie in map");
                    sendPersonalizedPage(httpExchange, userDAO.getById(sessionsUsers.get(sessionCookie)), cookie.getValue());
                    return;
                }
            }

            sendLoginPage(httpExchange);
        }

        if (method.equals(POST_METHOD)) {
            String formData = getFormData(httpExchange);
            System.out.println("Logging in!");

            if (isUserLoggingIn(formData)) {
                handleLoggingIn(httpExchange, formData);
            }

        }
        System.out.println("Done!");
    }

    private void handleLoggingIn(HttpExchange httpExchange, String formData) throws IOException {

        HttpCookie cookie;
        User userData = parseLoginData(formData);
        System.out.println("Login:");
        System.out.println(userData.getLogin());
        System.out.println("ID:");
        System.out.println(userData.getUserId());

        if (userDAO.getUserData(userData.getPassword(), userData.getLogin()) != null) {

            sessionCounter++;

            UUID uuid = UUID.randomUUID();

            User user = userDAO.getUserData(userData.getPassword(), userData.getLogin());

            cookie = new HttpCookie("sessionId", uuid.toString());

            httpExchange.getResponseHeaders().add("Set-Cookie", cookie.toString());

            sessionsUsers.put(uuid.toString(), user.getUserId());


            System.out.println(sessionsUsers.toString());
            sendPersonalizedPage(httpExchange, user, uuid.toString());

        } else {
            sendLoggingErrorPage(httpExchange);
        }
    }

    private String getFormData(HttpExchange httpExchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), Charsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        return br.readLine();
    }

    private void sendPersonalizedPage(HttpExchange httpExchange, User user, String sessionId) throws IOException {
        Integer userId = sessionsUsers.get(sessionId);
        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/template.twig");
        JtwigModel model = JtwigModel.newModel();
        model.with("userLogin", user.getLogin());
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

    private boolean isUserLoggingIn(String form) {
        final String SUBMIT_LOGIN = "submit-login";
        return form.contains(SUBMIT_LOGIN);
    }

    private boolean isUserLoggingOut(String form) {
        final String SUBMIT_LOGOUT = "submit-logout";
        return form.contains(SUBMIT_LOGOUT);
    }

    private void handleLoggingOut(HttpExchange httpExchange) throws IOException {
        HttpCookie cookie;
        String sessionCookie = httpExchange.getRequestHeaders().getFirst("Cookie");

        if (sessionCookie != null) {
            cookie = HttpCookie.parse(sessionCookie).get(0);
            sessionsUsers.remove(cookie.getValue());
        }
        sendLoginPage(httpExchange);
    }

    private User parseLoginData(String formData) throws UnsupportedEncodingException {

        User user = new User();

        final int LOGIN_INDEX = 0;
        final int PASSWORD_INDEX = 1;
        final int VALUE_INDEX = 1;

        String[] pairs = formData.split("&");
        List<String> values = new ArrayList<>();

        for(String pair : pairs){
            String[] keyValue = pair.split("=");
            values.add(URLDecoder.decode(keyValue[VALUE_INDEX], Charsets.UTF_8.displayName()));
        }

        user.setLogin(values.get(LOGIN_INDEX));
        user.setPassword(hashPassword(values.get(PASSWORD_INDEX)));

        return user;
    }

    private Integer hashPassword(String password) {
        return password.hashCode();
    }
}
