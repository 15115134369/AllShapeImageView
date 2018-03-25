# WJJ-CodeStore
自定义一个可以实现动态遮罩出任何图形的imageview控件


      自定义一个可以实现动态遮罩出任何图形的imageview控件

         公司项目需求是类似于美甲类app的开发，用户选择了不同的甲型图像后，要把界面上存放手指的imageview控件变换为用户选择好的形状，这个需求当时感觉挺头大的，之前确实没有坐过类似这样的自定义控件，于是Goole搜索了一番，最后浏览了几乎所有能搜到的相关博客文章后，找到了下面这一篇文章，看题目似乎正好能满足我的需求，此处附上博客链接（https://blog.csdn.net/u013015161/article/details/50993199），感兴趣的小伙伴可以学习研究一下。

        博客里面讲的确实挺好的，整个自定义的过程，步骤都非常清晰。正好博客里面有demo可以下载，于是下载了一个进行试用，运行demo后感觉挺好的，正在高兴之余发现问题来了，这个自定义控件使用必须将想要遮罩的图片的drawable以resid的形式静态放到xml布局中实现，并不灵活，没法满足我这样的在用户灵活操作下改变各种形状的需求，然而这已经是找寻了好久才找到的解决方案，没有更好的解决方案了，怎么办？

        别总想吃现成的了，自己动脑筋自定义吧，于是在原先博主的思路基础上寻找进行再次开发的思路，博主原来的方法是用自定义view的三个参数的构造函数里面将自定义形状图片的资源id传递进去，并在onmeasure方法的时候根据图片资源id生成bitmap获取对应的path对象并记录下来，我要想动态能够设置这个imageview的遮罩形状有两种途径，一种是图片的资源id，另一种是直接给一个图片的bitmap对象过来，都能达到遮罩出给的图片形状的效果才行，那就好办了，我们平时使用imageview的时候都是用Android系统开放给我们的setImageresouse（）方法和setImageBitamp()方法实现动态就能在代码里面改变imageview控件显示对象的需求，那我直接重写imageView的这两个方法不就行了吗，这样就可以动态的改变遮罩图像的资源了，无论id还是bitamp对象都可以，说干就干，自定义尝试后真的成功了，下面附上这两个方法的代码：

    
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
        showLayerBitmap = getYsSuoBitmap(resId);
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
这样动态设置遮罩图片资源的问题解决了，但是紧接着又有一个新的问题接踵而来，发现当遮罩比较大的图片的时候会出现顶部和底部显示不全的情况，小的时候不怎么明显，可是我们的项目偏偏有一个手指微调的功能，当测试妹子点击微调的时候马上大叫了起来，这个没有显示全怎么，而且中间有条纹，我看了看确实是有，好吧，又遇上烧脑的问题了，这到底是什么原因造成的呢，请教了很多高人后，一个经验比较多的杭州的前端哥们儿跟我说，有可能是因为这个遮罩的方案是通过读取bitmap对象的path并记录然后绘制出来的，刚开始的时候对宽高进行了缩放，所以不太精确了，建议我用apth.setStrokeWidth方法将线宽设置的细一些再试试看，于是我这样尝试了下，也又些效果，但是还是不能非常圆润的将指甲图片的顶部和底部部分都绘制的非常圆，依然是平的，图像中间还是会有些难看的白色线条出现，并没有根本上解决问题，看着ios哥们儿做好的应用遮罩效果非常好看，再看看自己做的，不禁有些失落，坚持尝试了好几天依然没有能解决问题，老板时不时过来看做的怎么样了，每当看到这里的时候都提我这个地方不完美，弄的我都有些尴尬了，好吧，拼命也要把它给实现出来，也许就是为了自己一个Android开发者的尊严。想法是好的，也就精神拼搏了，但是思路在哪里？换什么方案把当前的问题给解决掉，苦思冥想一番后，决定换遮罩绘制过程中的方案，不用读取和绘制path方案了，在Goole上搜罗了一大圈有了思路感觉，Android是自带图像遮罩的API的，但是之前没有尝试过，这一次也许可以解决这个头疼的问题，看这里的一段代码：

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
我贴上去的是一个可以直接使用的方法，其实我刚开始用的时候，这个方法总是出问题，调试了好几次后才能正常使用，深深感到程序员搬砖不易，调bug不易啊！！！这样一来就实现了完美的遮罩出任何你想要的形状的自定义的imageview控件了，附上几个效果图：











      

本以为一切问题都解决了，可以高枕无忧了，然而bug又来了，由于这个自定义imageView使用的太频繁了，每一个界面有好几个手型，每一个手型的每一个指甲位置又放了6到7个这个自定义的imageview控件，oom来了，这个头痛的bug，优化吧，不优化使用着直接崩溃了老板不把我骂死，于是认真的检查自定义控件中任何一个可以优化释放内存的地方，当然很头疼的这个，因为稍微该动一个地方，本以为不会有耦合影响的，结果运行起来直接崩溃了，这样反复折腾了好多次，我的神经线都快紧张的崩溃了，最终定义了一个压缩图片资源的方法，解决了这个问题，因为代码里面动态每一次设置遮罩图片资源和要进行遮罩的图片资源，但是并没有进行压缩，所以那么多的地方调用，肯定会oom了，果然这个方法很奏效，不再出现oom异常了，而且由于改变了初始图片资源的大小，用户操作的时候加载速度明显提高了，体验流畅了许多，贴下压缩图片的代码

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
            如此一来效果确实还可以，看着自己自定义的一个控件，心里美滋滋的，哈哈。
