package net.dixq.unlimiteddiary.singleton;

import com.google.api.services.drive.Drive;

public class ApiAccessor {
    private Drive _driveService;

    public void setDriveService(Drive service){
        _driveService = service;
    }

    public Drive getDriveService(){
        return _driveService;
    }

    private static final ApiAccessor _instance = new ApiAccessor();
    private ApiAccessor() {}
    public static ApiAccessor getInstance() { return _instance; }

}
