package ca.uqac.inf135.group3.tp2.model;

//import com.sun.javaws.exceptions.InvalidArgumentException;

public class HostPort {
    public static HostPort parse(String hostPort, int defaultPort) {
        String[] parts = hostPort.split(":");

        if (parts.length == 1) {
            //No port specified
            return new HostPort(parts[0], defaultPort);
        }
        else if (parts.length == 2) {
            //Port specified
            try {
                //Parse String port to integer port
                int port = Integer.parseInt(parts[1], 10);

                if (port <= 0 || port >= 65536) {
                    throw new Exception("Invalid port: " + parts[1]);
                }

                return new HostPort(parts[0], port);
            }
            catch (Exception e) {
                //Non integer or invalid port number
                return  null;
            }
        }
        else {
            //Invalid hostPort
            return null;
        }
    }
    public static HostPort parse(String hostPort) {
        //hostPort MUST contain the port number
        return parse(hostPort, -1/*Invalid port number, will return NULL if not specified*/);
    }

    private String hostname;
    private int port;

    public HostPort(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return String.format("%s:%d", hostname, port);
    }
}
