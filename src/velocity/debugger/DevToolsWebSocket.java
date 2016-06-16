package velocity.debugger;

import java.io.IOException;
import java.text.MessageFormat;
import javax.servlet.ServletContext;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

public class DevToolsWebSocket implements WebSocketListener {

    public static final String WEB_SOCKET_ATTR_NAME = "org.javafx.devtools.DevToolsWebSocket";
    private Session session;
    private final ServletContext context;

    public DevToolsWebSocket() {
        this.context = DevToolsDebuggerServer.getServletContext();
    }

    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        if (context.getAttribute(WEB_SOCKET_ATTR_NAME) != null) {
            session.close();
            System.out.println("Another client is already connected. Connection refused");
        } else {
            context.setAttribute(WEB_SOCKET_ATTR_NAME, this);
            System.out.println("Client connected");
        }
    }

    @Override
    public void onWebSocketClose(int closeCode, String message) {
        DevToolsWebSocket mainSocket = (DevToolsWebSocket) context.getAttribute(WEB_SOCKET_ATTR_NAME);
        if (mainSocket == this) {
            context.removeAttribute(WEB_SOCKET_ATTR_NAME);
            System.out.println("Client disconnected");
        }
    }

    public void sendMessage(String data) throws IOException {
        RemoteEndpoint remote = session.getRemote();
        remote.sendString(data);
    }

    @Override
    public void onWebSocketText(String data) {
        DevToolsDebuggerServer.sendMessageToBrowser(data);
    }

    @Override
    public void onWebSocketError(Throwable t) {
        String errorMessage = t.getMessage();
        System.out.println(MessageFormat.format("WebSocket error occurred: {0}", errorMessage));
    }

    @Override
    public void onWebSocketBinary(byte[] arg0, int arg1, int arg2) {
    }

}
