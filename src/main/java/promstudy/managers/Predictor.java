package promstudy.managers;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.nio.FloatBuffer;
import java.util.Arrays;

public class Predictor {
    Session s;

    public Predictor() {
        try {
            //SavedModelBundle smb = SavedModelBundle.load("model_ara1", "serve");
            //s = smb.session();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public Predictor(String parameter) {
        try {
            SavedModelBundle smb = SavedModelBundle.load(parameter, "serve");
            s = smb.session();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float[] predict(float[][][] sequences) {
        int batchSize = 50;
        int n = (int) (Math.ceil((float) (sequences.length) / batchSize));
        float[] result = new float[sequences.length];
        for (int b = 0; b < n; b++) {
            int end = b * batchSize + batchSize;
            if (b == n - 1) {
                end = sequences.length;
            }
            float[][][] subSeq = Arrays.copyOfRange(sequences, b * batchSize, end);
            long[] shape = new long[]{subSeq.length, subSeq[0].length, subSeq[0][0].length};
            Tensor in1 = Tensor.create(shape, makeFloatBuffer(subSeq));
            float t = 1;
            Tensor in2 = Tensor.create(t);
            Tensor tm = Tensor.create(false);
            //.feed("kr2", in2)
            Tensor out = s.runner().feed("input_prom", in1).feed("kr", in2).feed("in_training_mode", tm).fetch("output_prom").run().get(0);
            float[][] temp = (float[][]) out.copyTo(new float[subSeq.length][2]);
            //float[][] temp = (float[][]) out.copyTo(new float[subSeq.length][2]);
            for (int i = 0; i < temp.length; i++) {
                result[b * batchSize + i] = temp[i][0];
            }
            in1.close();
            in2.close();
            out.close();
        }
        return result;
    }

    public static FloatBuffer makeFloatBuffer(float[][][] sequences) {
        int size = sequences.length * sequences[0].length * sequences[0][0].length;
        FloatBuffer buf = FloatBuffer.allocate(size);
        for (int i = 0; i < sequences.length; i++) {
            for (int j = 0; j < sequences[i].length; j++) {
                for (int k = 0; k < sequences[i][j].length; k++) {
                    buf.put(sequences[i][j][k]);
                }
            }
        }
        buf.flip();
        return buf;
    }

    public int loadModel(String parameter) {
        try {
            SavedModelBundle smb = SavedModelBundle.load(parameter, "serve");
            s = smb.session();
            return (int) smb.graph().operation("input_java").output(0).shape().size(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
