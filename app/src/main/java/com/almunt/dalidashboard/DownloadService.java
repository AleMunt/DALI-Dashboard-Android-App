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

/**
 * A download service used to download files
 */
public class DownloadService extends IntentService {

    /**
     * Identifies the DownloadService
     */
    public static final int UPDATE_PROGRESS = 1;
    public String filename;
    public boolean continueDownload = true;
    public String internalStorageDir;
    public int imageIndex;
    boolean error = false;

    public DownloadService() {
        super("DownloadService");
    }

    /**
     * Handles an incoming intent with download details
     * @param intent The intent containing all download details
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        filename = intent.getStringExtra("url");
        String newFileName = filename + ".temp";
        internalStorageDir = intent.getStringExtra("internalStorageDir");
        imageIndex = intent.getIntExtra("imageIndex", -1);
        String urlToDownload = "http://mappy.dali.dartmouth.edu/" + intent.getStringExtra("url");

        // If imageIndex is greater than -1 then it is an image
        if (imageIndex > -1)
            urlToDownload = "http://mappy.dali.dartmouth.edu/images/" + intent.getStringExtra("url");
        ResultReceiver receiver = intent.getParcelableExtra("receiver");

        // Store file in a temporary folder while downloading
        try {
            URL url = new URL(urlToDownload);
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream input = new BufferedInputStream(connection.getInputStream());
            File tempDir = new File(internalStorageDir + "/temp/");
            tempDir.mkdir();
            OutputStream output = new FileOutputStream(internalStorageDir + "/temp/" + newFileName);
            byte data[] = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1 && continueDownload) {
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            // If their is an error then send a Bundle containing all of the details to the Download Receiver.
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
        // If the download completes then remove file from temporary folder, clear temporary folder,
        // and send completed download details to the Download Receiver
        if (this.continueDownload) {
            try {
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

    /**
     * Clears the temporary folder
     */
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

    /**
     * Copies a file
     * @param sourceFile The source file that it copied
     * @param destinationFile The copied file destination
     * @throws IOException
     */
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