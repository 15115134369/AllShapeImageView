package com.meijia.cxh.utils.anyshapeutil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.meijia.cxh.R;

/**
 * Created by taofangxin on 16/3/21.
 */
@SuppressLint("AppCompatCustomView")
public class AnyshapeImageView extends ImageView {

    private Context context;
    private Path originMaskPath = null;
    private  int originMaskWidth = 0;
    private  int originMaskHeight = 0;
    private Path realMaskPath = new Path();
    private Bitmap shapeLayerBitmap ;
    private Bitmap showLayerBitmap ;
    private Bitmap resouseBitmap;
    private PorterDuffXfermode xfermode;
    private Canvas SystemCanvas;
    // 实例化画笔并打开抗锯齿
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int maskResId = R.mipmap.na2;
    private  int mWidth = 0;
    private int mHeight = 0;
    private Bitmap bitmap;
    private int lastResId = 0;
    private boolean isReplayRes = true;
    private Drawable shapeLayerDrawable;

    public void setMaskResId(int maskResId) {
        this. maskResId = maskResId;
        if (mWidth != 0 && mHeight != 0) {
            if (maskResId <= 0) {
                return;
            }
//            bitmap = createImage();
            invalidate();

            PathInfo pi = PathManager.getInstance().getPathInfo(maskResId);
            if (null != pi) {
                originMaskPath = pi.path;
                originMaskWidth = pi.width;
                originMaskHeight = pi.height;
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(context.getResources(), maskResId, options);
                int widthRatio = (int)(options.outWidth * 1f / mWidth);
                int heightRatio = (int)(options.outHeight * 1f / mHeight);
                if (widthRatio > heightRatio) {
                    options.inSampleSize = widthRatio;
                } else {
                    options.inSampleSize = heightRatio;
                }
                if (options.inSampleSize == 0) {
                    options.inSampleSize = 1;
                }
                options.inJustDecodeBounds = false;
                Bitmap maskBitmap = BitmapFactory.decodeResource(context.getResources(), maskResId, options);
                originMaskPath = PathManager.getInstance().getPathFromBitmap(maskBitmap);
                originMaskWidth = maskBitmap.getWidth();
                originMaskHeight = maskBitmap.getHeight();
                pi = new PathInfo();
                pi.height = originMaskHeight;
                pi.width = originMaskWidth;
                pi.path = originMaskPath;
                PathManager.getInstance().addPathInfo(maskResId, pi);

                shapeLayerBitmap = maskBitmap;
                showLayerBitmap = getBitmapFromDrawable(getDrawable());
//                bitmap = createImage();
            }
        }
        if (originMaskPath != null) {
            //scale the size of the path to fit the one of this View
            Matrix matrix = new Matrix();
            matrix.setScale(vWidth * 1f / originMaskWidth, vHeight * 1f / originMaskHeight);
            originMaskPath.transform(matrix, realMaskPath);
        }
    }
    public void setMaskColor(int maskColor) {
        this. backColor = maskColor;
        if (mWidth != 0 && mHeight != 0) {
            if (backColor <= 0) {
                return;
            }
            PathInfo pi = PathManager.getInstance().getPathInfo(maskResId);
            if (null != pi) {
                originMaskPath = pi.path;
                originMaskWidth = pi.width;
                originMaskHeight = pi.height;
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(context.getResources(), maskResId, options);
                int widthRatio = (int)(options.outWidth * 1f / mWidth);
                int heightRatio = (int)(options.outHeight *1f / mHeight);
                if (widthRatio > heightRatio) {
                    options.inSampleSize = widthRatio;
                } else {
                    options.inSampleSize = heightRatio;
                }
                if (options.inSampleSize == 0) {
                    options.inSampleSize = 1;
                }
                options.inJustDecodeBounds = false;
                Bitmap maskBitmap = BitmapFactory.decodeResource(context.getResources(), maskResId, options);
                originMaskPath = PathManager.getInstance().getPathFromBitmap(maskBitmap);
                originMaskWidth = maskBitmap.getWidth();
                originMaskHeight = maskBitmap.getHeight();
                pi = new PathInfo();
                pi.height = originMaskHeight;
                pi.width = originMaskWidth;
                pi.path = originMaskPath;
                PathManager.getInstance().addPathInfo(maskResId, pi);
                maskBitmap.recycle();
            }
        }
        if (originMaskPath != null) {
            //scale the size of the path to fit the one of this View
            Matrix matrix = new Matrix();
            matrix.setScale(vWidth * 1f / originMaskWidth, vHeight * 1.0f / originMaskHeight);
            originMaskPath.transform(matrix, realMaskPath);
        }
    }

    public int getMaskResId() {
        return maskResId;
    }

    int backColor;
    int vWidth = 0;
    int vHeight = 0;
    public AnyshapeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setWillNotDraw(false);
        xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AnyShapeImageView, defStyleAttr, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++)
        {
            final int attr = a.getIndex(i);
            if (attr == R.styleable.AnyShapeImageView_anyshapeMask) {
                maskResId = a.getResourceId(attr, 0);
                if (0 == maskResId) {
                    //did not set mask
                    continue;
                }
                shapeLayerDrawable = a.getDrawable(R.styleable.AnyShapeImageView_anyshapeMask);
                shapeLayerBitmap = getBitmapFromDrawable(shapeLayerDrawable);
                showLayerBitmap = getBitmapFromDrawable(getDrawable());
                resouseBitmap = getBitmapFromDrawable(getDrawable());

            } else if (attr == R.styleable.AnyShapeImageView_anyshapeBackColor) {
                backColor = a.getColor(attr, Color.TRANSPARENT);
            }
        }
        a.recycle();
    }

    public AnyshapeImageView(Context context) {
        this(context, null);
        setWillNotDraw(false);
    }

    public AnyshapeImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        vHeight = getHeight();
        vWidth = getWidth();
//        if (originMaskPath != null) {
//            //scale the size of the path to fit the one of this View
//            Matrix matrix = new Matrix();
//            matrix.setScale(vWidth * 1f / originMaskWidth, vHeight * 1.0f / originMaskHeight);
//            originMaskPath.transform(matrix, realMaskPath);
//        }
//        bitmap = createImage();
    }

    // 利用PorterDuff.Mode的 SRC_IN 或 DST_IN 取形状图层和显示图层交集，从而得到自定义形状的图片
    private Bitmap createImage() {
        if(shapeLayerBitmap==null){
            return null;
        }
        if(showLayerBitmap==null){
            return null;
        }
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
//         shapeLayerBitmap = getBitmapFromDrawable(shapeLayerDrawable);
        shapeLayerBitmap = BitmapFactory.decodeResource(context.getResources(),maskResId,options);
//         showLayerBitmap = getBitmapFromDrawable(getDrawable());
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setXfermode(null);

        if (mWidth==0||mHeight==0) {
            return null;
        }
        Bitmap result = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
//        Bitmap finalBmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        RectF rectf=new RectF(0,0,mWidth,mHeight);

        canvas.saveLayer(rectf, null, Canvas.ALL_SAVE_FLAG);
        if (null != showLayerBitmap&&mWidth!=0&&mHeight!=0) {
            showLayerBitmap = getCenterCropBitmap(showLayerBitmap, mWidth, mHeight);

            canvas.drawBitmap(showLayerBitmap, 0, 0, paint);
        }
        if (null != shapeLayerBitmap&&mWidth!=0&&mHeight!=0) {
//            shapeLayerBitmap = getCenterInsideBitmap(shapeLayerBitmap, mWidth, mHeight);
            shapeLayerBitmap = getFillXYBitmap(shapeLayerBitmap, mWidth, mHeight);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            canvas.drawBitmap(shapeLayerBitmap, 0, 0, paint);
        }
        canvas.restore();

        return result;
    }
    /**
     * Drawable转Bitmap
     */
    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    /**
     * 类比ScaleType.FILL_XY
     */
    private Bitmap getFillXYBitmap(Bitmap src, float rectWidth, float rectHeight) {

        float srcRatio = ((float) src.getWidth()) / src.getHeight();
        float rectRadio = rectWidth / rectHeight;
        return getFILE_XYBitmap(src, rectWidth,rectHeight);
    }
    /**
     * 类比ScaleType.FILE_XY
     */
    private Bitmap getFILE_XYBitmap(Bitmap src, float viewWidth, float viewHeight) {
        float srcWidth = src.getWidth();
        float srcHeight = src.getHeight();
        float scaleWidth = 0;
        float scaleHeight = 0;

//        if (srcWidth > srcHeight) {
//            scaleWidth = sideLength;
//            scaleHeight = (sideLength / srcWidth) * srcHeight;
//        } else if (srcWidth < srcHeight) {
//            scaleWidth = (sideLength / srcHeight) * srcWidth;
//            scaleHeight = sideLength;
//        } else {
//            scaleWidth = scaleHeight = sideLength;
//        }

        return Bitmap.createScaledBitmap(src, (int) viewWidth, (int) viewHeight, false);
    }

    /**
     * 类比ScaleType.CENTER_INSIDE
     */
    private Bitmap getCenterInsideBitmap(Bitmap src, float sideLength) {
        float srcWidth = src.getWidth();
        float srcHeight = src.getHeight();
        float scaleWidth = 0;
        float scaleHeight = 0;

        if (srcWidth > srcHeight) {
            scaleWidth = sideLength;
            scaleHeight = (sideLength / srcWidth) * srcHeight;
        } else if (srcWidth < srcHeight) {
            scaleWidth = (sideLength / srcHeight) * srcWidth;
            scaleHeight = sideLength;
        } else {
            scaleWidth = scaleHeight = sideLength;
        }

        return Bitmap.createScaledBitmap(src, (int) scaleWidth, (int) scaleHeight, false);
    }

    /**
     * 类比ScaleType.CENTER_INSIDE
     */
    private Bitmap getCenterInsideBitmap(Bitmap src, float rectWidth, float rectHeight) {

        float srcRatio = ((float) src.getWidth()) / src.getHeight();
        float rectRadio = rectWidth / rectHeight;
        if (srcRatio < rectRadio) {
            return getCenterInsideBitmap(src, rectHeight);
        } else {
            return getCenterInsideBitmap(src, rectWidth);
        }
    }

    /**
     * 类比ScaleType.CENTER_CROP
     */
    private Bitmap getCenterCropBitmap(Bitmap src, float rectWidth, float rectHeight) {

        float srcRatio = ((float) src.getWidth()) / src.getHeight();
        float rectRadio = rectWidth / rectHeight;
        if (srcRatio < rectRadio) {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(src, (int) rectWidth, (int) ((rectWidth / src.getWidth()) * src.getHeight()), false);
            return Bitmap.createBitmap(scaledBitmap, 0, (int) ((scaledBitmap.getHeight() - rectHeight) / 2), (int) rectWidth, (int) rectHeight);
        } else {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(src, (int) ((rectHeight / src.getHeight()) * src.getWidth()), (int) rectHeight, false);
            return Bitmap.createBitmap(scaledBitmap, (int) ((scaledBitmap.getWidth() - rectWidth) / 2), 0, (int) rectWidth, (int) rectHeight);
        }
    }


    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        if( lastResId!=0){
            if( lastResId == resId){
                isReplayRes = false ;
            }else {
                isReplayRes = true ;
            }
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
         shapeLayerBitmap = getBitmapFromDrawable(shapeLayerDrawable);
        showLayerBitmap = getCompressBitmap(resId);
        resouseBitmap = showLayerBitmap;
//        showLayerBitmap = BitmapFactory.decodeResource(context.getResources(),resId,options);
//        resouseBitmap = BitmapFactory.decodeResource(context.getResources(),resId,options);
//        bitmap = createImage();
        lastResId = resId;
        postInvalidate();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        this.showLayerBitmap = bm;
        isReplayRes =false;
        this.resouseBitmap = bm;
//        bitmap = createImage();
    }

    @SuppressLint("WrongConstant")
    private void drawCoustm(Canvas canvas) {
        if(showLayerBitmap == null){
            return;
        }
        if(bitmap == null){
            return;
        }
        if(shapeLayerBitmap == null){
            return;
        }
//        canvas.drawBitmap(showLayerBitmap, 0, 0, paint);
        int saveFlags = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG;
        canvas.saveLayer(0, 0, mWidth, mHeight, null, saveFlags);
        canvas.drawBitmap(shapeLayerBitmap, 0, 0, paint);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setMaskFilter(new BlurMaskFilter(1f, BlurMaskFilter.Blur.NORMAL));
        paint.setXfermode(xfermode);
        canvas.drawBitmap(bitmap,0,0, paint);
        paint.setXfermode(null);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        if (mWidth != 0 && mHeight != 0) {
            if (maskResId <= 0) {
                return;
            }
            PathInfo pi = PathManager.getInstance().getPathInfo(maskResId);
            if (null != pi) {
                originMaskPath = pi.path;
                originMaskWidth = pi.width;
                originMaskHeight = pi.height;
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(context.getResources(), maskResId, options);
                int widthRatio = (int)(options.outWidth * 1f / mWidth);
                int heightRatio = (int)(options.outHeight * 1f / mHeight);
                if (widthRatio > heightRatio) {
                    options.inSampleSize = widthRatio;
                } else {
                    options.inSampleSize = heightRatio;
                }
                if (options.inSampleSize == 0) {
                    options.inSampleSize = 1;
                }
                options.inJustDecodeBounds = false;
                Bitmap maskBitmap = BitmapFactory.decodeResource(context.getResources(), maskResId, options);
                originMaskPath = PathManager.getInstance().getPathFromBitmap(maskBitmap);
                originMaskWidth = maskBitmap.getWidth();
                originMaskHeight = maskBitmap.getHeight();
                pi = new PathInfo();
                pi.height = originMaskHeight;
                pi.width = originMaskWidth;
                pi.path = originMaskPath;
                PathManager.getInstance().addPathInfo(maskResId, pi);
                maskBitmap.recycle();
            }
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onDraw(Canvas canvas) {
        SystemCanvas = canvas;
        if(showLayerBitmap == null){
            return;
        }
//        if(bitmap == null){
//            return;
//        }
        if(shapeLayerBitmap == null){
            return;
        }
        if(isReplayRes){
//            Log.d("没有更换图片id","没有更换图片id");   //视为拉长高度时的情况
            showLayerBitmap = resouseBitmap ;
            bitmap = createImage();

    //        canvas.drawBitmap(showLayerBitmap, 0, 0, paint);
            int saveFlags = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG;
            canvas.saveLayer(0, 0, mWidth, mHeight, null, saveFlags);
//        canvas.drawBitmap(shapeLayerBitmap, 0, 0, paint);
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setMaskFilter(new BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL));
//        paint.setXfermode(xfermode);
            if (bitmap!=null){
                canvas.drawBitmap(bitmap,0,0, paint);
            }
            paint.setXfermode(null);
//        super.onDraw(canvas);
            canvas.restore();
        }else {
            bitmap = createImage();
            int saveFlags = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG;
            canvas.saveLayer(0, 0, mWidth, mHeight, null, saveFlags);
    //      canvas.drawBitmap(shapeLayerBitmap, 0, 0, paint);
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setMaskFilter(new BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL));
    //      paint.setXfermode(xfermode);
            if (bitmap!=null){
                canvas.drawBitmap(bitmap,0,0, paint);
            }
            paint.setXfermode(null);
//          super.onDraw(canvas);
            canvas.restore();
        }
    }

    /**
     * allow coder to set the backColor
     * @param color
     */
    public void setBackColor(int color) {
        backColor = color;
        postInvalidate();
    }

    private int dp2px(float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    //压缩
    public Bitmap getCompressBitmap(int resid) {
        BitmapFactory.Options opts = new BitmapFactory.Options();     //new出对象
        opts.inJustDecodeBounds = true;                            //设置为true只返回宽 、高

        BitmapFactory.decodeResource(context.getResources(),resid,opts);     //设置出资源
        int w,h;
        w = opts.outWidth;          //获得到宽、高
        h = opts.outHeight;
        if (w>h){
            opts.inSampleSize = w/200;     //如果是横图 就以宽为比例设置inSampleSize
        }else {
            opts.inSampleSize = h/200;     //同上咯
        }

        opts.inJustDecodeBounds = false;  //记得还是要设置会false这样才能得到图，不然返回的还是宽高
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(),resid,opts); //这样就获取到了图像咯

        return bm;
    }
}
