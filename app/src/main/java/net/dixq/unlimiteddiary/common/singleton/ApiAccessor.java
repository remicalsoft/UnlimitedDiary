package net.dixq.unlimiteddiary.common.singleton;

import com.google.api.services.drive.Drive;

public class ApiAccessor {

    private Drive _driveService;

    private String _photosApiAccessToken;
    private String _folderId;
    private String _jpegFolderId;

    public void setDriveService(Drive service){
        _driveService = service;
    }
    public Drive getDriveService(){
        return _driveService;
    }

    public void setPhotosApiAccessToken(String token){
        _photosApiAccessToken = token;
    }
    public String getPhotosApiAccessToken(){
        return _photosApiAccessToken;
    }

    public void setFolderId(String folderId){
        _folderId = folderId;
    }
    public String getFolderId(){
        return _folderId;
    }

    public void setJpegFolderId(String folderId){
        _jpegFolderId = folderId;
    }
    public String getJpegFolderId(){
        return _jpegFolderId;
    }

    private static final ApiAccessor _instance = new ApiAccessor();
    private ApiAccessor() {}
    public static ApiAccessor getInstance() { return _instance; }

}
