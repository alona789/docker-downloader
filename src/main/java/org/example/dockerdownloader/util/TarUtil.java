package org.example.dockerdownloader.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

/**
 * Tar 工具类
 *
 * @author XSJ
 * @version 1.0.0
 */
public class TarUtil {
    private static final Logger log = LoggerFactory.getLogger(TarUtil.class);


    public static void createTarGzipFolder(Path source, Path target) throws IOException {
        createTarGzipFolder(source, target, true);
    }

    public static void createTarGzipFolder(Path source, Path target, boolean buffered) throws IOException {

        if (!Files.isDirectory(source)) {
            throw new IOException("Please provide a directory.");
        }

        // get folder name as zip file name
        ArrayList<OutputStream> osList = new ArrayList<>(2);

        OutputStream latestOs = null;
        try {
            latestOs = Files.newOutputStream(target);
            osList.add(latestOs);
            if (buffered) {
                latestOs = new BufferedOutputStream(latestOs);
                osList.add(latestOs);
            }
            if (target.getFileName().endsWith(".gz")) {
                latestOs = new GzipCompressorOutputStream(latestOs);
                osList.add(latestOs);
            }
        } catch (IOException e) {
            IOUtils.closeQuietly(osList.toArray(new OutputStream[]{}));
            throw e;
        }

        try (TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(latestOs)) {

            Files.walkFileTree(source, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {

                    // only copy files, no symbolic links
                    if (attributes.isSymbolicLink()) {
                        return FileVisitResult.CONTINUE;
                    }

                    // get filename
                    Path targetFile = source.relativize(file);

                    try {
                        TarArchiveEntry tarEntry = new TarArchiveEntry(file.toFile(), targetFile.toString());

                        tarArchiveOutputStream.putArchiveEntry(tarEntry);
                        Files.copy(file, tarArchiveOutputStream);
                        tarArchiveOutputStream.closeArchiveEntry();

                        log.debug("Added file to tar.gz: {}", file);

                    } catch (IOException e) {
                        log.error("Unable to tar.gz : {}", file, e);

                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    log.error("Unable to tar.gz : {}", file.toString(), exc);
                    return FileVisitResult.CONTINUE;
                }

            });

            tarArchiveOutputStream.finish();
        } finally {
            IOUtils.closeQuietly(osList.toArray(new OutputStream[]{}));
        }
    }


    /**
     * 来自 stackoverflow
     *
     * @param source
     * @throws IOException
     * @see <a href="https://stackoverflow.com/a/63396272/24178212">stackoverflow</a>
     */
    @Deprecated
    public static void createTarGzipFolder(Path source) throws IOException {

        if (!Files.isDirectory(source)) {
            throw new IOException("Please provide a directory.");
        }

        // get folder name as zip file name
        String tarFileName = source.getFileName().toString() + ".tar.gz";

        try (OutputStream fOut = Files.newOutputStream(Paths.get(tarFileName));
             BufferedOutputStream buffOut = new BufferedOutputStream(fOut);
             GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(buffOut);
             TarArchiveOutputStream tOut = new TarArchiveOutputStream(gzOut)) {

            Files.walkFileTree(source, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {

                    // only copy files, no symbolic links
                    if (attributes.isSymbolicLink()) {
                        return FileVisitResult.CONTINUE;
                    }

                    // get filename
                    Path targetFile = source.relativize(file);

                    try {
                        TarArchiveEntry tarEntry = new TarArchiveEntry(file.toFile(), targetFile.toString());

                        tOut.putArchiveEntry(tarEntry);
                        Files.copy(file, tOut);
                        tOut.closeArchiveEntry();

                        log.debug("Added file to tar.gz: {}", file);

                    } catch (IOException e) {
                        log.error("Unable to tar.gz : {}", file, e);

                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    log.error("Unable to tar.gz : {}", file.toString(), exc);
                    return FileVisitResult.CONTINUE;
                }

            });

            tOut.finish();
        }
    }


    public static void decompressGzipFile(String gzipFile, String newFile) throws IOException {
        try (FileInputStream in = new FileInputStream(gzipFile);
             GZIPInputStream gzip = new GZIPInputStream(in);
             FileOutputStream out = new FileOutputStream(newFile)) {
            IOUtils.copy(gzip, out);
        }
    }

    public static void decompressGzipFile(InputStream gzipFile, String newFile) throws IOException {
        try (GZIPInputStream gzip = new GZIPInputStream(gzipFile);
             FileOutputStream out = new FileOutputStream(newFile)) {
            IOUtils.copy(gzip, out);
        }
    }
}
