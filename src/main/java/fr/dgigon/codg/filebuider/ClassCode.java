package fr.dgigon.codg.filebuider;

import java.util.ArrayList;
import java.util.List;

public class ClassCode {
    private final String classFile;

    /** class name or yes */
    private String className;
    private String keyword;

    public final List<String> beforeClassContent = new ArrayList<String>();
    public final List<String> afterClassContent = new ArrayList<String>();

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
