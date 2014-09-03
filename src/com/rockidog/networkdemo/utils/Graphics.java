package com.rockidog.networkdemo.utils;

import android.graphics.PointF;

public class Graphics{
    public static float distance(PointF start, PointF end) {
        return (float) Math.sqrt(Math.pow(start.x - end.x, 2) + Math.pow(start.y - end.y, 2));
    }

    public static PointF borderPoint(PointF outer, PointF center, float radius) {
        float length = distance(center, outer);
        float borderX = center.x + (outer.x - center.x) * (radius / length);
        float borderY = center.y + (outer.y - center.y) * (radius / length);
        return new PointF(borderX, borderY);
    }

    public static class Vector {
        private float x;
        private float y;
        private PointF startPoint = null;
        private PointF endPoint = null;
        
        public Vector(PointF start, PointF end) {
            startPoint = start;
            endPoint = end;
            x = end.x - start.x;
            y = end.y - start.y;
        }
        
        public Vector(float startX, float startY, float endX, float endY) {
            startPoint = new PointF(startX, startY);
            endPoint = new PointF(endX, endY);
            x = endX - startX;
            y = endY - startY;
        }
        
        public Vector(float x, float y) {
            this.x = x;
            this.y = y;
        }
        
        public PointF getStartPoint() { return startPoint; }
        public PointF getEndPoint() { return endPoint; }
        public void setStartPoint(PointF start) { startPoint = start; }
        public void setEndPoint(PointF end) { endPoint = end; }
        
        public float dir() {
            float radian = (float) Math.atan2(endPoint.y - startPoint.y, endPoint.x - startPoint.x);
            radian = (float) (Math.PI / 2 + radian);
            if (0 > radian)
                radian += (Math.PI * 2);
            return radian;
        }
        
        public float mod() {
            return distance(startPoint, endPoint);
        }
        
        public Vector add(Vector other) {
            return new Vector(this.x + other.x, this.y + other.y);
        }
    }
}
