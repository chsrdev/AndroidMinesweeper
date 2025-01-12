package dev.chsr.minesweeper;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    final int rows = 7;
    final int columns = 7;
    final int bombCount = 10;
    int[][] minefield = new int[rows][columns];

    GridLayout gridLayout;
    Button restartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridLayout = findViewById(R.id.grid);
        restartButton = findViewById(R.id.restartBtn);

        initGame();

        restartButton.setOnClickListener(view -> initGame());
    }

    void initGame() {
        initField();

        gridLayout.removeAllViews();

        gridLayout.setColumnCount(columns);
        gridLayout.setRowCount(rows);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                final int y = i;
                final int x = j;
                calculateAround(y, x);
                CardView card = (CardView) layoutInflater.inflate(R.layout.card, gridLayout, false);
                ObjectAnimator rotateY = ObjectAnimator.ofFloat(card, "rotationY", 0f, 180f);
                rotateY.setDuration(500);
                rotateY.start();

                TextView mineText = card.findViewById(R.id.mine_text);
                mineText.setText(String.valueOf(minefield[y][x]));

                card.setOnClickListener(view -> {
                    card.setOnClickListener(_v -> {
                    });
                    rotateY.setDuration(1000);
                    rotateY.start();
                    ObjectAnimator animator = minefield[y][x] == -1 ?
                            ObjectAnimator.ofArgb(card, "cardBackgroundColor", 0xFFFFFFFF, 0xFFFF0000)
                            : ObjectAnimator.ofFloat(mineText, "alpha", 0f, 1f);
                    animator.setStartDelay(400);
                    animator.setDuration(600);
                    animator.start();
                });
                gridLayout.addView(card);
            }
        }
    }

    void initField() {
        Random random = new Random();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                minefield[y][x] = 0;
            }
        }
        for (int i = 0; i < bombCount; i++) {
            minefield[random.nextInt(rows)][random.nextInt(columns)] = -1;
        }
    }

    void calculateAround(int y, int x) {
        if (minefield[y][x] == -1) return;
        for (int i = y - 1; i < y + 2; i++) {
            for (int j = x - 1; j < x + 2; j++) {
                if (i >= 0 && i < columns && j >= 0 && j < rows && minefield[i][j] == -1)
                    minefield[y][x] += 1;
            }
        }
    }
}
