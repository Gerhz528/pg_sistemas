package dsi.plantilla.demo.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileService {

    // Guardaremos directo en uploads para que coincida con el HTML
    private final String UPLOAD_DIR = "uploads/";

    public String guardarArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs(); 

            String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String fileName = System.currentTimeMillis() + "_" + (int)(Math.random()*1000) + ext;
            
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}