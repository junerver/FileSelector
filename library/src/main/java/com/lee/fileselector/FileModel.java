package com.lee.fileselector;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * @author Lee
 */
public class FileModel implements Parcelable {

    private String extension;
    private String name;
    private String path;
    private long size;
    private long date;
    private boolean isSelected;

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.extension);
        dest.writeString(this.name);
        dest.writeString(this.path);
        dest.writeLong(this.size);
        dest.writeLong(this.date);
        dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
    }

    public FileModel() {
    }

    protected FileModel(Parcel in) {
        this.extension = in.readString();
        this.name = in.readString();
        this.path = in.readString();
        this.size = in.readLong();
        this.date = in.readLong();
        this.isSelected = in.readByte() != 0;
    }

    public static final Parcelable.Creator<FileModel> CREATOR = new Parcelable.Creator<FileModel>() {
        @Override
        public FileModel createFromParcel(Parcel source) {
            return new FileModel(source);
        }

        @Override
        public FileModel[] newArray(int size) {
            return new FileModel[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileModel)) return false;
        FileModel fileModel = (FileModel) o;
        return path.equals(fileModel.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
