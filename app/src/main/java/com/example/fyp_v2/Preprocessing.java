package com.example.fyp_v2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.provider.ContactsContract;
import android.util.Log;

import org.opencv.android.Utils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Preprocessing {

    private static final String TAG = "MainActivity";

    public Bitmap rotate(Bitmap bitmap){
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        Mat cannyMat = new Mat();
        Bitmap finalImage;
        double maxValue = 0;
        int maxValueId = 0;

        Utils.bitmapToMat(bitmap, rgbMat);

        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(grayMat, cannyMat, 100, 200);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(cannyMat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        for(int i = 0; i < contours.size(); i++){
            double contourArea = Imgproc.contourArea(contours.get(i));

            if(maxValue < contourArea){
                maxValue = contourArea;
                maxValueId = i;
            }
        }

        MatOfPoint2f point2f = new MatOfPoint2f(contours.get(maxValueId).toArray());
        RotatedRect box = Imgproc.minAreaRect(point2f);
        Log.i("Rotated rect_angle", "" + box.angle);
        Log.i("Rotated rect_angle", "" + box.size);
        Point points[] = new Point[4];
        box.points(points);
        /*for(int i=0; i<4; ++i) {
            Imgproc.line(rgbMat, points[i], points[(i + 1) % 4], new Scalar(255, 255, 255));
            Log.i("x point[" + i + "]", Double.toString(points[i].x));
            Log.i("y point[" + i + "]", Double.toString(points[i].y));
        }*/

        Mat result = deskew(rgbMat, box.angle, box.size.height, box.size.width);


        Mat gray = new Mat();;
        Mat canny = new Mat();
        Mat hierac = new Mat();

        List<MatOfPoint> contours_B = new ArrayList<MatOfPoint>();
        Imgproc.cvtColor(result, gray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(gray, canny, 100, 200);
        Imgproc.findContours(canny, contours_B, hierac, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = -1;
        int maxAreaIdx = -1;

        /*MatOfPoint temp_contour = contours.get(0);
        MatOfPoint2f approxCurve = new MatOfPoint2f();

        for (int idx = 0;idx < contours.size(); idx++){
            temp_contour = contours.get(idx);
            double contourArea=Imgproc.contourArea(temp_contour);

            if (contourArea > maxArea){

                MatOfPoint2f new_mat = new MatOfPoint2f(temp_contour.toArray());
                int contourSize= (int) temp_contour.total();
                MatOfPoint2f approxCurve_temp = new MatOfPoint2f();

                Imgproc.approxPolyDP(new_mat,approxCurve_temp,contourSize*0.05,true);
                if (approxCurve_temp.total() == 4){
                    maxArea = contourArea;
                    maxAreaIdx = idx;
                    approxCurve = approxCurve_temp;
                }
            }
        }
        Log.i("maxAreaIdx", Integer.toString(maxAreaIdx));

        //Mat mIntermediateMat = Mat.zeros(mat.size(), CvType.CV_8UC3);
        for(int i = 0; i < contours.size(); i++){

            //Scalar color = new Scalar( getRandomUniformInt(100, 200), getRandomUniformInt(100,200), getRandomUniformInt(100,200) );
            //Imgproc.drawContours( mIntermediateMat, contours, i, new Scalar(0,0,0,0), 4, 8, hierarchy, 0, new Point() );

            double contourArea = Imgproc.contourArea(contours_B.get(i));

            if(maxValue < contourArea){
                maxValue = contourArea;
                maxValueId = i;
            }
        }
        Log.i("maxValueId", Integer.toString(maxValueId));

        double[] temp_double=approxCurve.get(0,0);
        Point point1=new Point(temp_double[0],temp_double[1]);

        temp_double=approxCurve.get(1,0);
        Point point2=new Point(temp_double[0],temp_double[1]);

        temp_double=approxCurve.get(2,0);
        Point point3=new Point(temp_double[0],temp_double[1]);
        temp_double=approxCurve.get(3,0);

        Point point4=new Point(temp_double[0],temp_double[1]);

        List<Point> source = new ArrayList<>();
        source.add(point1);
        source.add(point2);
        source.add(point3);
        source.add(point4);
        //对4个点进行排序
        Point centerPoint=new Point(0,0);//质心
        for (Point corner:source){
            centerPoint.x+=corner.x;
            centerPoint.y+=corner.y;
        }
        centerPoint.x=centerPoint.x/source.size();
        centerPoint.y=centerPoint.y/source.size();
        Point lefttop=new Point();
        Point righttop=new Point();
        Point leftbottom=new Point();
        Point rightbottom=new Point();
        for (int i=0;i<source.size();i++){
            if (source.get(i).x<centerPoint.x&&source.get(i).y<centerPoint.y){
                lefttop=source.get(i);
            }else if (source.get(i).x>centerPoint.x&&source.get(i).y<centerPoint.y){
                righttop=source.get(i);
            }else if (source.get(i).x<centerPoint.x&& source.get(i).y>centerPoint.y){
                leftbottom=source.get(i);
            }else if (source.get(i).x>centerPoint.x&&source.get(i).y>centerPoint.y){
                rightbottom=source.get(i);
            }
        }
        source.clear();
        source.add(lefttop);
        source.add(righttop);
        source.add(leftbottom);
        source.add(rightbottom);

        Log.i("Source", source.toString());
        Log.i("Source", Double.toString(source.size()));

        Mat startM = Converters.vector_Point2f_to_Mat(source);

        Log.i("startM", Double.toString(startM.cols()));
        Log.i("startM", Double.toString(startM.rows()));*/

        //Mat mIntermediateMat = Mat.zeros(mat.size(), CvType.CV_8UC3);


        /*for(int i = 0; i < contours_B.size(); i++){
            //Scalar color = new Scalar( getRandomUniformInt(100, 200), getRandomUniformInt(100,200), getRandomUniformInt(100,200) );
            //Imgproc.drawContours( mIntermediateMat, startM, i, new Scalar(0,0,0,0), 4, 8, hierarchy, 0, new Point() );

            double contourArea = Imgproc.contourArea(contours_B.get(i));

            if(maxArea < contourArea){
                maxArea = contourArea;
                maxAreaIdx = i;
            }
        }

        MatOfPoint2f point2f1 = new MatOfPoint2f(contours_B.get(maxAreaIdx).toArray());
        RotatedRect box_B = Imgproc.minAreaRect(point2f1);
        Log.i("Rotated rect_angle", "" + box_B.angle);
        Log.i("Rotated rect_angle", "" + box_B.size);
        Point points_B[] = new Point[4];
        Point x[] = new Point[1];
        box_B.points(points_B);
        x[0] = points_B[0];
        Log.i("point", "" + points_B[0]);
        Log.i("x", "" + x[0]);

        for(int i=0; i<4; ++i) {
            Imgproc.line(result, points_B[i], points_B[(i + 1) % 4], new Scalar(255, 255, 255));
            Log.i("x point[" + i + "]", Double.toString(points_B[i].x));
            Log.i("y point[" + i + "]", Double.toString(points_B[i].y));
            if (x[0].x > points_B[i].x) {
                if (x[0].y > points_B[i].y) {
                    x[0].x = points_B[i].x;
                    x[0].y = points_B[i].y;
                }
            }
        }
        Log.i("point", "" + x[0]);
        Log.i("width", "" + box_B.size.width);
        Log.i("height", "" + box_B.size.height);*/

        //Size size = new Size(box_B.size.width, box_B.size.height);
        //Imgproc.resize(mat, result, size, x[0].x, x[0].y);*/


        finalImage = Bitmap.createBitmap(rgbMat.cols(),rgbMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(result, finalImage);
        Log.i(TAG, "procSrc2Gray sucess...");

        /*Bitmap cutBitmap = Bitmap.createBitmap((int)box_B.size.width, (int)box_B.size.height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(cutBitmap);
        Rect desRect = new Rect(0, 0, (int)box_B.size.width, (int)box_B.size.height);
        Rect srcRect = new Rect((int)x[0].x, (int)x[0].y, (int)(box_B.size.width *1.5) , (int)(box_B.size.height*1.5));
        canvas.drawBitmap(finalImage, srcRect, desRect, null);*/

        return finalImage;
    }

    public Bitmap crop(Bitmap bitmap){
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        Mat cannyMat = new Mat();
        Bitmap finalImage;
        double maxValue = 0;
        int maxValueId = 0;

        Utils.bitmapToMat(bitmap, rgbMat);

        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(grayMat, cannyMat, 100, 200);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(cannyMat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        for(int i = 0; i < contours.size(); i++){
            double contourArea = Imgproc.contourArea(contours.get(i));

            if(maxValue < contourArea){
                maxValue = contourArea;
                maxValueId = i;
            }
        }

        MatOfPoint2f point2f = new MatOfPoint2f(contours.get(maxValueId).toArray());
        RotatedRect box = Imgproc.minAreaRect(point2f);
        Log.i("Rotated rect_angle", "" + box.angle);
        Log.i("Rotated rect_angle", "" + box.size);
        Point points[] = new Point[4];
        Point x[] = new Point[1];
        box.points(points);
        x[0] = points[0];
        for(int i=0; i<4; ++i) {
            Imgproc.line(rgbMat, points[i], points[(i + 1) % 4], new Scalar(255, 255, 255));
            Log.i("x point[" + i + "]", Double.toString(points[i].x));
            Log.i("y point[" + i + "]", Double.toString(points[i].y));
            if (x[0].x > points[i].x) {
                if (x[0].y > points[i].y) {
                    x[0].x = points[i].x;
                    x[0].y = points[i].y;
                }
            }
        }

        Log.i("point", "" + x[0]);
        Log.i("width", "" + box.size.width);
        Log.i("height", "" + box.size.height);

        finalImage = Bitmap.createBitmap(rgbMat.cols(),rgbMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgbMat, finalImage);
        Log.i(TAG, "procSrc2Gray sucess...");

        Bitmap cutBitmap = Bitmap.createBitmap((int)box.size.width, (int)box.size.height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(cutBitmap);
        Rect desRect = new Rect(0, 0, (int)box.size.width, (int)box.size.height);
        Rect srcRect = new Rect((int)x[0].x, (int)x[0].y, (int)(box.size.width *1.5) , (int)(box.size.height*1.5));
        canvas.drawBitmap(bitmap, srcRect, desRect, null);
        return cutBitmap;
    }

    private int getRandomUniformInt(int min, int max) {
        Random r1 = new Random();
        return r1.nextInt() * (max - min) + min;
    }

    public Mat deskew(Mat src, double angle, double height, double width) {
        Point center = new Point(src.width()/2, src.height()/2);

        if(width > height){
            angle = 90 + angle;
        }

        Mat rotImage = Imgproc.getRotationMatrix2D(center, angle, 1.0);
        //1.0 means 100 % scale
        Size size = new Size(src.width(), src.height());
        Imgproc.warpAffine(src, src, rotImage, size, Imgproc.INTER_NEAREST);
        return src;
    }

    public Mat warp(Mat inputMat,Mat startM) {
        int resultWidth = 1000;
        int resultHeight = 1000;

        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);



        Point ocvPOut1 = new Point(0, 0);
        Point ocvPOut2 = new Point(0, resultHeight);
        Point ocvPOut3 = new Point(resultWidth, resultHeight);
        Point ocvPOut4 = new Point(resultWidth, 0);
        List<Point> dest = new ArrayList<Point>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);
        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat,
                outputMat,
                perspectiveTransform,
                new Size(resultWidth, resultHeight),
                Imgproc.INTER_CUBIC);

        return outputMat;
    }

    public Bitmap otsu(Bitmap bitmap){
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        Mat otsuMat = new Mat();
        Bitmap finalImage;

        Utils.bitmapToMat(bitmap, rgbMat);

        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        //Imgproc.threshold(grayMat, otsuMat, 0, 255, Imgproc.THRESH_OTSU | Imgproc.THRESH_BINARY);

        finalImage = Bitmap.createBitmap(rgbMat.cols(),rgbMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(grayMat, finalImage);
        Log.i(TAG, "procSrc2Gray sucess...");

        return finalImage;
    }


}

        /*Mat correctedImage = new Mat(rgbMat.rows(),rgbMat.cols(), rgbMat.type());

        Mat srcPoint = Converters.vector_Point2f_to_Mat(Arrays.asList(points));
        Mat destPoint = Converters.vector_Point2f_to_Mat(Arrays.asList(new Point[]{
                new Point(0, correctedImage.rows()),
                new Point(0, 0),
                new Point(correctedImage.cols(),0),
                new Point(correctedImage.cols(), correctedImage.rows())
        }));
        Log.i("src poit", "" + srcPoint);
        Log.i("dest point", "" + destPoint);
        Mat transformation = Imgproc.getPerspectiveTransform(srcPoint, destPoint);
        Imgproc.warpPerspective(rgbMat, correctedImage, transformation, correctedImage.size());*/