package ClientSettings;

import ClientSettings.Request.Request;
import ClientSettings.Request.Utils;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;
import java.util.Scanner;

public class PIClient {
    public static final String PATH_TO_PROPERTIES = "config.properties";
    private Properties properties;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Socket socket;
    private Scanner scanner;
    private String PATH;
    private boolean alive=true;

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public PIClient(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            scanner = new Scanner(System.in);
            properties = new Properties();
            PATH = dataInputStream.readUTF();
//            setServerPATH(PATH);
            System.out.println(PATH);

            String data = "";
            while (alive) {
                data = scanner.nextLine();
                if (!data.equals("")) read(data);
//                else {
//                    System.out.println(getServerPATH());
//                }
            }
        } catch (SocketException e){
            System.out.println("No open server. Try again later.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(String data) throws IOException {
        Request commands = Utils.getRequest(data);

        switch (commands) {
            case ELSE:
                dataOutputStream.writeUTF(data);
                dataOutputStream.flush();
                System.out.println(dataInputStream.readUTF());
                break;
            case STOP: {
                dataOutputStream.writeUTF(data);
                dataOutputStream.flush();
                dataOutputStream.close();
                dataInputStream.close();
                socket.close();
                setAlive(false);
                break;
            }
            case UPLOAD: {
                File file = new File(data.substring(7));
                dataOutputStream.writeUTF(data.substring(0,7)+file.getName());
                System.out.println(file.getPath());
                FileInputStream fileInputStream = new FileInputStream(file);

                byte [] bytes = new byte[(int)file.length()];
                dataOutputStream.writeInt(bytes.length);

                fileInputStream.read(bytes,0,bytes.length);

                dataOutputStream.write(bytes,0,bytes.length);
                dataOutputStream.flush();
                System.out.println(dataInputStream.readUTF());
                break;
            }
            case DOWNLOAD: {
                dataOutputStream.writeUTF(data);
                dataOutputStream.flush();

                int size = dataInputStream.readInt();
                byte[] bytes = new byte[size];
                dataInputStream.read(bytes, 0, bytes.length);

                String fileName = "";
                if (data.substring(4).startsWith("D:")) {
                    File fromData = new File(data.substring(4));
                    fileName = fromData.getName();
                } else {
                    fileName = data.substring(4);
                }
                FileInputStream fileInputStream = new FileInputStream(PATH_TO_PROPERTIES);
                properties.load(fileInputStream);
                File file = new File(properties.getProperty("directory") + fileName);
//                if (file.exists()) {
                file.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                fileOutputStream.write(bytes, 0, bytes.length);
                fileOutputStream.close();
                System.out.println(dataInputStream.readUTF());
                break;
            }
        }
    }
}
