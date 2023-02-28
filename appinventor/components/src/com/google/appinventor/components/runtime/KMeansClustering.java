package com.google.appinventor.components.runtime;

import java.util.ArrayList;
import java.util.List;

public class KMeansClustering {

    private int k;
    private int maxIterations;

    public KMeansClustering(int k, int maxIterations) {
        this.k = k;
        this.maxIterations = maxIterations;
    }

    public List<Integer> fit(double[][] data) {
        // Initialize centroids randomly
        double[][] centroids = new double[k][data[0].length];
        for (int i = 0; i < k; i++) {
            centroids[i] = data[(int) (Math.random() * data.length)];
        }

        List<Integer> clusterAssignments = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            clusterAssignments.add(-1);
        }

        // Run k-means clustering algorithm
        int iteration = 0;
        while (iteration < maxIterations) {
            // Assign data points to nearest centroid
            for (int i = 0; i < data.length; i++) {
                double[] point = data[i];
                double minDistance = Double.MAX_VALUE;
                int minCluster = -1;
                for (int j = 0; j < centroids.length; j++) {
                    double[] centroid = centroids[j];
                    double distance = euclideanDistance(point, centroid);
                    if (distance < minDistance) {
                        minDistance = distance;
                        minCluster = j;
                    }
                }
                clusterAssignments.set(i, minCluster);
            }

            // Update centroids to be the mean of assigned data points
            for (int i = 0; i < centroids.length; i++) {
                double[] sum = new double[data[0].length];
                int count = 0;
                for (int j = 0; j < data.length; j++) {
                    if (clusterAssignments.get(j) == i) {
                        sum = addVectors(sum, data[j]);
                        count++;
                    }
                }
                if (count > 0) {
                    centroids[i] = scalarMultiply(sum, 1.0 / count);
                }
            }

            iteration++;
        }

        // Calculate distances from each point to its assigned centroid
        double[] distances = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            double[] point = data[i];
            int cluster = clusterAssignments.get(i);
            double[] centroid = centroids[cluster];
            distances[i] = euclideanDistance(point, centroid);
        }

        // Calculate mean and standard deviation of distances
        double mean = 0.0;
        for (double distance : distances) {
            mean += distance;
        }
        mean /= distances.length;

        double stdev = 0.0;
        for (double distance : distances) {
            stdev += Math.pow(distance - mean, 2);
        }
        stdev = Math.sqrt(stdev / distances.length);

        // Identify anomalies as points with distances greater than mean + 2*stdev
        List<Integer> anomalies = new ArrayList<>();
        for (int i = 0; i < distances.length; i++) {
            if (distances[i] > mean + 2 * stdev) {
                anomalies.add(i);
            }
        }

        return anomalies;
    }

    private double euclideanDistance(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }
    private double[] addVectors(double[] vector1, double[] vector2) {
        double[] sum = new double[vector1.length];
        for (int i = 0; i < vector1.length; i++) {
            sum[i] = vector1[i] + vector2[i];
        }
        return sum;
    }
    private double[] scalarMultiply(double[] vector, double scalar) {
        double[] result = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = vector[i] * scalar;
        }
        return result;
    }


}
