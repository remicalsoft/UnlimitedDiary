package net.dixq.unlimiteddiary.top;

import com.google.api.services.drive.model.File;
import net.dixq.unlimiteddiary.drive.DriveProcessor;

import java.io.*;

public class FileData {

    static DiaryData convertFileToDiaryData(File file) throws IOException {
        DiaryData dat = new DiaryData(false);
        try {
            String[] split = file.getName().split("\\.");
            dat.setYear(Integer.parseInt(split[0]));
            dat.setMonth(Integer.parseInt(split[1]));
            dat.setDay(Integer.parseInt(split[2]));
            dat.setHour(Integer.parseInt(split[3]));
            dat.setMin(Integer.parseInt(split[4]));
            dat.setCount(Integer.parseInt(split[5]));
            dat.setRevision(Integer.parseInt(split[6]));
        } catch(Exception e){
            return null;
        }
        return dat;
    }

    static DiaryData readBody(DriveProcessor processor, String fileId, DiaryData dat) throws IOException {
        String content = processor.getContent(fileId);
        int index = content.indexOf("</title>");
        if(index==-1){
            dat.setBody(content);
            return dat;
        }
        dat.setTitle(content.substring(7, index - 1));
        dat.setBody(content.substring(index + 8));
        return dat;
    }

}
