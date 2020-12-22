package com.example.stepview.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet

import android.util.Log
import android.view.View
import cn.weeget.youxuanapp.common.util.ext.dp
import cn.weeget.youxuanapp.common.util.ext.sp
import com.example.stepview.R


/**
 * name：xiaoyu
 * time: 2020/12/18 09:48
 * desc:
 */
@Suppress("DEPRECATION")
@SuppressLint("UseCompatLoadingForDrawables")
class StepView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private var currentStep: Int=0
    private var lineHeight: Int = 0
    private var totalStep:Int  = 5 // 默认有5个步骤
    private  var textSize:Int=0 //字体大小
    private var circleRadius =0
    private   var  entries: Array<CharSequence>
    private var space:Int = 0//计算间距
    private var padding:Int = 0

    private var linePaint: Paint = Paint()
    private var circlePaint:Paint = Paint()
    private var progressPaint: Paint = Paint()
    private var bitmapPaint: Paint = Paint()
    private var finishLineColor:Int=0
    private var unfinishedLineColor:Int=0

    //画笔画文字：
    private var textPaint:Paint = Paint()

    private var circleDots:MutableList<PointF> = mutableListOf()//圆的位置：
    private   var mBitmap:Bitmap
    private lateinit var mBound: Rect
    private  var widthSize:Int=0
    private  var heightSize:Int=0
    private var textDots:MutableList<PointF> = mutableListOf()
    private var bitmapDots:MutableList<PointF> = mutableListOf()



    private var bitmapWidth:Int = 0
    private var bitmapHeight:Int = 0

    private var progressX:Float = 0f


    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.stepView)
        totalStep =ta.getInt(R.styleable.stepView_totalStep, 5)
        currentStep = ta.getInt(R.styleable.stepView_currentStep,totalStep/2)
        textSize = ta.getDimensionPixelSize(R.styleable.stepView_textSize, 12.sp)
        circleRadius = ta.getDimensionPixelSize(R.styleable.stepView_circleRadius, 7.dp)
        lineHeight = ta.getDimensionPixelSize(R.styleable.stepView_lineHeight, 2.dp)
        padding = ta.getDimensionPixelSize(R.styleable.stepView_textImagePadding, 10.dp)
        finishLineColor = ta.getColor(R.styleable.stepView_finishLineColor, Color.WHITE)
        unfinishedLineColor = ta.getColor(R.styleable.stepView_unfinishedLineColor, Color.parseColor("#aaffffff"))
        val d: BitmapDrawable?= ta.getDrawable(R.styleable.stepView_finishSrc) as BitmapDrawable?
        val bitmap = d?.bitmap
        mBitmap = if(bitmap ==null){
            val bitmapDrawable = resources.getDrawable(R.drawable.icon_checkbox) as BitmapDrawable
            bitmapDrawable.bitmap
        }else{
            bitmap
        }
        bitmapWidth = mBitmap.width
        bitmapHeight = mBitmap.height
        entries = ta.getTextArray(R.styleable.stepView_entries)
        if(entries.size!=totalStep){
            throw IllegalArgumentException("entry can't be empty or the entry size must be" +
                    " equal the totalStep")
        }

        initPaints()
        getTextViewBounds()
        ta.recycle()
    }

    private fun initPaints() {
        linePaint.style = Paint.Style.FILL//实心
        linePaint.strokeWidth = lineHeight.toFloat()
        linePaint.color = unfinishedLineColor
        linePaint.isAntiAlias = true

        progressPaint.style = Paint.Style.FILL
        progressPaint.strokeWidth = lineHeight.toFloat()
        progressPaint.color = finishLineColor
        progressPaint.isAntiAlias = true


        circlePaint.style = Paint.Style.FILL
        circlePaint.strokeWidth = circleRadius.toFloat()
        circlePaint.color = finishLineColor
        circlePaint.isAntiAlias = true


        textPaint.textSize= textSize.toFloat()
        textPaint.color = finishLineColor
        textPaint.isAntiAlias = true

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
       widthSize = MeasureSpec.getSize(widthMeasureSpec)
       heightSize = MeasureSpec.getSize(heightMeasureSpec)
       setMeasuredDimension(widthSize, heightSize)
       //计算间距：
       space =  widthSize/(totalStep-1)
       Log.d("hxy", "space = $space")

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d("hxy", "onSizeChanged")
        //在尺寸改变的时候，初始画圆点
        initCirclePoints()
        startAnim()
    }



    private fun initCirclePoints() {
        circleDots.clear()
        textDots.clear()
        bitmapDots.clear()
        var textHeight = mBound.bottom-mBound.top
        var textWidth = mBound.right -mBound.left
        Log.d("hxy ","textHeight = $textHeight,textWidth=$textWidth")
        var textPoint = padding*2+textHeight
        var offset = circleRadius/2
        for(i in 0 until totalStep){
            //第一个圆的位置：
            when (i) {
                0 -> {
                    var pointFirst = PointF(0f + circleRadius, heightSize / 2.toFloat())
                    circleDots.add(pointFirst)
                    Log.d("hxy", "padding=$padding,${mBound}")
                    textDots.add(PointF(0f, heightSize / 2.toFloat() + textPoint))
                    bitmapDots.add(PointF(0f, heightSize / 2.toFloat() - bitmapHeight / 2))
                }
                totalStep - 1 -> {
                    var pointLast = PointF(space * i.toFloat() - circleRadius, heightSize / 2.toFloat())
                    circleDots.add(pointLast)
                    textDots.add(PointF(widthSize - textWidth.toFloat()-offset, heightSize / 2.toFloat() + textPoint))
                    bitmapDots.add(PointF(widthSize - bitmapWidth.toFloat(), heightSize / 2.toFloat() - bitmapHeight / 2))
                }
                else -> {//中间位置
                    var point= PointF(space * i.toFloat(), heightSize / 2.toFloat())
                    circleDots.add(point)
                    textDots.add(PointF(space * i.toFloat() - circleRadius / 2 -  textWidth/2, heightSize / 2.toFloat() + textPoint))
                    bitmapDots.add(PointF(space * i.toFloat() - circleRadius / 2 - bitmapWidth / 2, heightSize / 2.toFloat() - bitmapHeight / 2))
                }
            }

        }
    }

    /**
     * 外界调用
     */
    open fun setProgress(currentStep: Int){
        if(currentStep<0||currentStep>totalStep-1){
            this.currentStep = 0
        }else{
            this.currentStep = currentStep
        }
        startAnim()
    }

    /**
     * 计算应该停止的点：
     */
    private fun calculateStop():Float {
        if(circleDots.isEmpty()){
            initCirclePoints()
        }
        if(currentStep>=totalStep){
            IllegalArgumentException("currentStep  can't be lager than totalStep")
        }
        return  circleDots[currentStep].x
    }



    private  fun  startAnim() {
        val valueAnimatorX = ValueAnimator.ofFloat(0f, calculateStop())
        valueAnimatorX.duration = 2000
        valueAnimatorX.addUpdateListener { animation ->
            progressX = animation.animatedValue as Float
            invalidate()
        }
        valueAnimatorX.start()
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        //画直线：
        canvas?.drawLine(0f, heightSize / 2.toFloat(),
                width.toFloat(), heightSize / 2.toFloat(),
                linePaint)
        //画进度：
        canvas?.drawLine(0f, heightSize / 2.toFloat(),
                progressX, heightSize / 2.toFloat(),
                progressPaint)

        //画圆圈：根据屏幕分成5份，画对应的圈圈，确定每个园的起点
        for(i in 0 until totalStep){
            //区分是画bitmap还是circle
            if(i<=currentStep){
                canvas?.drawBitmap(mBitmap, bitmapDots[i].x, bitmapDots[i].y, bitmapPaint)
            }else{
                canvas?.drawCircle(circleDots[i].x, circleDots[i].y, circleRadius.toFloat(), circlePaint)
            }

            //画文字
            if(i==currentStep){
                textPaint.typeface = Typeface.DEFAULT_BOLD
                canvas?.drawText(entries[i].toString(), textDots[i].x, textDots[i].y, textPaint)
            }else{
                textPaint.typeface = Typeface.DEFAULT
                canvas?.drawText(entries[i].toString(), textDots[i].x, textDots[i].y, textPaint)
            }
        }

    }


    private fun getTextViewBounds(){
        mBound = Rect()
        //根据最后一个文字做测量
        val minText = entries[entries.size-1].toString()
        textPaint.getTextBounds(minText, 0, minText.length, mBound)
    }


}