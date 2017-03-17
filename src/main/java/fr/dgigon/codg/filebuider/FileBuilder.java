package fr.dgigon.codg.filebuider;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
 * @author Manwe
 * 
 */
public class FileBuilder {
    private static final String END_COMMENT = "*/";

    private static final List<String> NO_PUBLIC_REMOVE = new ArrayList<String>();
    static {
        NO_PUBLIC_REMOVE.add("public String toString()");
        NO_PUBLIC_REMOVE.add("public boolean equals(");
    }

    private static class ClassCode {
        private final String classFile;

        /** class name or yes*/
        private String className;
        private String keyword;

        private final List<String> beforeClassContent = new ArrayList<String>();
        private final List<String> afterClassContent = new ArrayList<String>();

        private boolean isAbstract;

        ClassCode(String classFile) {
            this.classFile = classFile;
        }

        public void declaration(String line, String keyword, boolean isAbstract) {
            className = extractDeclaration(line, keyword);
            this.keyword = keyword;
            this.isAbstract = isAbstract;
        }

        public String className() {
            return className;
        }

        public String declaration() {
            return (isAbstract ? "static abstract " : "private static ") + keyword + className;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ClassCode other = (ClassCode) obj;
            if (classFile == null) {
                if (other.classFile != null) {
                    return false;
                }
            } else if (!classFile.equals(other.classFile)) {
                return false;
            }
            return true;
        }

        private String extractDeclaration(String line, String str) {
            return line.substring(line.indexOf(str) + str.length()).replaceAll("\\{", "").trim();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((classFile == null) ? 0 : classFile.hashCode());
            return result;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Unexpected number of arguments");
        } else {
            final FileBuilder builder = new FileBuilder();
            File rootDir = new File(args[0]).getParentFile();
            while (!rootDir.getName().equals("java")) {
                rootDir = rootDir.getParentFile();
            }

            final ClassCode treated = builder.processFile(args[0], rootDir.getAbsolutePath());
            builder.write(treated, args[1]);
            System.out.println("Fin du traitement");
        }
    }

    private static final Charset CHARSET = Charset.forName("UTF-8");
    private final Set<String> imports = new HashSet<String>();

    private final Set<String> knownFiles = new HashSet<String>();

    private final Map<String, ClassCode> innerClasses = new HashMap<String, ClassCode>();

    private FileBuilder() {
    }

    private String importToPath(String importStr, String baseDir) {
        final String className = importStr.substring(7).replaceAll(";", "");

        return toAbsolutePath(baseDir + "\\" + className.replaceAll("\\.", "/") + ".java");
    }

    private ClassCode processFile(String fileName, String baseDir) throws IOException {
        knownFiles.add(toAbsolutePath(fileName));
        final List<String> fileContent = readFile(fileName);
        final ClassCode code = readFileContent(fileName, fileContent, baseDir);
        readPackageClasses(fileName, baseDir);
        return code;
    }

    private List<String> readFile(String fileName) throws IOException {
        try {
            return Files.readAllLines(Paths.get(fileName), CHARSET);
        } catch (MalformedInputException excp) {
            System.err.println("Impossible de lire le fichier " + fileName);
            throw new IOException(excp);
        }
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
            clean = clean.replaceAll(" \\" + str, str).
                    replaceAll("\\" + str + " ", str);
        }
        return clean;
    }

    private boolean canPublicRemove(String concat, String previousLine) {
        if (previousLine.indexOf("@Override") != -1) {
            return false;
        }

        for (String noPublic : NO_PUBLIC_REMOVE) {
            if (concat.indexOf(noPublic) != -1) {
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
                if (Files.exists(Paths.get(toAbsolutePath(importedClassPath)))) {
                    innerClasses.put(importedClassPath, processFile(importedClassPath, baseDir));
                }
                else {
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
            if (lines.get(i).replace('}', ' ').trim().isEmpty() && lines.get(i - 1).indexOf("//") == -1) {
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