package com.rmzn.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;

import com.rmzn.app.databinding.ActivityMainBinding;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity {


    ActivityMainBinding binding;
    CascadeClassifier cascadeClassifier;
    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(binding.cameraView);
    }

    int SELECT_CODE=100,CAMERA_CODE=101;
    Bitmap bitmap;
    Mat mat;
    Mat gray,rgb;
    MatOfRect rects;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        getPermission();

        binding.cameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
            rgb=new Mat();
            gray =new Mat();
            rects=new MatOfRect();
            }

            @Override
            public void onCameraViewStopped() {
                rgb.release();
                gray.release();
                rects.release();
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                // todo process input frame and returned processed one
            rgb=inputFrame.rgba();
            gray =inputFrame.gray();

            cascadeClassifier.detectMultiScale(gray,rects,1.1,2);

            for(Rect rect:rects.toList()){
                Mat submat=rgb.submat(rect);
                Imgproc.blur(submat,submat,new Size(100,100));
                Imgproc.rectangle(rgb,rect,new Scalar(0,255,0),10);

                submat.release();
            }

                return rgb;
            }
        });

        if(OpenCVLoader.initDebug()){
            Log.d("LOADED","basarılı");
            binding.cameraView.enableView();


            try {
                InputStream inputStream=getResources().openRawResource(R.raw.lbpcascade_frontalface);
                File file=new File(getDir("cascade",MODE_PRIVATE),"lbpcascade_frontalface.xml");
                FileOutputStream fileOutputStream= new FileOutputStream(file);

                byte[] data=new byte[4096];
                int read_bytes;

                while ((read_bytes = inputStream.read(data))!=-1){
                    fileOutputStream.write(data,0,read_bytes);

                }

                cascadeClassifier=new CascadeClassifier(file.getAbsolutePath());

                if(cascadeClassifier.empty())cascadeClassifier=null;

                inputStream.close();
                fileOutputStream.close();
                file.delete();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else Log.d("LOADED","ERR");

      /*  binding.select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,SELECT_CODE);
            }
        });

        binding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,CAMERA_CODE);
            }
        });*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.cameraView.enableView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.cameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.cameraView.disableView();
    }


    private void getPermission() {
        if(checkCallingOrSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA},102);

        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==102 && grantResults.length>0){
           if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
               getPermission();
           }
        }

    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==SELECT_CODE && data!=null ) {
            try {
                bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(),data.getData());
                binding.imageView.setImageBitmap(bitmap);
                mat=new Mat();
                Utils.bitmapToMat(bitmap,mat);

                Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGB2GRAY);

                Utils.matToBitmap(mat,bitmap);
                binding.imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode==CAMERA_CODE && data!=null){
            bitmap=(Bitmap) data.getExtras().get("data");
            binding.imageView.setImageBitmap(bitmap);
            mat=new Mat();
            Utils.bitmapToMat(bitmap,mat);
            Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGB2GRAY);

            Utils.matToBitmap(mat,bitmap);
            binding.imageView.setImageBitmap(bitmap);
        }
    }*/
}