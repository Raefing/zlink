package com.zlink.ui.img;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ImageLoader {
    private static Map<String, byte[]> imgDataMap = new HashMap<>();

    @Value("${system.imgPath}")
    private String rootPath;

    @PostConstruct
    public void ImageLoader() {
        File file = new File(rootPath);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                loadFile(f);
            }
        }
    }

    private void loadFile(File file) {
        log.error("{}", file.getName());
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            long size = file.length();
            byte[] temp = new byte[(int) size];
            fis.read(temp, 0, (int) size);
            fis.close();
            imgDataMap.put(file.getName(), temp);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] getImgData(String path) {
        return imgDataMap.get(path);
    }
}
