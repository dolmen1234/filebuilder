package fr.dgigon.codg.filebuilder;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import fr.dgigon.codg.filebuider.FileBuilder;

public class BuilderTest {

    @Test
    public void testBundle() throws IOException, InterruptedException {
        File destDir = new File(System.getProperty("java.io.tmpdir"));
        File srcDir = new File("src\\test\\resources\\in\\Player.java");
        Assert.assertTrue(srcDir.exists());
        System.err.println(srcDir.getAbsolutePath());
        FileBuilder.bundle(srcDir, destDir);
        Assert.assertTrue(new File(destDir, "Player.java").exists());
//        FileBuilder.watchAndBundle(srcDir.getAbsolutePath(), destDir.getAbsolutePath());
    }
    
}
