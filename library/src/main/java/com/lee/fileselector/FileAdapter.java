package com.lee.fileselector;

import android.app.Activity;
import android.content.Context;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.lee.adapter.recyclerview.CommonAdapter;
import com.lee.adapter.recyclerview.base.ViewHolder;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lee
 */
public class FileAdapter extends CommonAdapter<FileModel> implements FastScrollRecyclerView.SectionedAdapter {

    private MenuItem mCountMenuItem;

    private ArrayList<FileModel> mSelectedFileList;

    private List<FileModel> modelList;

    public void setCountMenuItem(MenuItem mCountMenuItem) {
        this.mCountMenuItem = mCountMenuItem;
    }

    public void setSelectedFileList(ArrayList<FileModel> mSelectedFileList) {
        this.mSelectedFileList = mSelectedFileList;
    }

    public FileAdapter(Context context, int layoutId, List<FileModel> datas) {
        super(context, layoutId, datas);
        modelList = datas;
    }

    @Override
    public void convert(ViewHolder holder, FileModel fileModel, int position) {
        ImageView imageView = holder.getView(R.id.iv_type);
        String extension = fileModel.getExtension();
        if (extension.equalsIgnoreCase("gif")) {
            Glide.with(mContext).asGif().load(fileModel.getPath()).into(imageView);
        } else if (extension.equalsIgnoreCase("jpg") ||
                extension.equalsIgnoreCase("png") ||
                extension.equalsIgnoreCase("bmp") ||
                extension.equalsIgnoreCase("jpeg") ||
                extension.equalsIgnoreCase("webp") ||
                extension.equalsIgnoreCase("wmv") ||
                extension.equalsIgnoreCase("flv") ||
                extension.equalsIgnoreCase("mp4") ||
                extension.equalsIgnoreCase("avi") ||
                extension.equalsIgnoreCase("mpg") ||
                extension.equalsIgnoreCase("rmvb") ||
                extension.equalsIgnoreCase("rm") ||
                extension.equalsIgnoreCase("asf") ||
                extension.equalsIgnoreCase("f4v") ||
                extension.equalsIgnoreCase("vob") ||
                extension.equalsIgnoreCase("mkv") ||
                extension.equalsIgnoreCase("3gp") ||
                extension.equalsIgnoreCase("mov")) {
            Glide.with(mContext).load(fileModel.getPath()).into(imageView);
        } else {
            String s = AvatarUtils.generateDefaultAvatar(mContext, extension);
            Glide.with(mContext).load(s).into(imageView);
        }

        SmoothCheckBox checkBox = holder.getView(R.id.checkbox);
        RelativeLayout layout = holder.getView(R.id.layout_item);
        holder.setText(R.id.tv_name, fileModel.getName());
        holder.setText(R.id.tv_detail, FileUtils.getDateTime(fileModel.getDate()) + "  -  " + formatFileSize(fileModel.getSize()));
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(fileModel.isSelected(), false);
        checkBox.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {
                if (!isChecked && fileModel.isSelected()) {
                    int index = findFileIndex(fileModel);
                    if (index != -1) {
                        mSelectedFileList.remove(index);
                    }
                    fileModel.setSelected(false);
                } else if (isChecked && !fileModel.isSelected()) {
                    if (mSelectedFileList.size() >= FileSelector.getInstance((Activity) mContext).maxCount) {
                        Toast.makeText(mContext, "您最多只能选择" + FileSelector.getInstance((Activity) mContext).maxCount + "个", Toast.LENGTH_SHORT).show();
                        checkBox.setChecked(false, true);
                        return;
                    }
                    mSelectedFileList.add(fileModel);
                    fileModel.setSelected(true);
                }
                mCountMenuItem.setTitle(String.format(mContext.getString(R.string.selected_file_count), String.valueOf(mSelectedFileList.size()), String.valueOf(FileSelector.getInstance((Activity) mContext).maxCount)));
            }
        });
        layout.setOnClickListener(v -> {
            if (fileModel.isSelected()) {
                int index = findFileIndex(fileModel);
                if (index != -1) {
                    mSelectedFileList.remove(index);
                }
                fileModel.setSelected(false);
            } else {
                if (mSelectedFileList.size() >= FileSelector.getInstance((Activity) mContext).maxCount) {
                    Toast.makeText(mContext, "您最多只能选择" + FileSelector.getInstance((Activity) mContext).maxCount + "个", Toast.LENGTH_SHORT).show();
                    return;
                }
                mSelectedFileList.add(fileModel);
                fileModel.setSelected(true);
            }
            checkBox.setChecked(fileModel.isSelected(), true);
            mCountMenuItem.setTitle(String.format(mContext.getString(R.string.selected_file_count), String.valueOf(mSelectedFileList.size()), String.valueOf(FileSelector.getInstance((Activity) mContext).maxCount)));
        });
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        String namePinyin = CharacterParser.getInstance().getSpelling(modelList.get(position).getName());
        return namePinyin.substring(0, 1).toUpperCase();
    }

    public String formatFileSize(long size) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString;
        if (size < 1024) {
            fileSizeString = df.format((double) size) + "B";
        } else if (size < 1048576) {
            fileSizeString = df.format((double) size / 1024) + "K";
        } else if (size < 1073741824) {
            fileSizeString = df.format((double) size / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) size / 1073741824) + "G";
        }
        return fileSizeString;
    }

    private int findFileIndex(FileModel item) {
        for (int i = 0; i < mSelectedFileList.size(); i++) {
            if (mSelectedFileList.get(i).getPath().equals(item.getPath())) {
                return i;
            }
        }
        return -1;
    }
}
