/*
 * Copyright (C) 2018 Alexandru Munteanu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.almunt.dalidashboard;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;

public class DownloadService extends IntentService {
    public static final int UPDATE_PROGRESS = 1;
    public String filename;
    public boolean continueDownload = true;
    public String internalStorageDir;
    public int imageIndex;
    boolean error = false;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        filename = intent.getStringExtra("url");
        String newFileName = filename + ".temp";
        internalStorageDir = intent.getStringExtra("internalStorageDir");
        imageIndex = intent.getIntExtra("imageIndex", -1);
        String urlToDownload = "http://mappy.dali.dartmouth.edu/" + intent.getStringExtra("url");
        if (imageIndex > -1)
            urlToDownload = "http://mappy.dali.dartmouth.edu/images/" + intent.getStringExtra("url");
        ResultReceiver receiver = intent.getParcelableExtra("receiver");
        try {
            URL url = new URL(urlToDownload);
            URLConnection connection = url.openConnection();
            connection.connect();
            int fileLength = connection.getContentLength();

            InputStream input = new BufferedInputStream(connection.getInputStream());
            File tempDir = new File(internalStorageDir + "/temp/");
            tempDir.mkdir();
            OutputStream output = new FileOutputStream(internalStorageDir + "/temp/" + newFileName);
            byte data[] = new byte[1024];
            int count;
            File temp = new File(internalStorageDir + "/temp/nd");
            while ((count = input.read(data)) != -1 && continueDownload) {
                Bundle resultData = new Bundle();
                resultData.putInt("imageIndex", imageIndex);
                resultData.putString("filename", filename);
                resultData.putBoolean("error", false);
                resultData.putBoolean("done", false);
                receiver.send(UPDATE_PROGRESS, resultData);
                output.write(data, 0, count);
                if (temp.exists()) {
                    continueDownload = false;
                    temp.delete();
                }
            }
            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            ClearTemp();
            System.out.println(e.getMessage());
            Bundle resultData = new Bundle();
            resultData.putString("filename", filename);
            resultData.putBoolean("error", true);
            resultData.putInt("imageIndex", imageIndex);
            resultData.putBoolean("done", false);
            resultData.putString("errorDetails", e.getMessage());
            receiver.send(UPDATE_PROGRESS, resultData);
            error = true;
        }
        if (error)
            return;
        if (this.continueDownload) {
            try {
                if (new File(internalStorageDir + "/temp/" + newFileName).length() > 5)
                    copy(new File(internalStorageDir + "/temp/" + newFileName), new File(internalStorageDir + "/" + filename));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bundle resultData = new Bundle();
            resultData.putBoolean("done", true);
            resultData.putBoolean("error", false);
            resultData.putString("filename", filename);
            resultData.putInt("imageIndex", imageIndex);
            receiver.send(UPDATE_PROGRESS, resultData);
            ClearTemp();
        }

    }

    public void ClearTemp() {
        String dir = internalStorageDir + "/";
        File toBedeletedFolder = new File(dir + "/temp");
        if (toBedeletedFolder.exists()) {
            File[] contents = toBedeletedFolder.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    f.delete();
                }
            }
        }
        toBedeletedFolder.delete();
    }

    public void copy(File sourceFile, File destinationFile) throws IOException {
        FileInputStream inStream = new FileInputStream(sourceFile);
        FileOutputStream outStream = new FileOutputStream(destinationFile);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }
}