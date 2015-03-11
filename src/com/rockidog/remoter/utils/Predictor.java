package com.rockidog.remoter.utils;

import java.util.LinkedList;

public class Predictor {
  private int mSize;
  private LinkedList<Double> mXList;
  private LinkedList<Double> mYList;
  private double mPredictX;
  private double mPredictY;

  public Predictor(int size) {
    mSize = size;
    mXList = new LinkedList<Double>();
    mYList = new LinkedList<Double>();
    mPredictX = 0;
    mPredictY = 0;
    for (int i = 0; i != mSize; ++i) {
      mXList.add(0.0);
      mYList.add(0.0);
    }
  }

  public int getSize() { return mSize; }
  public double getX() { return mPredictX; }
  public double getY() { return mPredictY; }

  public void doUpdate(double x, double y) {
    mPredictX = x;
    mPredictY = y;
    update();
  }

  public void doPredict() {
    predict();
    update();
  }

  private void predict() {
    mPredictX = doLeastSquares(mXList);
    mPredictY = doLeastSquares(mYList);
  }

  private void update() {
    mXList.addLast(mPredictX);
    mYList.addLast(mPredictY);
    mXList.removeFirst();
    mYList.removeFirst();
  }

  private double doLeastSquares(LinkedList<Double> dataList) {
    double numerator = 0;
    double denominator = 0;
    double dataAvg = 0;
    double tAvg = 0;
    
    // Use the method of least squares to predict the lost cycle
    double i = 0;
    for (double item: dataList) {
      dataAvg += item;
      numerator += (i * item);
      denominator += (i * i);
      ++i;
    }
    dataAvg /= mSize;
    tAvg = (mSize - 1) / 2.0;
    numerator -= (mSize * tAvg * dataAvg);
    denominator -= (mSize * tAvg * tAvg);
    
    double b1 = numerator / denominator;
    double b0 = dataAvg - b1 * tAvg;
    return b0 + b1 * mSize;
  }

  public static void main(String[] args) {
    Predictor predictor = new Predictor(60);
    for (int i = 0; i != 30; ++i)
      predictor.doUpdate(i, i);
    for (double item: predictor.mXList)
      System.out.print(item + " ");
    System.out.println();
    predictor.doPredict();
    for (double item: predictor.mXList)
      System.out.print(item + " ");
    System.out.println();
  }
}
