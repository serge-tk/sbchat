package org.example.picture;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class PictureTypeTest {
    @Test
    public void testGetType() throws Exception {
        assertEquals(PictureType.JPEG, getType("brian.jpg"));
        assertEquals(PictureType.PNG, getType("stewie.png"));
        assertEquals(PictureType.GIF, getType("meg.gif"));
        assertEquals(null, getType("chris.tiff"));
    }

    private PictureType getType(String filename) throws IOException, URISyntaxException {
        byte[] data = Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(filename).toURI()));
        return PictureType.detectType(data);
    }
}
