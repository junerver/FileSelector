package xyz.junerver.fileselectordemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import xyz.junerver.fileselector.FileModel;
import xyz.junerver.fileselector.FileSelector;
import xyz.junerver.fileselector.OnResultListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.text);
    }


    public void select(View view) {
        selectKotlin();
    }


    private void selectKotlin() {

        FileSelector.Companion
                .getInstance(this)
                .setSelectPath("/storage/emulated/0", "/storage/emulated/0/Android/data/")
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
//                .startScanWorker()
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
                        mTextView.setText("");
                        for (FileModel fileModel : result) {
                            Log.d("bbb~~~~~~~~~~~~~~~~", fileModel.getPath());
                            Log.d("bbb~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~");
                            mTextView.append(fileModel.getPath()+"\n\n");
                        }
                    }


                    @Override
                    public void onCancel() {
                        Log.d("bbb~~~~~~~~~~~~~~~~", "onCancel");
                    }
                }).start();
    }
}