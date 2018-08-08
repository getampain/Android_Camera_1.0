package beprogrammer.camera_test;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 *
 *  camera api를 이용하여 현재 카메라가 비추고있는 이미지를 Surface뷰를 통하여 미리보기로
 *  만들어주며 버튼을 누르면 이미지를 bitmap.compress와 Media.insertImage 2가지 방법으로 저장
 *  해주는 어플리케이션입니다.
 *
 */
public class MainActivity extends AppCompatActivity {

    //Media.insertImage 방법으로 이미지를 저장해줄때 사용하는 이름변수 입니다.
    private static String IMAGE_FILE = "capture.jpg";
    //bitmap.compress 형태로 이미지를 저장해둘떄 경로를 위한 변수 입니다.
    private static String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CameraSurfaceView cameraView = new CameraSurfaceView(getApplicationContext());
        FrameLayout previewFrame = (FrameLayout) findViewById(R.id.previewFrame);
        previewFrame.addView(cameraView);

        Button saveBtn = (Button) findViewById(R.id.saveBtn);

        //사진찍기 버튼의 클릭 리스너입니다.
        saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"이미지를 저장중입니다.",
                        Toast.LENGTH_LONG).show();

                cameraView.capture(new Camera.PictureCallback() {
                    public void onPictureTaken(byte[] data, Camera camera) {
                        try {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            String outUriStr = MediaStore.Images.Media.insertImage(getContentResolver(),
                                    bitmap, "Captured Image", "Captured Image using Camera.");

                            //비트맵 이미지를 jpg로 가공하고 자기가 원하는 경로를 지정하여 저장해주기
                            //위한 부분입니다.
                            File testFile = createImageFile();
                            FileOutputStream out = new FileOutputStream(testFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.close();
                            Log.d("Path_Cheacker","file://"+currentPhotoPath);
                            //이미지를 저장한뒤 이미지를 미디어스케너로 확인 해줍니다 스캔을하지않으면
                            //파일이 오랫동안안나타날수있습니다.
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                    Uri.parse("file://"+currentPhotoPath)));

                            //비트맵이미지를 jpg파일로 설정하여 기본 Picture 디렉토리로 저장해줍니다.
                            if (outUriStr == null) {
                                Log.d("SampleCapture", "Image insert failed.");
                                return;
                            } else {
                                Uri outUri = Uri.parse(outUriStr);
                                //이미지를 저장한뒤 이미지를 미디어스케너로 확인 해줍니다 스캔을하지않으면
                                //파일이 오랫동안안나타날수있습니
                                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, outUri));
                            }

                            Toast.makeText(getApplicationContext(),"이미지가 저장되었습니다.",
                                    Toast.LENGTH_LONG).show();
                            //서페스뷰의 미리보기를 다시시작해줍니다.
                            camera.startPreview();

                        } catch (Exception e) {
                            Log.e("SampleCapture", "Failed to insert image.", e);
                        }
                    }
                });
            }
        });

        checkDangerousPermissions();
    }


    //<summary>
    //유저에게 보안상의 이슈가 될 수 있는 권한에대한 허가요청을 위한 함수 입니다.
    //</summary>
    private void checkDangerousPermissions() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "권한 있음", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                Toast.makeText(this, "권한 설명 필요함", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }


    //<summary>
    //android.support.v4.app.ActivityCompat의 함수로 권한요청의
    //결과를 받아주는 함수 입니다.
    //</summary>
    //<param name="permssion">
    //필요한 퍼미션의 갯수와 종류와 갯수
   //</param>
    //<param name="grantResults">
    //유저가 퍼미션을 승인했는지 하지않았는지 파악해주기위한변수  0일경우 승인
    //</param>
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, permissions[i] + "권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //<summary>
    //현재 시간에 따라 JPEG형태의 이미지 파일을 저장소/Pictures/test 경로의 디랙토리안에
    //생성해주고 파일의 경로를 String currentPhotoPath 에 저장해주는 함수 입니다 또한
    //Pictures/test 디렉토리가 없다면 생성해줍니다.
    //</summary>
    //<returns> 새로 생성된 JPEG형태의 이미지파일 </returns>
    public File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_"+timeStamp+".jpg";
        File imageFile = null;
        File storageDir = new File(Environment.getExternalStorageDirectory()+ "/Pictures",
                "test");

        //경로상의 디렉토리가 없을시 생성해줍니다.
        if(!storageDir.exists()){
            Log.i("currentPhotoPaht1",storageDir.toString());
            storageDir.mkdirs();
        }

        imageFile = new File(storageDir,imageFileName);
        currentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;

    }





}

