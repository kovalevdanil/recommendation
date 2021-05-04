package ua.kovalev.recommendation.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class SerializationUtilsTest {
    @Test
    public void serializeTest() throws IOException, ClassNotFoundException {
        double[] array = new double[]{ 1, 2, 3, 4};
        byte[] bytes = SerializeUtils.serializeDoubleArray(array);

        double[] deserialized = SerializeUtils.deserializeByteArrayToDoubleArray(bytes);

        assertEquals(array.length, deserialized.length);
        for (int i = 0; i < array.length; i++){
            assertEquals(array[i], deserialized[i]);
        }
    }

    @Test
    public void serializeOneDoubleTest() throws Exception{
        double[] array = new double[] {1};
        byte[] serialized = SerializeUtils.serializeDoubleArray(array);

        double[] deserialized = SerializeUtils.deserializeByteArrayToDoubleArray(serialized);

        assertEquals(array.length, deserialized.length);
        assertEquals(array[0], deserialized[0]);
    }
}
