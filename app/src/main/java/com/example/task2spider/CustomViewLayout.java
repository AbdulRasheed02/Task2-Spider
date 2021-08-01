package com.example.task2spider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.RequiresApi;

import java.util.Random;

public class CustomViewLayout extends SurfaceView implements Runnable{

    Thread thread=null;
    Boolean canDraw=false;
    Canvas canvas;
    SurfaceHolder surfaceHolder;
    Context context;

    Boolean noviceMode,normalMode,nightmareMode;
    Boolean endgameFlag, winFlag, vibratorFlag;

    Random random;

    int columns, rows, cellSize,width, height;
    int score, highScoreNovice, highScoreNormal, highScoreNightmare, highScore;
    int totalBombs, flagsRemaining;

    int cell[][],empty,bomb;
    int cellState[][],hidden,revealed;
    int cellNeighbourBomb[][];
    Boolean cellFlag[][],notFlagged,flagged;

    Boolean flagMode;

    Rect cellRect[][];
    RectF btn1Background,btn2Background,flagRect;

    Paint paint_Black,paint_White,paint_Grey,paint_Grey2,paint_Green,paint_Red,paint_Button;
    Paint.FontMetrics fm;
    int margin;

    public CustomViewLayout(Context context) {
        super(context);
        surfaceHolder=getHolder();
        canvas=new Canvas();
        this.context=context;
    }

    public CustomViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomViewLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void run() {
        initialise();
        generateBomb();
        generateNeighbourNumber();
        paint();

        while (canDraw){
            if(!surfaceHolder.getSurface().isValid()){
                continue;
            }
            canvas=surfaceHolder.lockCanvas();
            canvas.drawColor(Color.WHITE);
            grid();
            header();
            endgame();
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause(){

        canDraw=false;

        while (true) {
            try {
                thread.join();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        thread=null;
    }

    public void resume(){
        canDraw=true;
        thread=new Thread(this);
        thread.start();
    }

    private void initialise(){

        Bundle transporter = ((Activity)getContext()).getIntent().getExtras();
        noviceMode=transporter.getBoolean("NoviceMode");
        normalMode=transporter.getBoolean("NormalMode");
        nightmareMode=transporter.getBoolean("NightmareMode");

        SharedPreferences prefs = context.getSharedPreferences("HighScorePrefsKey", Context.MODE_PRIVATE);
        highScoreNovice = prefs.getInt("HighScoreNoviceKey", 0);
        highScoreNormal= prefs.getInt("HighScoreNormalKey",0);
        highScoreNightmare= prefs.getInt("HighScoreNightmareKey",0);

        columns=8;
        rows=8;
        cellSize=120;
        highScore=0;

        cell=new int[columns][rows];
        empty=0;
        bomb=1;

        cellState =new int[columns][rows];
        hidden=0;
        revealed=1;

        cellNeighbourBomb=new int[columns][rows];

        cellFlag=new Boolean[columns][rows];
        notFlagged=false;
        flagged=true;

        cellRect=new Rect[columns][rows];

        if(noviceMode){
            totalBombs=10;
            highScore = prefs.getInt("HighScoreNoviceKey", 0);
        }
        else if(normalMode){
            totalBombs=15;
            highScore = prefs.getInt("HighScoreNormalKey", 0);
        }
        else {
            totalBombs=25;
            highScore = prefs.getInt("HighScoreNightmareKey", 0);
        }


        for(int i=0;i<columns;i++){
            for(int j=0;j<rows;j++){
                cell[i][j]=empty;
                cellState[i][j]=hidden;
                cellNeighbourBomb[i][j]=0;
                cellFlag[i][j]=notFlagged;
            }
        }

        random=new Random();
        score=0;

        endgameFlag=false;
        winFlag=false;
        vibratorFlag=false;

        flagMode=false;
        flagsRemaining=totalBombs;
    }

    private void grid(){
        width=canvas.getWidth();
        height=canvas.getHeight();

        for (int i=0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                cellRect[i][j]=new Rect();
                cellRect[i][j].left=(width/2)-((4-i)*cellSize);
                cellRect[i][j].top=(height/2)-((4-j)*cellSize);
                cellRect[i][j].right= cellRect[i][j].left+cellSize;
                cellRect[i][j].bottom= cellRect[i][j].top+cellSize;

                if(cellState[i][j]==hidden) {
                    canvas.drawRect(cellRect[i][j],paint_Grey);
                    if(cellFlag[i][j]==flagged){
                        paint_Black.setTextSize(75);
                        paint_Black.setTextAlign(Paint.Align.CENTER);
                        canvas.drawText("\uD83D\uDEA9",(cellRect[i][j].left+cellRect[i][j].right)/2,(cellRect[i][j].top+cellRect[i][j].bottom)/2+25,paint_Black);
                    }
                }
                else if(cellState[i][j]==revealed && cell[i][j]==bomb){
                    canvas.drawRect(cellRect[i][j],paint_Grey2);
                    paint_Black.setTextSize(75);
                    paint_Black.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText("\uD83D\uDCA3",(cellRect[i][j].left+cellRect[i][j].right)/2,(cellRect[i][j].top+cellRect[i][j].bottom)/2+25,paint_Black);
                }
                else{
                    canvas.drawRect(cellRect[i][j],paint_Grey2);
                    if(cellNeighbourBomb[i][j]==0){

                    }
                    else if(cellNeighbourBomb[i][j]==1){
                        paint_White.setTextSize(75);
                        paint_White.setTextAlign(Paint.Align.CENTER);
                        canvas.drawText(String.valueOf(cellNeighbourBomb[i][j]),(cellRect[i][j].left+cellRect[i][j].right)/2,(cellRect[i][j].top+cellRect[i][j].bottom)/2+25,paint_White);
                    }
                    else if(cellNeighbourBomb[i][j]==2){
                        paint_Green.setTextSize(75);
                        paint_Green.setTextAlign(Paint.Align.CENTER);
                        canvas.drawText(String.valueOf(cellNeighbourBomb[i][j]),(cellRect[i][j].left+cellRect[i][j].right)/2,(cellRect[i][j].top+cellRect[i][j].bottom)/2+25,paint_Green);
                    }
                    else{
                        paint_Red.setTextSize(75);
                        paint_Red.setTextAlign(Paint.Align.CENTER);
                        canvas.drawText(String.valueOf(cellNeighbourBomb[i][j]),(cellRect[i][j].left+cellRect[i][j].right)/2,(cellRect[i][j].top+cellRect[i][j].bottom)/2+25,paint_Red);
                    }

                }
            }
        }

        for(int i=0; i<columns+1;i++){
            canvas.drawLine((width/2)-((4-i)*cellSize),(height/2)-(4*cellSize),(width/2)-((4-i)*cellSize),(height/2)+(4*cellSize),paint_White);
        }

        for(int j=0; j<rows+1; j++){
            canvas.drawLine((width/2)-(4*cellSize),(height/2)-((4-j)*cellSize),(width/2)+(4*cellSize),(height/2)-((4-j)*cellSize),paint_White);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float downx = event.getX();
            float downy = event.getY();

            if(endgameFlag || winFlag){
                if(downx>=btn1Background.left && downx<=btn1Background.right && downy>= btn1Background.top && downy <= btn1Background.bottom){
                    Intent Restart= new Intent(context, CustomViewActivity.class);
                    Restart.putExtra("NoviceMode", noviceMode);
                    Restart.putExtra("NormalMode", normalMode);
                    Restart.putExtra("NightmareMode", nightmareMode);
                    context.startActivity(Restart);
                }
                else if(downx>=btn2Background.left && downx<=btn2Background.right && downy>= btn2Background.top && downy <= btn2Background.bottom){
                    context.startActivity(new Intent(context, MainActivity.class));
                }
            }
            else {
                if (downx > cellRect[0][0].left && downx < cellRect[7][7].right && downy > cellRect[0][0].top && downy < cellRect[7][7].bottom) {
                    for (int i = 0; i < columns; i++) {
                        for (int j = 0; j < rows; j++) {
                            if (downx > cellRect[i][j].left && downx < cellRect[i][j].right && downy > cellRect[i][j].top && downy < cellRect[i][j].bottom) {
                                cellTouched(i, j);
                            }
                        }
                    }
                }
                if(downx>flagRect.left && downx<flagRect.right && downy>flagRect.top && downy<flagRect.bottom){
                    flagMode=!flagMode;
                }

            }

        }
        return true;
    }

    private void generateBomb(){
        for(int x=0;x<totalBombs;){
            int i=random.nextInt(columns);
            int j=random.nextInt(rows);
            if(cell[i][j]==bomb){
                continue;
            }
            else{
                cell[i][j]=bomb;
                x++;
            }
        }
    }

    private void generateNeighbourNumber(){
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                if(cell[i][j]==empty){
                    if(i-1>=0 && i-1<columns && cell[i-1][j]==bomb){
                        cellNeighbourBomb[i][j]++;
                    }
                    if(i+1>=0 && i+1<columns && cell[i+1][j]==bomb){
                        cellNeighbourBomb[i][j]++;
                    }
                    if(j-1>=0 && j-1<rows && cell[i][j-1]==bomb){
                        cellNeighbourBomb[i][j]++;
                    }
                    if(j+1>=0 && j+1<rows && cell[i][j+1]==bomb){
                        cellNeighbourBomb[i][j]++;
                    }
                    if(i-1>=0 && i-1<columns && j-1>=0 && j-1<rows && cell[i-1][j-1]==bomb){
                        cellNeighbourBomb[i][j]++;
                    }
                    if(i+1>=0 && i+1<columns && j-1>=0 && j-1<rows &&cell[i+1][j-1]==bomb){
                        cellNeighbourBomb[i][j]++;
                    }
                    if(i-1>=0 && i-1<columns && j+1>=0 && j+1<rows && cell[i-1][j+1]==bomb){
                        cellNeighbourBomb[i][j]++;
                    }
                    if(i+1>=0 && i+1<columns && j+1>=0 && j+1<rows && cell[i+1][j+1]==bomb){
                        cellNeighbourBomb[i][j]++;
                    }
                }
            }
        }
    }

    private void cellTouched(int i, int j){
        if(flagMode){
            if(flagsRemaining>=1) {
                if (cellState[i][j] == hidden) {
                    cellFlag[i][j] = !cellFlag[i][j];
                    if(cellFlag[i][j]==flagged){
                        flagsRemaining--;
                    }
                    else{
                        flagsRemaining++;
                    }
                }
            }
            else{
                if(cellFlag[i][j]==flagged){
                    cellFlag[i][j] = !cellFlag[i][j];
                    flagsRemaining++;
                }
            }
        }
        else if(cellFlag[i][j]==flagged){

        }
        else if(cell[i][j]==bomb){
            cellState[i][j]=revealed;
            endgameFlag = true;
            vibratorFlag=true;
        }
        else{
            if(cellState[i][j]==hidden) {
                cellState[i][j] = revealed;
                score++;
                if(cellNeighbourBomb[i][j]==0) {
                    generateNeighbourReveal(i, j);
                }
                if(score==(columns*rows)-totalBombs){
                    winFlag=true;
                }
            }
        }
    }

    private void generateNeighbourReveal(int I,int J){
        int i=I,j=J;
        while(i-1>=0 && i-1<columns && cell[i-1][j]==empty && cellState[i-1][j]==hidden && cellFlag[i-1][j]==notFlagged){
            cellState[i-1][j]=revealed;
            score++;
            if(cellNeighbourBomb[i-1][j]>0){
                break;
            }
            i--;
            generateNeighbourReveal(i,j);
        }

        i=I;
        j=J;
        while(i+1>=0 && i+1<columns && cell[i+1][j]==empty && cellState[i+1][j]==hidden && cellFlag[i+1][j]==notFlagged){
            cellState[i+1][j]=revealed;
            score++;
            if(cellNeighbourBomb[i+1][j]>0){
                break;
            }
            i++;
            generateNeighbourReveal(i,j);
        }

        i=I;
        j=J;
        while(j-1>=0 && j-1<columns && cell[i][j-1]==empty && cellState[i][j-1]==hidden && cellFlag[i][j-1]==notFlagged){
            cellState[i][j-1]=revealed;
            score++;
            if(cellNeighbourBomb[i][j-1]>0){
                break;
            }
            j--;
            generateNeighbourReveal(i,j);
        }

        i=I;
        j=J;
        while(j+1>=0 && j+1<columns && cell[i][j+1]==empty && cellState[i][j+1]==hidden && cellFlag[i][j+1]==notFlagged){
            cellState[i][j+1]=revealed;
            score++;
            if(cellNeighbourBomb[i][j+1]>0){
                break;
            }
            j++;
            generateNeighbourReveal(i,j);
        }

        i=I;
        j=J;
        while(i-1>=0 && i-1<columns && j-1>=0 && j-1<rows && cell[i-1][j-1]==empty && cellState[i-1][j-1]==hidden && cellFlag[i-1][j-1]==notFlagged){
            cellState[i-1][j-1]=revealed;
            score++;
            if(cellNeighbourBomb[i-1][j-1]>0){
                break;
            }
            i--;
            j--;
            generateNeighbourReveal(i,j);
        }

        i=I;
        j=J;
        while(i+1>=0 && i+1<columns && j-1>=0 && j-1<rows && cell[i+1][j-1]==empty && cellState[i+1][j-1]==hidden && cellFlag[i+1][j-1]==notFlagged){
            cellState[i+1][j-1]=revealed;
            score++;
            if(cellNeighbourBomb[i+1][j-1]>0){
                break;
            }
            i++;
            j--;
            generateNeighbourReveal(i,j);
        }

        i=I;
        j=J;
        while(i-1>=0 && i-1<columns && j+1>=0 && j+1<rows && cell[i-1][j+1]==empty && cellState[i-1][j+1]==hidden && cellFlag[i-1][j+1]==notFlagged){
            cellState[i-1][j+1]=revealed;
            score++;
            if(cellNeighbourBomb[i-1][j+1]>0){
                break;
            }
            i--;
            j++;
            generateNeighbourReveal(i,j);
        }

        i=I;
        j=J;
        while(i+1>=0 && i+1<columns && j+1>=0 && j+1<rows && cell[i+1][j+1]==empty && cellState[i+1][j+1]==hidden && cellFlag[i+1][j+1]==notFlagged){
            cellState[i+1][j+1]=revealed;
            score++;
            if(cellNeighbourBomb[i+1][j+1]>0){
                break;
            }
            i++;
            j++;
            generateNeighbourReveal(i,j);
        }
    }

    private void endgame(){

        if(endgameFlag || winFlag) {
            paint_Black.setTextSize(100);
            paint_Black.setTextAlign(Paint.Align.CENTER);

            if(winFlag){
                canvas.drawText("YOU WIN", canvas.getWidth() / 2, ((200+cellRect[0][0].top)/2)-30, paint_Black);
            }
            else {
                canvas.drawText("GAME OVER", canvas.getWidth() / 2, ((200 + cellRect[0][0].top)/2)-30 , paint_Black);
            }

            buttons();

            SharedPreferences prefs = context.getSharedPreferences("HighScorePrefsKey", Context.MODE_PRIVATE);
            highScoreNovice = prefs.getInt("HighScoreNoviceKey", 0);
            highScoreNormal = prefs.getInt("HighScoreNormalKey", 0);
            highScoreNightmare = prefs.getInt("HighScoreNightmareKey", 0);

            if (noviceMode && score >= highScoreNovice) {
                highScoreNovice = score;
                highScore=score;
            } else if (normalMode && score >= highScoreNormal) {
                highScoreNormal = score;
                highScore=score;
            } else if (nightmareMode && score >= highScoreNightmare) {
                highScoreNightmare = score;
                highScore=score;
            }

            prefs = context.getSharedPreferences("HighScorePrefsKey", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("HighScoreNoviceKey", highScoreNovice);
            editor.putInt("HighScoreNormalKey", highScoreNormal);
            editor.putInt("HighScoreNightmareKey", highScoreNightmare);
            editor.commit();

            if(vibratorFlag) {
                CustomViewActivity.vibrator.vibrate(75);
                vibratorFlag=false;
            }
        }

    }

    private void buttons(){
        btn1Background=new RectF();
        btn2Background=new RectF();

        String str_restart ="RESTART";
        String str_menu="MENU";

        paint_Button.setColor(Color.parseColor("#808080"));
        paint_Button.setTextSize(75.0F);
        paint_Button.getFontMetrics(fm);

        btn1Background.left=(canvas.getWidth()/2)-(paint_Button.measureText(str_restart)/2)-margin;
        btn1Background.top=cellRect[7][7].bottom-fm.top+150;
        btn1Background.right=btn1Background.left+paint_Button.measureText(str_restart)+(2*margin);
        btn1Background.bottom=btn1Background.top+fm.bottom+75;

        btn2Background.left=(canvas.getWidth()/2)-(paint_Button.measureText(str_menu)/2)-margin;
        btn2Background.top=btn1Background.top+150;
        btn2Background.right=btn2Background.left+paint_Button.measureText(str_menu)+(2*margin);
        btn2Background.bottom=btn1Background.bottom+150;

        canvas.drawRect(btn1Background,paint_Button);
        canvas.drawRect(btn2Background,paint_Button);

        paint_Button.setColor(Color.WHITE);

        canvas.drawText(str_restart,(canvas.getWidth()/2)-(paint_Button.measureText(str_restart)/2),btn1Background.top+75,paint_Button);
        canvas.drawText(str_menu,(canvas.getWidth()/2)-(paint_Button.measureText(str_menu)/2),btn2Background.top+75,paint_Button);
    }

    private void header(){
        paint_Black.setTextSize(75);

        paint_Black.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Score : "+score,(canvas.getWidth()/2)-(4*cellSize)+10,200,paint_Black);

        paint_Black.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("\uD83D\uDCA3 : "+totalBombs,(canvas.getWidth()/2)+(4*cellSize)-10,200,paint_Black);

        String flagModeString;
        if(flagMode){
            flagModeString="On";
        }
        else{
            flagModeString="Off";
        }
        String flagString="Flag : "+flagModeString+"   \uD83D\uDEA9 : "+flagsRemaining;

        flagRect=new RectF();
        paint_Black.setTextAlign(Paint.Align.CENTER);
        paint_Black.setTextSize(60);
        paint_Black.getFontMetrics(fm);

        flagRect.left=(canvas.getWidth()/2)-(paint_Black.measureText(flagString)/2)-margin;
        flagRect.top=cellRect[0][0].top-fm.top-220;
        flagRect.right=flagRect.left+paint_Black.measureText(flagString)+(2*margin);
        flagRect.bottom=flagRect.top+fm.bottom+90;

        canvas.drawRect(flagRect,paint_Grey);
        canvas.drawText(flagString,(canvas.getWidth()/2),flagRect.top+75,paint_Black);

        paint_Black.setTextSize(60);
        paint_Black.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("High Score : "+highScore,canvas.getWidth()/2,cellRect[7][7].bottom+120,paint_Black);
    }

    private void paint(){
        fm = new Paint.FontMetrics();
        margin=20;

        paint_Black=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_Black.setColor(Color.BLACK);
        paint_Black.setStyle(Paint.Style.FILL);
        paint_Black.setTextSize(75);

        paint_White=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_White.setColor(Color.WHITE);
        paint_White.setStyle(Paint.Style.FILL);
        paint_White.setStrokeWidth(6);

        paint_Grey=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_Grey.setColor(Color.parseColor("#c2c2c2"));
        paint_Grey.setStyle(Paint.Style.FILL);

        paint_Grey2=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_Grey2.setColor(Color.parseColor("#696969"));
        paint_Grey2.setStyle(Paint.Style.FILL);

        paint_Green=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_Green.setColor(Color.parseColor("#FF03DAC5"));
        paint_Green.setStyle(Paint.Style.FILL);

        paint_Red=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_Red.setColor(Color.parseColor("#ff4a4a"));
        paint_Red.setStyle(Paint.Style.FILL);

        paint_Button=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_Button.setColor(Color.parseColor("#808080"));
        paint_Button.setStyle(Paint.Style.FILL);
        paint_Button.setTextSize(75.0F);
    }
}
