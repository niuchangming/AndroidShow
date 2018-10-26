package ekoolab.com.show.beans;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.media.MediaPlayer;
import android.net.Uri;

import com.luck.picture.lib.cameralibrary.util.FileUtil;
import com.orhanobut.logger.Logger;

import java.io.File;

import ekoolab.com.show.beans.enums.FileType;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DataBaseManager;
import ekoolab.com.show.utils.FileUtils;

public class ResourceFile {
    public long chatMessageId;// temp no this value
    public String fileName;
    public String extension;
    public String filePath;
    public String fileUrl; // temp no this value
    public FileType fileType;
    public String mimeType;
    public long fileSize;
    public long duration;

    public ResourceFile(){}
    public ResourceFile(Context context, File file, FileType fileType) {
        this.fileName = file.getName();
        this.filePath = file.getPath();
        this.fileType = fileType;
        this.mimeType = FileUtils.getFileContentType(file);
        this.fileSize = FileUtils.getFileLength(file);
        this.extension = FileUtils.getFileExtension(file);

        if(fileType == FileType.AUDIO){
            duration = MediaPlayer.create(context, Uri.parse(file.getPath())).getDuration();
        }
    }

    public long save(Context context, long chatMessageId) {
        this.chatMessageId = chatMessageId;

        SQLiteDatabase db = DataBaseManager.getInstance(context).openDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.ResourceFileTableColumns.chatMessageId, this.chatMessageId);
        values.put(Constants.ResourceFileTableColumns.fileName, this.fileName);
        values.put(Constants.ResourceFileTableColumns.fileType, this.fileType.getIndex());
        values.put(Constants.ResourceFileTableColumns.filePath, this.filePath);
        values.put(Constants.ResourceFileTableColumns.fileUrl, this.fileUrl);
        values.put(Constants.ResourceFileTableColumns.mimeType, this.mimeType);
        values.put(Constants.ResourceFileTableColumns.fileSize, this.fileSize);
        values.put(Constants.ResourceFileTableColumns.extension, this.extension);
        values.put(Constants.ResourceFileTableColumns.duration, this.duration);

        long result = -1;
        synchronized (context) {
            result = db.insert(Constants.RESOURCE_FILE_TB, null, values);
        }

        if (db != null){
            DataBaseManager.getInstance(context).closeDatabase();
        }

        return result;
    }

    public static ResourceFile getResourceChatMessageId(Context context, long chatMessageId){
        SQLiteDatabase db = DataBaseManager.getInstance(context).openDatabase();

        Cursor cursor = null;
        ResourceFile resourceFile = null;
        try {
            cursor = db.query(Constants.RESOURCE_FILE_TB, null,
                    Constants.ResourceFileTableColumns.chatMessageId + "=?", new String[]{chatMessageId + ""}, null, null, null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                resourceFile = new ResourceFile();
                resourceFile.chatMessageId = cursor.getLong(cursor.getColumnIndexOrThrow(Constants.ResourceFileTableColumns.chatMessageId));
                resourceFile.fileName = cursor.getString(cursor.getColumnIndexOrThrow(Constants.ResourceFileTableColumns.fileName));
                resourceFile.fileType = FileType.getMediaType(cursor.getInt(cursor.getColumnIndexOrThrow(Constants.ResourceFileTableColumns.fileType)));
                resourceFile.filePath = cursor.getString(cursor.getColumnIndexOrThrow(Constants.ResourceFileTableColumns.filePath));
                resourceFile.fileUrl = cursor.getString(cursor.getColumnIndexOrThrow(Constants.ResourceFileTableColumns.fileUrl));
                resourceFile.mimeType = cursor.getString(cursor.getColumnIndexOrThrow(Constants.ResourceFileTableColumns.mimeType));
                resourceFile.fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(Constants.ResourceFileTableColumns.fileSize));
                resourceFile.extension = cursor.getString(cursor.getColumnIndexOrThrow(Constants.ResourceFileTableColumns.extension));
                resourceFile.duration = cursor.getLong(cursor.getColumnIndexOrThrow(Constants.ResourceFileTableColumns.duration));
            }
        } catch (SQLiteException e){
            Logger.e(e.getLocalizedMessage());
        }finally {
            if(cursor != null && !cursor.isClosed()){
                cursor.close();
            }
            if (db != null){
                DataBaseManager.getInstance(context).closeDatabase();
            }
        }
        return resourceFile;
    }

}
