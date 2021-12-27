package xyz.junerver.fileselectordemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import xyz.junerver.fileselector.FileModel;
import xyz.junerver.fileselector.FileSelector;
import xyz.junerver.fileselector.FileUtils;
import xyz.junerver.fileselector.OnResultListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (data != null) {
                ArrayList<FileModel> list = data.getParcelableArrayListExtra(FileUtils.RESULT_KEY);
                for (FileModel fileModel : list) {
                    Log.d("aaa", fileModel.getPath());
                    Log.d("aaa", "~~~~~~~~~~~~~~~~");
                }
            }
        }
    }

    public void select(View view) {
        selectKotlin();
    }

//    private void selectJava() {
//        FileSelector
//                .getInstance(this)
//                .setSelectPath("/storage/emulated/0/DCIM", "/storage/emulated/0/Android/data/")
////                .isShowHiddenFile(true)
//                .setBarColorRes(R.color.colorAccent)
//                .setIgnorePath("Android", "Tencent")
//                .setFileType(
//                        "jpg", "gif", "png", "bmp", "jpeg", "webp", "wmv", "flv", "mp4", "avi", "mpg", "mpeg", "rmvb", "rm", "asf",
//                        "f4v", "vob", "mkv", "3gp", "mov", "mid", "wav", "wma", "mp3", "ogg", "amr", "m4a", "3gpp", "aac", "swf",
//                        "wps", "doc", "docx", "txt", "xlsx", "xls", "pdf", "ppt", "pptx", "zip", "rar", "7z", "exe", "gsp", "bbx",
//                        "btx", "dat", "dws", "other", "chm", "unity3d", "xmind", "gf", "dsek")
////                .setSortType(FileUtils.BY_NAME_ASC)
////                .setMaxCount(1)
////                .requestCode(1)
//                .forResult(new OnResultListener<FileModel>() {
//                    @Override
//                    public void onResult(List<FileModel> result) {
//                        for (FileModel fileModel : result) {
//                            Log.d("bbb", fileModel.getPath());
//                            Log.d("bbb", "~~~~~~~~~~~~~~~~");
//                        }
//                    }
//
//                    @Override
//                    public void onCancel() {
//                        Log.d("bbb", "onCancel");
//                    }
//                }).start();
//    }

    private void selectKotlin() {
        FileSelector.Companion
                .getInstance(this)
                .setSelectPath("/storage/emulated/0/DCIM", "/storage/emulated/0/Android/data/")
//                .isShowHiddenFile(true)
                .setBarColorRes(R.color.colorAccent)
//                .setIgnorePath("Android", "Tencent")
                .setFileType(
                        "jpg", "gif", "png", "bmp", "jpeg", "webp", "wmv", "flv", "mp4", "avi", "mpg", "mpeg", "rmvb", "rm", "asf",
                        "f4v", "vob", "mkv", "3gp", "mov", "mid", "wav", "wma", "mp3", "ogg", "amr", "m4a", "3gpp", "aac", "swf",
                        "wps", "doc", "docx", "txt", "xlsx", "xls", "pdf", "ppt", "pptx", "zip", "rar", "7z", "exe", "gsp", "bbx",
                        "btx", "dat", "dws", "other", "chm", "unity3d", "xmind", "gf", "dsek")
                .setSortType(FileSelector.BY_NAME_ASC)
//                .setMaxCount(1)
//                .requestCode(1)
//                .getWorker()
//                .setCallBack(new FilesScanWorker.FilesScanCallBack() {
//                    @Override
//                    public void onNext(List<FileModel> fileModels) {
//                        Log.d(TAG, "扫描到：" + fileModels.size() + "个文件"+Thread.currentThread().getName());
//                    }
//
//                    @Override
//                    public void onCompleted(List<FileModel> fileModels) {
//                        Log.d(TAG, "扫描完成，总数：" + fileModels.size() + "个文件" );
//                    }
//                })
//                .work();
                .startUIWorker()
                .forResult(new OnResultListener<FileModel>() {
                    @Override
                    public void onResult(List<FileModel> result) {
                        for (FileModel fileModel : result) {
                            Log.d("bbb", fileModel.getPath());
                            Log.d("bbb", "~~~~~~~~~~~~~~~~");
                        }
                    }


                    @Override
                    public void onCancel() {
                        Log.d("bbb", "onCancel");
                    }
                }).start();
    }
}