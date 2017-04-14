package com.intsig.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by jesse on 17-4-10.
 */

public class PieChart extends View {
    ArrayList<PieChart.Entry> mDataSet = new ArrayList<>();
    public PieChart(Context context) {
        super(context);
        init();
    }

    public PieChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PieChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setData(ArrayList<Entry> data,String centerText){
        mDataSet.clear();
        if(data!=null) {
            mDataSet.addAll(data);
        }
        mCenterText= centerText;
        preCalcute();
        this.requestLayout();
    }
    public void setData(String[] labels, int values[],String centerText){
        mDataSet.clear();
        for(int i=0;i<labels.length;i++) {
            mDataSet.add(new Entry(labels[i], values[i]));
        }
        mCenterText= centerText;
        preCalcute();
        this.requestLayout();
    }
    // 计算角度，颜色
    private void preCalcute(){
        int count =0;
        for(Entry e:mDataSet){
            count+=e.value;
        }
        mCount =  count;

        //计算 颜色（最大->最小 按COLORS数字依次分配，所以先排序
        ArrayList<PieChart.Entry> tmp = new ArrayList<>(mDataSet.size());
        tmp.addAll(mDataSet);
        Collections.sort(tmp);

        //计算角度,和颜色
        float arrearage = 0;
        //从小到大依次计算角度，小的不够2度，就借下一个的
        int size = tmp.size();
        for(int i=size-1;i>=0;i--){
            Entry e= tmp.get(i);
            e.color= COLORS[i];
            e.angle = (360.0f*e.value)/count +arrearage;
            // 角度太小，就画不出来了,所以设置最小角度为2，把多占用的让下一个承担
            if(e.angle<2f) {
                arrearage = e.angle-2f;
                e.angle = 2;
            }else{
                arrearage=0;
            }
        }


        if(mSortData){
            Collections.sort(mDataSet);
        }

    }

    //是否需要对数据进行排序
    boolean mSortData;
    public void setSort(boolean sort){
        mSortData = sort;
    }

    //预制颜色，从大到小
    final int COLORS[] = {0xFF00A7CF,0xFF8E7BE6,0xFF0179B1, 0xFF73AC1A,0xFFF5B910,0xFFA5BBD1,
            0xFF00A7CF,0xFF8E7BE6,0xFF0179B1, 0xFF73AC1A,0xFFF5B910,0xFFA5BBD1, };

    //中心的文字“总量”多语言由外面传入
    String mCenterText ;
    //总量
    int mCount;

    private void init(){

    }

    // 上下padding
    float padding = convertDpToPixel(10);
    // 大圆的半径
    float radius = convertDpToPixel(65);
    // 圆环的宽度
    float border = radius/4;
    // 小圆半径
    float sradius =convertDpToPixel(15);

    //大圆心 数字大小
    float textSizeBigCount = convertDpToPixel(18);
    //大圆心 文字大小
    float textSizeBigCircle2 = convertDpToPixel(20);
    // label文字大小
    float textSizeLabel= convertDpToPixel(14);
    // label数字字大小
    float textSizeSmallCount= convertDpToPixel(12);
    //线条的宽度
    float lineStrokeWidth= convertDpToPixel(1);
    //阴影的半径
    float shadownRadisu = convertDpToPixel(2);
    //小圆的间距
    float spaceSmallCircle = convertDpToPixel(53);

    Paint paint = new Paint();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //背景涂成白色
        canvas.drawColor(0xffffffff);
        int width = getWidth();
        int height = getHeight();

        //计算圆心，
        float cx = width/2;
        float cy = radius+padding;



        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        float strokeWidth = paint.getStrokeWidth();

        //绘制圆环的阴影
        setLayerType(LAYER_TYPE_SOFTWARE,paint);
        paint.setShadowLayer(shadownRadisu,0,shadownRadisu,0xffAAAAAA);
        paint.setStrokeWidth(border+shadownRadisu/2);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(cx,cy,radius-border/2, paint);
        //取消阴影
        paint.clearShadowLayer();
        //回复线框宽度
        paint.setStyle(Paint.Style.FILL);

        //如果2个数据，绘制线条， label
        if(mDataSet.size()==2){
            //绘制线条
            float ex=0,ey=0;
            // 椭圆方程 (x/a)^2+(y/b）^2=1
            float b=radius+border/4;
            float a = radius+border;
         /*  paint.setColor(Color.RED);
            for(float x=-a;x<a;x++){
                float y =(float) Math.sqrt(( 1 - x*x/(a*a))*b*b);
                canvas.drawPoint(cx+x,cy+y,paint);
                canvas.drawPoint(cx+x,cy-y,paint);
            }*/
            paint.setStrokeWidth(lineStrokeWidth);


            //绘制 线条和文字label
            float tmp=0;
            for(int i=0;i<mDataSet.size();i++) {
                Entry e = mDataSet.get(i);
                int sg =1;
                if(tmp+e.angle/2>180) {
                    sg = -1;
                    paint.setTextAlign(Paint.Align.RIGHT);
                }else{
                    paint.setTextAlign(Paint.Align.LEFT);
                }
                float angle = e.angle;
                if(angle>180){
                    angle= 240;
                }else{
                    if(angle > 120)
                        angle = 120;
                }
                angle = (float) ((angle / 2) * Math.PI / 180);
                float k2 = (float) (1 / Math.tan(angle));
                ex = (float) Math.sqrt(1 / (1 / (a * a) + (k2 * k2) / (b * b)));
                ey = k2 * ex;
                float sx =cx + sg*(float)(radius*Math.sin(angle));
                float sy =cy -(float)(radius*Math.cos(angle));
                float linepad = radius + border*2;
                paint.setColor(e.color);
                canvas.drawLine(sx, sy, cx +sg* ex, cy - ey, paint);
                canvas.drawLine(cx +sg* ex, cy - ey, cx +sg*(linepad), cy - ey, paint);
                paint.setColor(0xff5f5f5f);
                paint.setTextSize(textSizeLabel);
                canvas.drawText(e.label, cx +sg*(linepad)   +sg*textSizeLabel / 2, cy - ey + textSizeLabel * 1 / 3, paint);
                tmp+=e.angle;
            }
        }

        //再绘制白色圆环，把外边缘的白色漏出来
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(border+shadownRadisu/2);
        canvas.drawCircle(cx,cy,radius-border/2, paint);

        paint.setStrokeWidth(strokeWidth);

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.save();
        //剪切内小圆，再绘制扇形，就成了扇形圆环
        Path path = new Path();
        path.addCircle(cx,cy,radius-border, Path.Direction.CCW);
        canvas.clipPath(path, Region.Op.DIFFERENCE);
        float startAngle = -90f;
        RectF arcRect =new RectF(cx-radius,cy-radius,cx+radius,cy+radius);
        for(int i=0;i<mDataSet.size();i++){//绘制扇形圆环
            Entry e=mDataSet.get(i);
            paint.setColor(e.color);
            canvas.drawArc(arcRect,startAngle,e.angle==360?e.angle:(e.angle-1),true,paint);
            startAngle+=e.angle;
        }
        canvas.restore();
        //绘制大圆中心文字数字
        if(!TextUtils.isEmpty(mCenterText)) {
            paint.setColor(Color.BLACK);

            paint.setTextSize(textSizeBigCount);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(""+mCount, cx, cy, paint);
            paint.setTextSize(textSizeBigCircle2);
            String text = mCenterText;
            if(mDataSet.size()==1)
                text = mDataSet.get(0  ).label;
            canvas.drawText(text, cx, cy+textSizeBigCircle2, paint);
        }
        // 底部绘制 小圆圈，及label
        if(mDataSet.size()>2){
            float scx,scy;
            float sdif =spaceSmallCircle;
            scy = padding+radius*2+padding+sradius;
            float sw =(width-mDataSet.size()*sdif)/2;
            scx=sw+sdif/2;

            for(int i=0;i<mDataSet.size();i++){
                Entry e=mDataSet.get(i);
                paint.setColor(e.color);
                canvas.drawCircle(scx,scy,sradius, paint);
                paint.setColor(Color.WHITE);
                paint.setTextSize(textSizeSmallCount);
                canvas.drawText(""+e.value,scx,scy+textSizeSmallCount*1/3,paint);
                paint.setTextSize(textSizeLabel);
                paint.setColor(0xFF5F5F5F);
                canvas.drawText(""+e.label,scx,scy+sradius*2,paint);
                scx+=sdif;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec) ;
        int minheight = (int) ( padding+radius*2+padding);
        if(mDataSet.size()>2){
            //加上小面小圆和文字的高度，粗略使用4倍小圆半径
            minheight+=(sradius*4);
        }

        setMeasuredDimension(
                Math.max(getSuggestedMinimumWidth(),
                        resolveSize(width,
                                widthMeasureSpec)),
                Math.max(getSuggestedMinimumHeight(),
                        resolveSize(minheight,
                                heightMeasureSpec)));
    }

    float convertDpToPixel(float dp) {
        return getResources().getDisplayMetrics().density * dp;

    }

    public static class Entry implements Comparable{
        public String label;
        public int value;
        public float angle;
        public int color;

        public Entry(String label, int value) {
            this.label = label;
            this.value = value;
        }



        @Override
        public int compareTo(@NonNull Object o) {
            Entry e = (Entry)o;
            if(value>e.value)
                return -1;
            if(value<e.value)
                return 1;
            return 0;
        }
    }
}
