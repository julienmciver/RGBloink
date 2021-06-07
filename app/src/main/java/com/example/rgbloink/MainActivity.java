package com.example.rgbloink;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {



    private ImageView imageView;
    private Button button;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        imageView = (ImageView) findViewById(R.id.colorWheel);
        button = (Button) findViewById(R.id.tempView);

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache(true);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int x = (int) event.getX();
                int y = (int) event.getY();

                if ((event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) && ((event.getX() >= 0) && (event.getY() >= 0))){
                    try {
                        bitmap = imageView.getDrawingCache();
                        int pixel = bitmap.getPixel(x, y);

                        int r = Color.red(pixel);
                        int g = Color.green(pixel);
                        int b = Color.blue(pixel);
                        if (!(r==g && g == b && b == 0)) {
                            button.setBackgroundColor(Color.rgb(r, g, b));
                        }
                    }
                    catch (Exception e){

                    }
                }

                return true;
            }
        });
    }
}