package br.com.alura.ecommerce;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class HttpEcommerceService {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        var context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new NewOrderServlet()), "/new");
        context.addServlet(new ServletHolder(new GenerateAllReportsServlet()), "/admin/generate-reports");

        server.start();
        server.join();
    }
}
