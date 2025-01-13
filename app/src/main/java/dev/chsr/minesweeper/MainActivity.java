package dev.chsr.minesweeper;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    final int rows = 7;
    final int columns = 7;
    final int bombCount = 10;
    int[][] minefield = new int[rows][columns];
    int[] bombCountColor = {
            0xAAAAAAAA,
            0xFF0000FF,
            0xFF00AA00,
            0xFFFF0000,
            0xFF000080,
            0xFF800000,
            0xFF008000,
            0xFF000000,
            0xFF808080
    };
    boolean isGameStarted = false;

    GridLayout gridLayout;
    Button restartButton;

    LayoutInflater layoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutInflater = LayoutInflater.from(this);
        gridLayout = findViewById(R.id.grid);
        restartButton = findViewById(R.id.restartBtn);
        restartButton.setOnClickListener(view -> initGame());

        initGame();
    }

    void openCard(int y, int x, int depthLevel, List<Integer> visited) {
        int cardPos = y * columns + x;

        if (visited.contains(cardPos)) return;
        visited.add(cardPos);

        CardView card = (CardView) gridLayout.getChildAt(cardPos);
        card.setOnClickListener(_v -> {
        });

        ObjectAnimator rotateY = ObjectAnimator.ofFloat(card, "rotationY", 0f, 180f);
        ObjectAnimator animator = minefield[y][x] == 10
                ? ObjectAnimator.ofArgb(card, "cardBackgroundColor", 0xFFFFFFFF, 0xFFFF0000)
                : ObjectAnimator.ofFloat(card.findViewById(R.id.mine_text), "alpha", 0f, 1f);

        rotateY.setDuration(1000);
        rotateY.setStartDelay(depthLevel * 300L);
        animator.setStartDelay(depthLevel * 300L + 400);
        rotateY.start();
        animator.start();

        if (minefield[y][x] == 0) {
            new Handler().postDelayed(() -> {
                for (int _y = y - 1; _y <= y + 1; _y++) {
                    for (int _x = x - 1; _x <= x + 1; _x++) {
                        if (_y >= 0 && _y < rows && _x >= 0 && _x < columns && !(_y == y && _x == x)) {
                            openCard(_y, _x, depthLevel + 1, visited);
                        }
                    }
                }
            }, 0);
        }
    }


    void initGame() {
        initField();

        gridLayout.removeAllViews();
        gridLayout.setColumnCount(columns);
        gridLayout.setRowCount(rows);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                final int y = i;
                final int x = j;
                final int bombsAround = calculateAround(y, x);

                CardView card = (CardView) layoutInflater.inflate(R.layout.card, gridLayout, false);
                ObjectAnimator rotateY = ObjectAnimator.ofFloat(card, "rotationY", 0f, 180f);
                rotateY.setDuration(500);
                rotateY.start();
                TextView mineText = card.findViewById(R.id.mine_text);
                mineText.setText(String.valueOf(minefield[y][x]));
                mineText.setTextColor(bombCountColor[bombsAround]);
                card.setOnClickListener(view -> openCard(y, x, 0, new ArrayList<>()));
                gridLayout.addView(card);
            }
        }

    }

    void initField() {
        isGameStarted = true;
        Random random = new Random();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                minefield[y][x] = 0;
            }
        }
        for (int i = 0; i < bombCount; i++) {
            int y = random.nextInt(rows);
            int x = random.nextInt(columns);
            while (minefield[y][x] == 10) {
                y = random.nextInt(rows);
                x = random.nextInt(columns);
            }
            minefield[y][x] = 10;
        }
    }

    int calculateAround(int y, int x) {
        int bombsAround = 0;
        for (int i = y - 1; i < y + 2; i++) {
            for (int j = x - 1; j < x + 2; j++) {
                if (i >= 0 && i < columns && j >= 0 && j < rows && minefield[i][j] == 10) {
                    bombsAround++;
                }
            }
        }
        if (minefield[y][x] != 10)
            minefield[y][x] = bombsAround;
        return bombsAround;
    }
}
