package swedberg.applications.jrobot;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class GameLogger {
    private static PrintWriter writer;

    private static PrintWriter writer() {
        if (writer == null) {
            try {
                Files.createDirectories(Paths.get("build", "logs"));
                writer = new PrintWriter(Files.newBufferedWriter(
                    Paths.get("build", "logs", "jrobot.log"),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND));
                Runtime.getRuntime().addShutdownHook(new Thread(
                    () -> { if (writer != null) writer.close(); }));
            } catch (IOException e) {
                System.err.println("Kunde inte skapa logg: " + e.getMessage());
                writer = new PrintWriter(System.out, true);
            }
        }
        return writer;
    }

    public static void log(String msg) {
        writer().println(msg);
        writer().flush();
    }
}
