package net.dixq.unlimiteddiary.common.singleton;

import com.google.api.services.drive.Drive;

public class ApiAccessor {

    private Drive _driveService;

    private String _photosApiAccessToken;
    private String _folderId;

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

    private static final ApiAccessor _instance = new ApiAccessor();
    private ApiAccessor() {}
    public static ApiAccessor getInstance() { return _instance; }

}
