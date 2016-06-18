package com.oddsoft.quicktranslatex.controller;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;

import java.io.File;


/**
 * Created by andycheng on 2016/6/18.
 */
public class TheBackupAgent extends BackupAgentHelper {

    private static final String DB_NAME = "history.db";

    @Override
    public void onCreate() {
        FileBackupHelper dbs = new FileBackupHelper(this, DB_NAME);
        addHelper("dbs", dbs);
    }

    @Override
    public File getFilesDir() {
        final File f = getDatabasePath(DB_NAME);
        return f.getParentFile();
    }
}
