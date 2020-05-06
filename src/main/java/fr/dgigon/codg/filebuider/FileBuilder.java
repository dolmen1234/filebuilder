package fr.dgigon.codg.filebuider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * In some contests you will have to submit all your code in a single file. This
 * class is here to help you build this unique file by scanning the base class
 * of your code, reading the imported classes, parse them and build your file
 * containing all your imported classes as private classes in a unique file in
 * the default package
 * 
 * Usage:
 * Run the main of this class and pass as argument the path of the file where you have your main.
 * 
 * Example path : /src/builder/sample/Sample.java
 * 
 * Repris & adapté de :
 * 
 * @author Manwe
 * 
 * 
 */
public class FileBuilder {
    private static final String END_COMMENT = "*/";

    private static final List<String> NO_PUBLIC_REMOVE = new ArrayList<String>();
    static {
        NO_PUBLIC_REMOVE.add("public String toString()");
        NO_PUBLIC_REMOVE.add("public boolean equals(");
        NO_PUBLIC_REMOVE.add("static void main(");
        NO_PUBLIC_REMOVE.add("public int compare(");
        NO_PUBLIC_REMOVE.add("public boolean hasNext()");
        NO_PUBLIC_REMOVE.add("public String next()");
        NO_PUBLIC_REMOVE.add("public int nextInt()");
        NO_PUBLIC_REMOVE.add("public String nextLine()");
        NO_PUBLIC_REMOVE.add("public void println(String str)");
    }

    private static final Charset CHARSET = Charset.forName("UTF-8");
    private final Set<String> imports = new HashSet<String>();

    private final Set<String> knownFiles = new HashSet<String>();

    private final Map<String, ClassCode> innerClasses = new HashMap<String, ClassCode>();

    private FileBuilder() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 2) {
            System.err.println("Unexpected number of arguments : " +
                    args.length + ", expected 2 : main source file, dest dir");
        } else {
            watchAndBundle(args[0], args[1]);
        }
    }

    public static void bundle(File srcDir, File destDir) throws InterruptedException, IOException {
        bundle(srcDir.getAbsolutePath(), destDir.getAbsolutePath());
    }

    public static void bundle(String srcDir, String destDir) throws InterruptedException, IOException {
        final FileBuilder builder = new FileBuilder();
        final ClassCode treated = builder.processFile(srcDir, destDir);
        builder.write(treated, destDir);
        System.out.println(new Date().toString() + " : " + srcDir + " -> " + destDir);
    }

    public static void watchAndBundle(String srcDir, String destDir) throws InterruptedException, IOException {
        File src = new File(srcDir);
        File rootDir = src.isDirectory() ? src : src.getParentFile();

        Watcher watcher = new Watcher(rootDir);
        System.err.println("Start watch " + rootDir.getAbsolutePath());
        List<String> exludeDirNames = Collections.singletonList("test");
        while (true) {
            if (watcher.needBundle(exludeDirNames)) {
                bundle(srcDir, rootDir.getAbsolutePath());
                watcher.notifyBundle();
            }
            Thread.sleep(2000);
        }
    }

    private String importToPath(String importStr, String baseDir) {
        if (importStr.indexOf("java") != -1) {
            return importStr; 
        }
        final String className = importStr.substring(7).replaceAll(";", "");

        return toAbsolutePath(baseDir + "\\" + className.replaceAll("\\.", "/") + ".java");
    }

    private ClassCode processFile(String fileName, String baseDir) throws IOException {
        knownFiles.add(toAbsolutePath(fileName));
        final List<String> fileContent = readFile(new File(fileName));
        final ClassCode code = readFileContent(fileName, fileContent, baseDir);
        readPackageClasses(fileName, baseDir);
        return code;
    }

    private List<String> readFile(File src) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader buffRead = new BufferedReader(new InputStreamReader(new FileInputStream(src)))) {
            String line;
            while ((line = buffRead.readLine()) != null) {
                lines.add(line);
            }
        } catch (MalformedInputException excp) {
            System.err.println("Impossible de lire le fichier " + src.getAbsolutePath());
            throw new IOException(excp);
        } catch (AccessDeniedException excp) {
            System.err.println("Pas le droit de lire le fichier " + src.getAbsolutePath());
            throw new IOException(excp);
        }
        return lines;
    }

    private ClassCode readFileContent(String fileName, List<String> fileContent, String baseDir) throws IOException {
        final ClassCode code = new ClassCode(fileName);
        boolean fileKeyWordRead = false;
        boolean insideComment = false;
        String previousLine = "";
        for (final String line : fileContent) {
            String trimedLine = cleanLine(line, previousLine);
            if (insideComment) {
                if (trimedLine.contains(END_COMMENT)) {
                    insideComment = false;
                    String remainingCode = trimedLine.substring(trimedLine.indexOf(END_COMMENT) + END_COMMENT.length());
                    if (!remainingCode.trim().isEmpty()) {
                        fileKeyWordRead = addLineToCode(code, fileKeyWordRead, remainingCode, baseDir);
                    }
                }
                // We can skip comments since generated file size might be
                // limited
            } else if (trimedLine.isEmpty()) {
                // We don't need empty lines
            } else if (trimedLine.startsWith("//")) {
                // We can skip comments since generated file size might be
                // limited
            } else if (trimedLine.startsWith("/*")) {
                // We can skip comments since generated file size might be
                // limited
                if (!trimedLine.contains(END_COMMENT)) {
                    insideComment = true;
                }
            } else {
                fileKeyWordRead = addLineToCode(code, fileKeyWordRead, trimedLine, baseDir);
            }
            previousLine = line;
        }
        return code;
    }

    private String cleanLine(String line, String previousLine) {
        String concat = line;
        if (canPublicRemove(concat, previousLine)) {
            concat = concat.replaceAll("public ", " ");
        }
        concat = concat.replace('\t', ' ').trim();
        String clean;
        do {
            clean = concat;
            concat = clean.replaceAll("  ", " ");
        } while (!concat.equals(clean));

        clean = concat;
        for (String str : new String[] { "{", "}", "=", "(", ")", ",", "-", "+", "*", "/", ";", "<", ">", "?", ":", }) {
            clean = clean.replaceAll(" \\" + str, str).replaceAll("\\" + str + " ", str);
        }
        return clean;
    }

    private boolean canPublicRemove(String concat, String previousLine) {
        if (previousLine.contains("@Override")) {
            return false;
        }

        for (String noPublic : NO_PUBLIC_REMOVE) {
            if (concat.contains(noPublic)) {
                return false;
            }
        }

        return true;
    }

    private boolean addLineToCode(final ClassCode code, boolean fileKeyWordRead, final String line, String baseDir)
            throws IOException {
        if (line.startsWith("package ")) {
            // Do nothing, we'll remove the package info
        } else if (line.startsWith("import ")) {
            final String importedClassPath = importToPath(line, baseDir);
            if (!knownFiles.contains(importedClassPath)) {
                if (importedClassPath.indexOf('*') == -1 && Files.exists(Paths.get(toAbsolutePath(importedClassPath)))) {
                    innerClasses.put(importedClassPath, processFile(importedClassPath, baseDir));
                } else {
                    imports.add(line);
                }
            }
        } else {
            if (fileKeyWordRead) {
                code.afterClassContent.add(line);
            } else {
                if (line.contains("class ")) {
                    code.declaration(line, "class ", line.contains("abstract "));
                    fileKeyWordRead = true;
                } else if (line.contains("interface ")) {
                    code.declaration(line, "interface ", line.contains("abstract "));
                    fileKeyWordRead = true;
                } else if (line.contains("enum ")) {
                    code.declaration(line, "enum ", line.contains("abstract "));
                    fileKeyWordRead = true;
                } else {
                    code.beforeClassContent.add(line);
                }
            }

        }
        return fileKeyWordRead;
    }

    private void readPackageClasses(String fileName, String baseDir) {
        Path directory = Paths.get(fileName);
        if (!directory.toFile().isDirectory()) {
            directory = Paths.get(fileName).getParent();
        }
        DirectoryStream<Path> ds;
        try {
            ds = Files.newDirectoryStream(directory);
            for (final Path child : ds) {
                final String absolutePath = toAbsolutePath(child);
                if (Files.isDirectory(child)) {
                    readPackageClasses(child.toFile().getAbsolutePath(), baseDir);
                } else if (absolutePath.endsWith(".java") && !knownFiles.contains(absolutePath)) {
                    innerClasses.put(absolutePath, processFile(absolutePath, baseDir));
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private String toAbsolutePath(Path path) {
        return path.toFile().getAbsolutePath();
    }

    private String toAbsolutePath(String fileName) {
        return toAbsolutePath(Paths.get(fileName));
    }

    private void write(ClassCode treated, String destDir) {
        final String outputFile = destDir + "/" + treated.className() + ".java";

        final List<String> lines = new ArrayList<>();
        lines.addAll(imports);
        for (final String line : treated.beforeClassContent) {
            lines.add(line);
        }
        lines.add("class " + treated.className() + "{");
        for (final ClassCode innerClass : innerClasses.values()) {
            for (final String line : innerClass.beforeClassContent) {
                lines.add(line);
            }
            lines.add(innerClass.declaration() + "{");
            for (final String line : innerClass.afterClassContent) {
                lines.add(line);
            }
        }
        for (final String line : treated.afterClassContent) {
            lines.add(line);
        }

        for (int i = lines.size() - 1; i > 0; i--) {
            if (lines.get(i).replace('}', ' ').trim().isEmpty() && !lines.get(i - 1).contains("//")) {
                lines.set(i - 1, lines.get(i - 1) + lines.get(i));
                lines.remove(i);
            }
        }
        try {
            Files.write(Paths.get(outputFile), lines, CHARSET);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}