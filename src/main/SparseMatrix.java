package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class SparseMatrix implements Matrix {

    public int rows;
    public int cols;
    public ConcurrentHashMap<Integer, HashMap<Integer, Double>> map;

    public SparseMatrix(ConcurrentHashMap<Integer, HashMap<Integer, Double>> m, int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.map = m;
    }


    public SparseMatrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.map = new ConcurrentHashMap<>();
    }

    public SparseMatrix(String fileName) {
        rows = 0;
        cols = 0;
        ConcurrentHashMap<Integer, HashMap<Integer, Double>> matrix = new ConcurrentHashMap<>();
        try {
            File f = new File(fileName);
            Scanner in = new Scanner(f);
            String[] line = {};
            HashMap<Integer, Double> temp;
            while (in.hasNextLine()) {
                temp = new HashMap<>();
                line = in.nextLine().split(" ");
                for (int i = 0; i < line.length; i++) {
                    if (line[i] != "0") {
                        temp.put(i, Double.parseDouble(line[i]));
                    }
                }
                if (temp.size()!= 0) {
                    matrix.put(rows++, temp);
                }
            }
            cols = line.length;
            map = matrix;
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public Matrix mul(Matrix other) {
        if (other instanceof SparseMatrix) try {
            return this.treadMul((SparseMatrix) other);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        else return this.mulSparseDence((DenseMatrix) other);
    }

    public SparseMatrix treadMul(SparseMatrix other) throws InterruptedException {

        other = other.MatrixSTrans();
        SparseMatrix result = new SparseMatrix(rows, cols);
        Iterator<ConcurrentHashMap.Entry<Integer, HashMap<Integer, Double>>> iter1 = this.map.entrySet().iterator();
        MulSS t = new MulSS(this.map, other.map, result.map, iter1);

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

    class MulSS implements Runnable {
        ConcurrentHashMap<Integer, HashMap<Integer, Double>> A;
        ConcurrentHashMap<Integer, HashMap<Integer, Double>> B;
        ConcurrentHashMap<Integer, HashMap<Integer, Double>> result;
        Iterator<ConcurrentHashMap.Entry<Integer, HashMap<Integer, Double>>> iter1;

        public MulSS(ConcurrentHashMap<Integer, HashMap<Integer, Double>> A, ConcurrentHashMap<Integer, HashMap<Integer, Double>> B, ConcurrentHashMap<Integer, HashMap<Integer, Double>> result, Iterator<ConcurrentHashMap.Entry<Integer, HashMap<Integer, Double>>> iter1) {
            this.A = A;
            this.B = B;
            this.result = result;
            this.iter1 = iter1;
        }

        public void run() {
            while (iter1.hasNext()) {
                Map.Entry entry1 = iter1.next();
                Integer key1 = (Integer) entry1.getKey();
                HashMap<Integer, Double> value1 = (HashMap<Integer, Double>) entry1.getValue();
                Iterator<HashMap.Entry<Integer, HashMap<Integer, Double>>> iter2 = B.entrySet().iterator();
                HashMap<Integer, Double> resRow = new HashMap<>();
                while (iter2.hasNext()) {
                    HashMap.Entry entry2 = iter2.next();
                    Integer key2 = (Integer) entry2.getKey();
                    HashMap<Integer, Double> value2 = (HashMap<Integer, Double>) entry2.getValue();
                    Iterator iterElement = value1.entrySet().iterator();
                    double resValue = 0;
                    while (iterElement.hasNext()) {
                        HashMap.Entry entryElement = (HashMap.Entry) iterElement.next();
                        Integer keyElement1 = (Integer) entryElement.getKey();
                        Double valueElement1 = (Double) entryElement.getValue();
                        if (value2.get(keyElement1) != null) {
                            double a = value2.get(keyElement1);
                            resValue = resValue + valueElement1 * a;
                        }
                    }
                    if (resValue != 0) {
                        resRow.put(key2, resValue);
                    }
                }
                if (resRow != null) {
                    result.put(key1, resRow);
                }
            }

        }
    }

    public SparseMatrix MatrixSTrans() {
        Iterator<Map.Entry<Integer, HashMap<Integer, Double>>> iter = map.entrySet().iterator();
        ConcurrentHashMap<Integer, HashMap<Integer, Double>> matrixTr = new ConcurrentHashMap<>();
        while (iter.hasNext()) {
            Map.Entry entry = iter.next();
            Integer keyRow = (Integer) entry.getKey();
            HashMap<Integer, HashMap<Integer, Double>> value = (HashMap<Integer, HashMap<Integer, Double>>) entry.getValue();
            Iterator iterRow = value.entrySet().iterator();
            while (iterRow.hasNext()) {
                HashMap<Integer, Double> RowTr;
                Map.Entry entryRow = (Map.Entry) iterRow.next();
                Integer keyElements = (Integer) entryRow.getKey();
                Double valueElements = (Double) entryRow.getValue();
                RowTr = matrixTr.get(keyElements);
                if (RowTr == null) {
                    RowTr = new HashMap<>();
                }
                RowTr.put(keyRow, valueElements);
                matrixTr.put(keyElements, RowTr);
            }

        }
        return new SparseMatrix(matrixTr, rows, cols);
    }

    public SparseMatrix mulSparseDence(DenseMatrix other) {
        SparseMatrix res = new SparseMatrix(rows, cols);
        other = other.MatrixSTrans();
        double[][] a = other.matrix;
        Iterator<Map.Entry<Integer, HashMap<Integer, Double>>> iter1 = this.map.entrySet().iterator();
        while (iter1.hasNext()) {
            Map.Entry entry1 = iter1.next();
            Integer key1 = (Integer) entry1.getKey();
            HashMap<Integer, Double> value1 = (HashMap<Integer, Double>) entry1.getValue();
            HashMap<Integer, Double> resRow = new HashMap<>();
            for (int i = 0; i < rows; i++) {
                double resValue = 0.0;
                Iterator iterElement = value1.entrySet().iterator();
                while (iterElement.hasNext()) {
                    Map.Entry entryElement = (Map.Entry) iterElement.next();
                    Integer keyElement = (Integer) entryElement.getKey();
                    Double valueElement = (Double) entryElement.getValue();
                    if (other.matrix[i][keyElement] != 0.0) {
                        resValue = resValue + valueElement * a[i][keyElement];
                    }
                }
                if (resValue != 0.0) {
                    resRow.put(i, resValue);
                }
            }
            if (resRow != null) {
                res.map.put(key1, resRow);
            }

        }

        return res;
    }


    public boolean equals(Object o) {
        boolean eq = true;
        if (o instanceof DenseMatrix) {
            DenseMatrix temp = (DenseMatrix)o;
            if (temp.matrix.length == rows && temp.matrix[0].length == cols) {
                for (int i = 0; i < rows; i++) {
                    if (map.containsKey(i)) {
                        for (int j = 0; j < cols; j++) {
                            if (map.get(i).containsKey(j)) {
                                if (map.get(i).get(j) != temp.matrix[i][j]) {
                                    eq = false;
                                }
                            } else {
                                if (temp.matrix[i][j] != 0) {
                                    eq = false;
                                }
                            }
                        }
                    } else {
                        for (int j = 0; j < cols; j++) {
                            if (temp.matrix[i][j] != 0) {
                                eq = false;
                            }
                        }
                    }
                }
            } else {
                eq = false;
            }
        } else if (o instanceof SparseMatrix) {
            SparseMatrix temp = (SparseMatrix) o;
            if (temp.cols == cols && temp.rows == rows) {
                for (int i = 0; i < rows; i++) {
                    if (map.containsKey(i) && temp.map.containsKey(i))  {
                        for (int j = 0; j < cols; j++) {
                            if (map.get(i).containsKey(j) && temp.map.get(i).containsKey(j)) {
                                if (map.get(i).get(j).doubleValue() != temp.map.get(i).get(j).doubleValue()) {
                                    eq = false;
                                }
                            } else if (map.get(i).containsKey(j) || temp.map.get(i).containsKey(j)) {
                                eq = false;
                            }
                        }
                    } else if (map.containsKey(i) || temp.map.containsKey(i)) {
                        eq = false;
                    }
                }
            } else {
                eq = false;
            }
        }
        return eq;
    }

    public void printMartix(String filename) {
        try {
            PrintWriter w = new PrintWriter(filename);
            for (int i = 0; i<rows; i++) {
                if (map.containsKey(i)) {
                    for (int j = 0; j<cols; j++) {
                        if (map.get(i).containsKey(j)) {
                            w.print(map.get(i).get(j));
                        } else {
                            w.print((double)0);
                        }
                    }
                } else {
                    for (int j = 0; j < cols; j++) {
                        w.print((double)0);
                    }
                    w.println();
                }
            }
            w.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}