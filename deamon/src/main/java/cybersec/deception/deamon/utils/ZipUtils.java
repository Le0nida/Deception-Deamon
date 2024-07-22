package cybersec.deception.deamon.utils;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static byte[] getZip(String path) throws IOException {
        File sourceDirectory = new File(path);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            zipFile(sourceDirectory, sourceDirectory.getName(), zos);

            zos.finish();
            zos.flush();

            byte[] zipBytes = baos.toByteArray();

            // Verifica la validità del file ZIP
            if (isZipValid(zipBytes)) {
                return zipBytes;
            } else {
                throw new IOException("Il file ZIP non è valido.");
            }
        }
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOutputStream) throws IOException {
        if (fileToZip.isDirectory()) {
            for (File file : Objects.requireNonNull(fileToZip.listFiles())) {
                zipFile(file, fileName + "/" + file.getName(), zipOutputStream);
            }
        } else {
            try (FileInputStream fis = new FileInputStream(fileToZip)) {
                ZipEntry zipEntry = new ZipEntry(fileName);
                zipOutputStream.putNextEntry(zipEntry);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    zipOutputStream.write(buffer, 0, bytesRead);
                }
                zipOutputStream.closeEntry();
            }
        }
    }

    private static boolean isZipValid(byte[] zipBytes) throws IOException {
        try (InputStream is = new ByteArrayInputStream(zipBytes);
             ZipArchiveInputStream zis = new ZipArchiveInputStream(is)) {
            return zis.getNextEntry() != null;
        }
    }

    public static void extractAndDeleteZip(String zipFilePath) throws IOException {
        File zipFile = new File(zipFilePath);
        if (!zipFile.exists()) {
            throw new IOException("File not found: " + zipFilePath);
        }

        // Ottieni la cartella in cui si trova il file .zip
        Path parentDir = zipFile.toPath().getParent();

        // Estrai il contenuto del file .zip
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                Path newFilePath = parentDir.resolve(zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newFilePath);
                } else {
                    // Assicurati che la directory di destinazione esista
                    Files.createDirectories(newFilePath.getParent());
                    try (FileOutputStream fos = new FileOutputStream(newFilePath.toFile())) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }

        // Elimina il file .zip
        Files.delete(zipFile.toPath());
    }

}
