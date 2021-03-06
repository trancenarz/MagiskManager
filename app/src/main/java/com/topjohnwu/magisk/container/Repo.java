package com.topjohnwu.magisk.container;

import android.content.ContentValues;
import android.database.Cursor;

import com.topjohnwu.magisk.utils.Const;
import com.topjohnwu.magisk.utils.WebService;

import java.util.Date;

public class Repo extends BaseModule {

    private String repoName;
    private Date mLastUpdate;

    public Repo(String name, Date lastUpdate) throws IllegalRepoException {
        mLastUpdate = lastUpdate;
        repoName = name;
        update();
    }

    public Repo(Cursor c) {
        super(c);
        repoName = c.getString(c.getColumnIndex("repo_name"));
        mLastUpdate = new Date(c.getLong(c.getColumnIndex("last_update")));
    }

    public void update() throws IllegalRepoException {
        String props = WebService.getString(getManifestUrl());
        String lines[] = props.split("\\n");
        try {
            parseProps(lines);
        } catch (NumberFormatException e) {
            throw new IllegalRepoException("Repo [" + repoName + "] parse error: " + e.getMessage());
        }

        if (getId() == null) {
            throw new IllegalRepoException("Repo [" + repoName + "] does not contain id");
        }
        if (getVersionCode() < 0) {
            throw new IllegalRepoException("Repo [" + repoName + "] does not contain versionCode");
        }
        if (getTemplateVersion() < Const.Value.MIN_TEMPLATE_VER) {
            throw new IllegalRepoException("Repo [" + repoName + "] is outdated");
        }
    }

    public boolean update(Date lastUpdate) throws IllegalRepoException {
        if (lastUpdate.after(mLastUpdate)) {
            mLastUpdate = lastUpdate;
            update();
            return true;
        }
        return false;
    }

    public ContentValues getContentValues() {
        ContentValues values = super.getContentValues();
        values.put("repo_name", repoName);
        values.put("last_update", mLastUpdate.getTime());
        return values;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getZipUrl() {
        return String.format(Const.Url.ZIP_URL, repoName);
    }

    public String getManifestUrl() {
        return String.format(Const.Url.FILE_URL, repoName, "module.prop");
    }

    public String getDetailUrl() {
        return String.format(Const.Url.FILE_URL, repoName, "README.md");
    }

    public Date getLastUpdate() {
        return mLastUpdate;
    }

    public class IllegalRepoException extends Exception {
        IllegalRepoException(String message) {
            super(message);
        }
    }
}
