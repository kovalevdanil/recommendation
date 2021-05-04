package ua.kovalev.recommendation.utils;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@UtilityClass
public class SerializeUtils {

    public byte[] serializeDoubleArray(double[] array) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        ObjectOutputStream os = new ObjectOutputStream(byteStream);

        os.writeObject(array);

        return byteStream.toByteArray();
    }

    public double[] deserializeByteArrayToDoubleArray(byte[] array) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(array);

        ObjectInputStream is = new ObjectInputStream(byteStream);

        return (double[]) is.readObject();
    }

}
