package beprogrammer.camera_test;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/*
    카메라에서 보여주고있는 미리보기 이미지를 서피스뷰에서 보여주기 위한 클레스입니다.
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera camera = null;

    public CameraSurfaceView(Context context) {
        super(context);

        mHolder = getHolder();
        mHolder.addCallback(this);

    }

    //<summary>서피스뷰를 처음 실행했을때 생성해주는 함수입니다</summary>
    //<param name="holder">실제로 그림을 그리는등의 작업을하는 그래픽버퍼</param>
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();

        try {
            camera.setPreviewDisplay(mHolder);
        } catch (Exception e) {
            Log.e("CameraSurfaceView", "Failed to set camera preview.", e);
        }
    }

    //<summary>서피스뷰의 크기가 변경될때 호출되는 함수입니다.</summary>
    //<param name="holder">실제로 그림을 그리는등의 작업을하는 그래픽버퍼</param>
    //<param name="formatr">이미지의 포멧</param>
    //<param name="width">이미지의 widht</param>
    //<param name="height">이미지의 height</param>
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        camera.startPreview();
    }

    //<summary>서피스뷰가 종료되기직전에 사용합니다.</summary>
    //<param name="holder">실제로 그림을 그리는등의 작업을하는 그래픽버퍼</param>
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    //<summary>서피스뷰의 미리보기이미지를 캡쳐하기위한 함수입니다..</summary>
    //<param name="handler">카메라를 관리하는 핸들러(camera1.0)</param>
    public boolean capture(Camera.PictureCallback handler) {
        if (camera != null) {
            camera.takePicture(null, null, handler);
            return true;
        } else {
            return false;
        }
    }

}