package com.example.goy;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

    private static int REQUESTCODE;
    private PermissionCallback permissionCallback;

    public static void requestPermissions(Context ctx, String[] permissions, PermissionCallback callback, Integer reqCode) {

        REQUESTCODE = reqCode != null ? reqCode : 4;

        if(hasPermissions(ctx, permissions)) return;
        ActivityCompat.requestPermissions((Activity) ctx, permissions, REQUESTCODE);
    }

    public static boolean hasPermissions(Context ctx, String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(ctx, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUESTCODE){
            boolean allGranted = true;
            for(int grantRes : grantResults){
                if(grantRes != PackageManager.PERMISSION_GRANTED){
                    allGranted = false;
                    break;
                }
            }
            if(permissionCallback != null){
                permissionCallback.onPermissionResult(allGranted);
            }
        }

    }

    public interface PermissionCallback{
        void onPermissionResult(boolean allGranted);
    }

}
