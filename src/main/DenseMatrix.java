package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class DenseMatrix implements Matrix {
    public int size;
    public double matrix[][];


    public DenseMatrix(double[][] matrix, int size) {
        this.size = size;
        this.matrix = matrix;
    }

    public DenseMatrix(int size) {
        this.matrix = new double[size][size];
        this.size = size;
    }

    public DenseMatrix(String fileName) {
        try {
            File f = new File(fileName);
            Scanner in = new Scanner(f);
            String[] line;
            ArrayList<Double[]> a = new ArrayList<>();
            Double[] tmp = {};
            while (in.hasNextLine()) {
                line = in.nextLine().split(" ");
                tmp = new Double[line.length];
                for (int i = 0; i < tmp.length; i++) {
                    tmp[i] = Double.parseDouble(line[i]);
                }
                a.add(tmp);
            }
            double[][] mat = new double[a.size()][tmp.length];
            for (int i = 0; i < mat.length; i++) {
                for (int j = 0; j < mat[0].length; j++) {
                    mat[i][j] = a.get(i)[j];
                }
            }
            matrix = mat;
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public Matrix mul(Matrix other) {
        if (other instanceof DenseMatrix) try {
            return this.threadMul((DenseMatrix) other);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        else return this.mulDenceSparse((SparseMatrix) other);
    }


    public DenseMatrix threadMul(DenseMatrix other) throws InterruptedException {
        other = other.MatrixSTrans();
        DenseMatrix result = new DenseMatrix(size);
        mulDD t = new mulDD(this.matrix, other.matrix, result.matrix);
        Thread t1 = new Thread(t);
        Thread t2 = new Thread(t);
        Thread t3 = new Thread(t);
        Thread t4 = new Thread(t);

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t1.join();
        t2.join();
        t3.join();
        t4.join();
        return result;
    }

    public class mulDD implements Runnable {
        double[][] A;
        double[][] B;
        double[][] result;
        int num = 0;

        public mulDD(double[][] A, double[][] B, double[][] result) {
            this.A = A;
            this.B = B;
            this.result = result;
        }

        public void run() {
            for (int i = next(); i < size; i = next()) {
                for (int j = 0; j < size; j++) {
                    for (int k = 0; k < size; k++) {
                        result[i][j] += A[i][k] * B[j][k];
                    }
                }

            }
        }

        public int next() {
            synchronized (this) {
                return num++;
            }
        }
    }


    public SparseMatrix mulDenceSparse(SparseMatrix other) {
        other = other.MatrixSTrans();
        SparseMatrix res = new SparseMatrix(matrix.length, other.cols);
        for (int i = 0; i < size; i++) {
            HashMap<Integer, Double> resRow = new HashMap<Integer, Double>();
            Iterator<Map.Entry<Integer, HashMap<Integer, Double>>> iter1 = other.map.entrySet().iterator();// итератор строк
            while (iter1.hasNext()) {
                Map.Entry entry1 = iter1.next();
                Integer key1 = (Integer) entry1.getKey();// ключ строки
                HashMap<Integer, Double> value1 = (HashMap<Integer, Double>) entry1.getValue();// сама строка
                Iterator iterElement = value1.entrySet().iterator();// итератор элементов
                double resValue = 0;
                while (iterElement.hasNext()) {
                    Map.Entry entryElement = (Map.Entry) iterElement.next();
                    Integer keyElement = (Integer) entryElement.getKey();// ключ элемента
                    Double valueElement = (Double) entryElement.getValue();//значение элемента
                    resValue = resValue + this.matrix[i][keyElement] * valueElement;
                }
                if (resValue != 0) {
                    resRow.put(key1, resValue);
                }
            }
            if (resRow != null) {
                res.map.put(i, resRow);
            }
        }
        return res;
    }


    public DenseMatrix MatrixSTrans() {
        double[][] mTr = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = i; j < size; j++) {
                double aT = this.matrix[i][j];
                mTr[i][j] = this.matrix[j][i];
                mTr[j][i] = aT;
            }
        }
        return new DenseMatrix(mTr, size);
    }


    public void matOut(BufferedWriter dn) {
        try {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    dn.write(matrix[i][j] + " ");

                }
                dn.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean equals(Object o) {
        boolean t = true;
        if (!(o instanceof DenseMatrix)) {
            return false;
        }
        DenseMatrix other = (DenseMatrix) o;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (this.matrix[i][j] != other.matrix[i][j]) {
                    t = false;
                }
            }
        }
        return t;
    }
}