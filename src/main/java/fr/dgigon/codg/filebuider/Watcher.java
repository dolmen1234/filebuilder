package fr.dgigon.codg.filebuider;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dire si qqch a changé
 * 
 * @author GIGOND
 *
 */
public class Watcher {

    private static final List<String> EXTENSIONS = Collections.singletonList(".java");

    private final File baseDir;
    private long lastBundle;
    private Map<Long, File> files = new HashMap<>();

    public Watcher(File baseDir) {
        this.baseDir = baseDir;
        lastBundle = System.currentTimeMillis();
    }

    private long lastModified(File file, List<String> exludeDirNames) {
        if (!file.exists()) {
            return 0L;
        }
        if (file.isDirectory()) {
            if (exludeDirNames.contains(file.getName())) {
                return 0L;
            }
            long max = 0L;
            for (File sub : file.listFiles()) {
                max = Math.max(max, lastModified(sub, Collections.emptyList()));
            }
            return max;
        }
        for (String ext : EXTENSIONS) {
            if (file.getName().endsWith(ext)) {
                files.put(file.lastModified(), file);
                return file.lastModified();
            }
        }
        return 0L;
    }

    /**
     * 
     * @return true si un des fichiers *.java dans l'arborescence a changé
     */
    public boolean needBundle() {
        return needBundle(Collections.emptyList());
    }

    /**
     * 
     * @param exludeDirNames on ne scanne pas les répertoires qui ont ces noms
     * @return true si un des fichiers *.java dans l'arborescence a changé
     */
    public boolean needBundle(List<String> exludeDirNames) {
        files.clear();
        long lastM = lastModified(baseDir, exludeDirNames);
        if (lastM > lastBundle) {
            return true;
        }
        return false;
    }

    public void notifyBundle() {
        lastBundle = System.currentTimeMillis();
    }
}
