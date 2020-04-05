package net.dixq.unlimiteddiary.singleton;

import com.google.api.services.drive.Drive;

public class DriveAccessor {
    private Drive _driveService;

    public void setService(Drive service){
        _driveService = service;
    }

    public Drive getService(){
        return _driveService;
    }

    private static final DriveAccessor _instance = new DriveAccessor();
    private DriveAccessor() {}
    public static DriveAccessor getInstance() { return _instance; }

}
