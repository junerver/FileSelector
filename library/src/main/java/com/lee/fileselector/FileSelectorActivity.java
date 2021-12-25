package com.lee.fileselector;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lee.utils.ToastUtils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Lee
 */
public class FileSelectorActivity extends AppCompatActivity {
    private static final String TAG="FileSelector";

    private FastScrollRecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView empty;

    private  List<FileModel> fileModels = new ArrayList<>();
    private final ArrayList<FileModel> mSelectedFileList = new ArrayList<>();
    private MenuItem mCountMenuItem;
    private int mSelectSortTypeIndex;
    private FileAdapter fileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_selector);
        recyclerView = findViewById(R.id.recycleView);
        progressBar = findViewById(R.id.progressBar);
        Toolbar mToolBar = findViewById(R.id.toolbar);
        empty = findViewById(R.id.empty);
        getWindow().setStatusBarColor(FileSelector.getInstance(this).barColor);
        mToolBar.setBackgroundColor(FileSelector.getInstance(this).barColor);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("文件选择");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        PermissionsUtils.getInstance().checkPermissions(this, permissions, new PermissionsUtils.PermissionsResult() {
            @Override
            public void passPermission() {
                progressBar.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getFiles();
                    }
                }).start();
            }

            @Override
            public void continuePermission() {
                Toast.makeText(FileSelectorActivity.this, "读写权限被拒绝", Toast.LENGTH_LONG).show();
            }

            @Override
            public void refusePermission() {
                Toast.makeText(FileSelectorActivity.this, "读写权限被拒绝", Toast.LENGTH_LONG).show();
            }
        });
    }

    private synchronized void getFiles() {
        fileModels.clear();
        if (mCountMenuItem != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCountMenuItem.setTitle(String.format(getString(R.string.selected_file_count), String.valueOf(mSelectedFileList.size()), String.valueOf(FileSelector.getInstance(FileSelectorActivity.this).maxCount)));
                }
            });
        }
        FileSelector.getInstance(this).setThreadStop(false);
        long start = System.currentTimeMillis();
        fileModels =  FileSelector.getInstance(this).getFiles();

        sortFileList(FileSelector.getInstance(this).mSortType);
        if (fileModels.size() <= 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    empty.setVisibility(View.VISIBLE);
                    initAdapter();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initAdapter();
                }
            });
        }
        long end = System.currentTimeMillis();
        Log.d(TAG, "全流程耗时: "+(end-start) + "文件总量："+fileModels.size());
    }

    private void sortFileList(int mSortType) {
        mSelectedFileList.clear();
        long start = System.currentTimeMillis();
        try {
            if (mSortType == FileUtils.BY_NAME_ASC) {
                Collections.sort(fileModels, new FileUtils.SortByName());
            } else if (mSortType == FileUtils.BY_NAME_DESC) {
                Collections.sort(fileModels, new FileUtils.SortByName());
                Collections.reverse(fileModels);
            } else if (mSortType == FileUtils.BY_TIME_ASC) {
                Collections.sort(fileModels, new FileUtils.SortByTime());
            } else if (mSortType == FileUtils.BY_TIME_DESC) {
                Collections.sort(fileModels, new FileUtils.SortByTime());
                Collections.reverse(fileModels);
            } else if (mSortType == FileUtils.BY_SIZE_ASC) {
                Collections.sort(fileModels, new FileUtils.SortBySize());
            } else if (mSortType == FileUtils.BY_SIZE_DESC) {
                Collections.sort(fileModels, new FileUtils.SortBySize());
                Collections.reverse(fileModels);
            } else if (mSortType == FileUtils.BY_EXTENSION_ASC) {
                Collections.sort(fileModels, new FileUtils.SortByExtension());
            } else if (mSortType == FileUtils.BY_EXTENSION_DESC) {
                Collections.sort(fileModels, new FileUtils.SortByExtension());
                Collections.reverse(fileModels);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initAdapter() {
        fileAdapter = new FileAdapter(this, R.layout.item_file_selector, fileModels);
        fileAdapter.setSelectedFileList(mSelectedFileList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(fileAdapter);
        if (mCountMenuItem != null) {
            fileAdapter.setCountMenuItem(mCountMenuItem);
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.selector_menu, menu);
        mCountMenuItem = menu.findItem(R.id.select_count);
        if (fileAdapter != null) {
            fileAdapter.setCountMenuItem(mCountMenuItem);
        }
        mCountMenuItem.setTitle(String.format(getString(R.string.selected_file_count), String.valueOf(mSelectedFileList.size()), String.valueOf(FileSelector.getInstance(FileSelectorActivity.this).maxCount)));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (fileAdapter == null) {
            ToastUtils.showShort("请等待文件加载完毕");
            return true;
        }
        int i = item.getItemId();
        if (i == R.id.select_count) {
            //选中
            if (mSelectedFileList.isEmpty()) {
                Toast.makeText(this, "未选择任何文件", Toast.LENGTH_SHORT).show();
                return true;
            }
            //不为空
            if (FileSelector.getInstance(FileSelectorActivity.this).listener != null) {
                FileSelector.getInstance(FileSelectorActivity.this).listener.onResult(mSelectedFileList);
            } else {
                Intent result = new Intent();
                result.putParcelableArrayListExtra(FileUtils.RESULT_KEY, mSelectedFileList);
                setResult(RESULT_OK, result);
            }
            finish();
        } else if (i == R.id.browser_sort) {
            mSelectSortTypeIndex = 0;
            new AlertDialog
                    .Builder(this)
                    .setSingleChoiceItems(R.array.sort_list, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSelectSortTypeIndex = which;
                        }
                    })
                    .setNegativeButton("降序", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setVisibility(View.VISIBLE);
                                            recyclerView.setVisibility(View.GONE);
                                        }
                                    });
                                    switch (mSelectSortTypeIndex) {
                                        case 0:
                                            sortFileList(FileUtils.BY_NAME_DESC);
                                            break;
                                        case 1:
                                            sortFileList(FileUtils.BY_TIME_DESC);
                                            break;
                                        case 2:
                                            sortFileList(FileUtils.BY_SIZE_DESC);
                                            break;
                                        case 3:
                                            sortFileList(FileUtils.BY_EXTENSION_DESC);
                                            break;
                                        default:
                                            break;
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (fileAdapter != null) {
                                                fileAdapter.notifyDataSetChanged();
                                                progressBar.setVisibility(View.GONE);
                                                recyclerView.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                                }
                            }).start();
                        }
                    })
                    .setPositiveButton("升序", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setVisibility(View.VISIBLE);
                                            recyclerView.setVisibility(View.GONE);
                                        }
                                    });
                                    switch (mSelectSortTypeIndex) {
                                        case 0:
                                            sortFileList(FileUtils.BY_NAME_ASC);
                                            break;
                                        case 1:
                                            sortFileList(FileUtils.BY_TIME_ASC);
                                            break;
                                        case 2:
                                            sortFileList(FileUtils.BY_SIZE_ASC);
                                            break;
                                        case 3:
                                            sortFileList(FileUtils.BY_EXTENSION_ASC);
                                            break;
                                        default:
                                            break;
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (fileAdapter != null) {
                                                fileAdapter.setData(fileModels);
                                                fileAdapter.notifyDataSetChanged();
                                                progressBar.setVisibility(View.GONE);
                                                recyclerView.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                                }
                            }).start();
                        }
                    })
                    .setTitle("请选择")
                    .show();

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FileSelector.getInstance(this).setThreadStop(true);
        if (FileSelector.getInstance(this).listener != null) {
            FileSelector.getInstance(this).listener.onCancel();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileSelector.getInstance(this).mFileModels.clear();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
