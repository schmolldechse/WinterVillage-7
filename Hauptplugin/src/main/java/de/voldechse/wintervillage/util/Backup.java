package de.voldechse.wintervillage.util;

import de.voldechse.wintervillage.WinterVillage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Backup {

    private final WinterVillage plugin;

    private final int MAX_BACKUPS;

    public Backup(WinterVillage plugin) {
        this.plugin = plugin;
        this.MAX_BACKUPS = 15;
    }

    public File folderToZip(File folder, String name) {
        try {
            File backupsFolder = new File(this.plugin.getInstance().getDataFolder(), "backups");
            backupsFolder.mkdirs();

            purgeBackups(backupsFolder);

            File zipFile = new File(backupsFolder, name);
            zipFile.createNewFile();

            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            addFolderToZip("", folder, zos);

            zos.close();
            fos.close();

            this.plugin.getInstance().getLogger().info("Created backup");
            return zipFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void purgeBackups(File logDir) {
        File[] logFiles = logDir.listFiles();
        long oldestDate = Long.MAX_VALUE;
        File oldestFile = null;
        if (logFiles != null && logFiles.length >= this.MAX_BACKUPS) {
            for (File f : logFiles) {
                if (f.lastModified() < oldestDate) {
                    oldestDate = f.lastModified();
                    oldestFile = f;
                }
            }

            if (oldestFile != null) deleteFolder(oldestFile);
        }
    }

    private void addFolderToZip(String parentPath, File folder, ZipOutputStream zos) throws IOException {
        if (folder.getName().equals("backups")) return;

        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                String path = parentPath + file.getName() + "/";
                ZipEntry zipEntry = new ZipEntry(path);
                zos.putNextEntry(zipEntry);
                addFolderToZip(path, file, zos);
                zos.closeEntry();
            } else {
                try {
                    ZipEntry zipEntry = new ZipEntry(parentPath + file.getName());
                    zos.putNextEntry(zipEntry);

                    FileInputStream fis = new FileInputStream(file);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }

                    fis.close();
                    zos.closeEntry();
                } catch (Exception e) {
                    this.plugin.getInstance().getLogger().severe("Skipped " + file.getName() + " due to OS protection");
                }
            }
        }
    }

    private void deleteFolder(File folder) {
        if (folder == null) return;

        if (folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                deleteFolder(file);
            }
        }

        folder.delete();
    }
}