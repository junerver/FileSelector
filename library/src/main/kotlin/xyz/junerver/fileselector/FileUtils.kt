package xyz.junerver.fileselector;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;

/**
 * 文件处理
 *
 * @author Lee
 */
public class FileUtils {

    public static final int BY_NAME_ASC = 0;
    public static final int BY_NAME_DESC = 1;
    public static final int BY_TIME_ASC = 2;
    public static final int BY_TIME_DESC = 3;
    public static final int BY_SIZE_ASC = 4;
    public static final int BY_SIZE_DESC = 5;
    public static final int BY_EXTENSION_ASC = 6;
    public static final int BY_EXTENSION_DESC = 7;
    public static final String RESULT_KEY = "extra_result";

    public static String getDateTime(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.PRC);
        return sdf.format(cal.getTime());
    }

    /**
     * 获取文件名
     */
    public static String getNameExcludeExtension(String path) {
        try {
            String fileName = (new File(path)).getName();
            int lastIndexOf = fileName.lastIndexOf(".");
            if (lastIndexOf != -1) {
                fileName = fileName.substring(0, lastIndexOf);
            }
            return fileName;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取文件名
     */
    public static String getName(String path) {
        int dotPos = path.lastIndexOf(File.separator);
        if (dotPos >= 0) {
            return path.substring(dotPos + 1);
        } else {
            return "?";
        }
    }

    /**
     * 获取文件扩展名
     */
    public static String getExtensionByName(String name) {
        int dotPos = name.lastIndexOf('.');
        if (dotPos >= 0) {
            return name.substring(dotPos + 1);
        } else {
            return "?";
        }
    }

    /**
     * 获取文件扩展名
     */
    public static String getExtension(String pathOrUrl) {
        String name = pathOrUrl.substring(pathOrUrl.lastIndexOf("/"));
        int dotPos = name.lastIndexOf('.');
        if (dotPos >= 0) {
            return name.substring(dotPos + 1);
        } else {
            return "?";
        }
    }

    public static class SortByExtension implements Comparator<FileModel> {

        public SortByExtension() {
            super();
        }

        @Override
        public int compare(FileModel f1, FileModel f2) {
            if (f1 == null || f2 == null) {
                if (f1 == null) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                String ext_1 = f1.getExtension();
                String ext_2 = f2.getExtension();
                if (ext_1.equalsIgnoreCase(ext_2)) {
                    return new SortByName().compare(f1, f2);
                } else {
                    return ext_1.compareToIgnoreCase(ext_2);
                }
            }
        }
    }


    public static class SortByName implements Comparator<FileModel> {

        public SortByName() {
            super();
        }

        @Override
        public int compare(FileModel f1, FileModel f2) {
            if (f1 == null || f2 == null) {
                if (f1 == null) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        }
    }

    public static class SortBySize implements Comparator<FileModel> {

        public SortBySize() {
            super();
        }

        @Override
        public int compare(FileModel f1, FileModel f2) {
            if (f1 == null || f2 == null) {
                if (f1 == null) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                if (f1.getSize() < f2.getSize()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }

    public static class SortByTime implements Comparator<FileModel> {

        public SortByTime() {
            super();
        }

        @Override
        public int compare(FileModel f1, FileModel f2) {
            if (f1 == null || f2 == null) {
                if (f1 == null) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                if (f1.getDate() > f2.getDate()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
    }
}
