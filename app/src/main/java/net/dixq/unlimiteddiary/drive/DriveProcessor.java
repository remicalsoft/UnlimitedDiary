package net.dixq.unlimiteddiary.drive;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import net.dixq.unlimiteddiary.common.Define;
import net.dixq.unlimiteddiary.exception.FatalErrorException;
import net.dixq.unlimiteddiary.singleton.DriveAccessor;
import net.dixq.unlimiteddiary.utils.Lg;
import net.dixq.unlimiteddiary.utils.StreamUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DriveProcessor {

    private DriveAccessor _accessor;

    public DriveProcessor(DriveAccessor accessor){
        _accessor = accessor;
    }

    public String getFolderId() throws IOException {
        ArrayList<File> list = new ArrayList<>();
        Drive.Files.List request = DriveAccessor.getInstance().getService().files().list();
        request.setQ("mimeType = 'application/vnd.google-apps.folder' and name = '"+ Define.FOLDER_NAME+"'");
        do {
            FileList files = request.execute();
            list.addAll(files.getFiles());
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() > 0);
        if (list.size() != 1) {
            throw new FatalErrorException("UnlimitedDiaryのフォルダが見つからないか、2つ以上あります");
        }
        return list.get(0).getId();
    }

    public LinkedList<File> getAllFile() throws IOException {
        String folderId = getFolderId();
        return getAllFile(folderId);
    }

    public LinkedList<File> getAllFile(String folderId) throws IOException {
        LinkedList<File> list = new LinkedList<>();
        Drive.Files.List request = DriveAccessor.getInstance().getService().files().list()
            //対象のフォルダ以下　かつ　フォルダは除外　かつ　ゴミ箱行きは除外
            .setQ("'" + folderId + "' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false");
        do {
            FileList files = request.execute();
            list.addAll(files.getFiles());
            // 全アイテムを取得するために繰り返し
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null
                && request.getPageToken().length() > 0);
        return list;
    }

    public String getContent(String fileId) throws IOException {
        InputStream im = DriveAccessor.getInstance().getService().files().get(fileId).executeMediaAsInputStream();
        return StreamUtils.getText(im);
    }


}
