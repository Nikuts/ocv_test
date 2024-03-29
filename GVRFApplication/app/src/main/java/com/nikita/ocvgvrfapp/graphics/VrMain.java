package com.nikita.ocvgvrfapp.graphics;

import android.graphics.Color;
import android.util.Log;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.opencv.core.Point;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Created by n.kutsachenk on 4/4/2017.
 */

public class VrMain extends GVRMain {
    private static final String TAG = VrMain.class.getSimpleName();
    private static final float ROTATION_SCALE = 1.0f;

    private GVRContext mGVRContext;
    private GVRSceneObject mSceneObject;

    private Point mRotationVector;

    @Override
    public void onInit(GVRContext gvrContext) {
        // save context for possible use in onStep(), even though that's empty
        // in this sample
        mRotationVector = new Point(0,0);
        mGVRContext = gvrContext;

        GVRScene scene = mGVRContext.getMainScene();
//        GVRAssetLoader assetLoader = new GVRAssetLoader(mGVRContext);
        // set background color
        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getLeftCamera()
                .setBackgroundColor(Color.WHITE);
        mainCameraRig.getRightCamera()
                .setBackgroundColor(Color.WHITE);
        // load texture
        Future<GVRTexture> futureTexture = null;
        try {
            futureTexture = mGVRContext.loadFutureTexture(new GVRAndroidResource(mGVRContext, "textures/box.jpg"));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        GVRMaterial material = new GVRMaterial(mGVRContext);
        material.setMainTexture(futureTexture);

        mSceneObject = new GVRCubeSceneObject(mGVRContext, true, futureTexture);
        // set the scene object position
        mSceneObject.getTransform().setPosition(0.0f, 0.0f, 3.0f);
        // add the scene object to the scene graph
        scene.addSceneObject(mSceneObject);

    }

    @Override
    public void onStep() {
        float length = (float) Math.sqrt(Math.pow(mRotationVector.x, 2) + Math.pow(mRotationVector.y, 2));
        //swap x and y
        if (length != 0) mSceneObject.getTransform().rotateByAxis(length * ROTATION_SCALE, (float)mRotationVector.y/length, (float) mRotationVector.x/length, 0);
        mRotationVector.x = 0;
        mRotationVector.y = 0;
    }

    public Point getRotationVector() {
        return mRotationVector;
    }

    public void setRotationVector(Point rotationVector) {
        mRotationVector = rotationVector;
    }
}
