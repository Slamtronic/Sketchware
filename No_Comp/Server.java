package ser;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
//import java.nio.file.Files;
//import java.nio.file.Path;
import java.io.*;

public class Server {

        public static void main(String[] args) throws IOException {
        ServerSocket srv =null;
        Socket soc=null;

        try {
                srv =new ServerSocket(6666);
                System.out.println("listening.........");
        } catch (IOException e) {
                // TODO Auto-generated catch block
                System.out.println("please check port num");
        }


        try {
                 soc=srv.accept();
        } catch (IOException e) {
                // TODO Auto-generated catch block
                System.out.println("cant accept connection");
        }

        InputStream in=null;
        OutputStream out=null;




try {
                in=soc.getInputStream();
        } catch (IOException e) {
                // TODO Auto-generated catch block
                System.out.println("cant get inputstream or soc error");
        }

        try {    File dir = new File( "/storage/emulated/0/" , "serveo");
                  if (!dir.exists()) dir.mkdirs();
                out=new FileOutputStream(dir+"/"+"server4.txt");
                System.out.println("file received");
        } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                System.out.println("file not found");
        }

        byte[] b=new byte[20480];
        int i;
        while((i = in.read(b))>0) {

                out.write(b,0,i);}
                out.close();
                in.close();
                soc.close();
                srv.close();



}}