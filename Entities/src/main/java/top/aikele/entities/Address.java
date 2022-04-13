package top.aikele.entities;

public class Address {
    private String name;
    private String ip;
    private String port;
    private int time;

    public Address() {
    }

    public Address(String name, String ip, String port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public Address(String name, String ip, String port, int time) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.time = time;
    }

    @Override
    public String toString() {
        return "name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", port='" + port + '\''
                ;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
