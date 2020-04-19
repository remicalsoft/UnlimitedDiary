package net.dixq.unlimiteddiary.common;

import java.io.*;

public class StreamUtils {
    public static String getText(InputStream im){
        String body = "";
        Reader reader = null;
        try{
            reader = new InputStreamReader(im, "utf-8");
            BufferedReader br = new BufferedReader(reader);
            String str = br.readLine();
            while(str != null){
                body += str;
                str = br.readLine();
            }
            br.close();
        }catch(FileNotFoundException e){
            System.out.println(e);
        }catch(IOException e){
            System.out.println(e);
        }
        return body;
    }

    public static byte[] readAll(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte [] buffer = new byte[1024];
        while(true) {
            int len = inputStream.read(buffer);
            if(len < 0) {
                break;
            }
            bout.write(buffer, 0, len);
        }
        return bout.toByteArray();
    }

}
