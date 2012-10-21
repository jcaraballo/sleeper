package server;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.management.MBeanContainer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;

public class Sleeper {
    private Server server;
    private static final MBeanContainer mBeanContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());

    public void start(int port) {
        server = new Server(port);
        new Context(server, "/").addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                String requestURI = req.getRequestURI();
                Integer sleepingTimeInMillis = calculateSleepingTime(requestURI);
                if (sleepingTimeInMillis == null) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("Wrong request " + requestURI + ", ask for /sleep/<sleeping time in ms>\n");
                } else {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    PrintWriter writer = resp.getWriter();
                    writer.write("Going for a " + sleepingTimeInMillis + "ms nap. Zzzzz...\n");
                    writer.flush();
                    try {
                        Thread.sleep(sleepingTimeInMillis);
                    } catch (InterruptedException e) {
                        resp.getWriter().write("Whose interrupting my sleep!??");
                        e.printStackTrace(resp.getWriter());
                    }
                }
            }

            private Integer calculateSleepingTime(String requestURI) {
                String[] parts = requestURI.split("/");
                if (parts.length != 3) return null;
                else
                    try {
                        return Integer.parseInt(parts[2]);
                    } catch (NumberFormatException e) {
                        return null;
                    }

            }
        }), "/sleep/*");

        server.getContainer().addEventListener(mBeanContainer);
        mBeanContainer.start();

        try {
            server.start();
            System.out.println("Sleeper started at http://localhost:" + port);
        } catch (Exception e) {
            throw new RuntimeException("Sleeper could not be started", e);
        }
    }

    public void stop() throws Exception {
        server.stop();
    }

    public static void main(String[] args) {
        new Sleeper().start(8081);
    }
}