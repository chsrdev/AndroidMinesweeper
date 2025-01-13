package dev.chsr.minesweeper;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
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
    final int bombCount = 6;
    int flagCount = 0;
    int closedCardsCount = rows * columns;
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
    boolean isGameLost = false;
    TextView wonTextView;
    TextView lostTextView;
    List<Pair<Integer, Integer>> bombs = new ArrayList<>();

    GridLayout gridLayout;
    Button restartButton;
    TextView bombsCountTextView;
    TextView flagsTextView;

    LayoutInflater layoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutInflater = LayoutInflater.from(this);
        gridLayout = findViewById(R.id.grid);
        restartButton = findViewById(R.id.restartBtn);
        restartButton.setOnClickListener(view -> restartGame());
        flagsTextView = findViewById(R.id.flags);
        bombsCountTextView = findViewById(R.id.bombsCount);
        bombsCountTextView.setText(String.valueOf(bombCount));
        wonTextView = findViewById(R.id.wonText);
        lostTextView = findViewById(R.id.lostText);

        initGame();
    }

    private void restartGame() {
        if (wonTextView.getAlpha() == 1) {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(wonTextView, "alpha", 1, 0);
            fadeOut.setDuration(250);
            fadeOut.start();
        }
        if (lostTextView.getAlpha() == 1) {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(lostTextView, "alpha", 1, 0);
            fadeOut.setDuration(250);
            fadeOut.start();
        }
        closedCardsCount = rows * columns;
        isGameLost = false;
        new Handler().postDelayed(() -> {
            if (gridLayout.getAlpha() == 0) {
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(gridLayout, "alpha", 0, 1);
                fadeOut.setDuration(300);
                fadeOut.setStartDelay(100);
                fadeOut.start();
            }
            for (int i = 0; i < rows * columns; i++) {
                CardView card = (CardView) gridLayout.getChildAt(i);
                View text = card.findViewById(R.id.mine_text);
                View flag = card.findViewById(R.id.flagImage);
                if (text.getAlpha() == 1) {
                    ObjectAnimator fadeOut = ObjectAnimator.ofFloat(text, "alpha", 1, 0);
                    fadeOut.setDuration(250);
                    fadeOut.start();
                }
                if (flag.getAlpha() == 1) {
                    ObjectAnimator fadeOut = ObjectAnimator.ofFloat(flag, "alpha", 1, 0);
                    fadeOut.setDuration(250);
                    fadeOut.start();
                }
                if (card.getCardBackgroundColor().getDefaultColor() == 0xFFFF0000) {
                    ObjectAnimator fadeOut = ObjectAnimator.ofArgb(card, "cardBackgroundColor", 0xFFFF0000, 0xFFFFFFFF);
                    fadeOut.setDuration(250);
                    fadeOut.start();
                }
            }
            new Handler().postDelayed(this::initGame, 200);
        }, 250);
    }

    void lostGame() {
        for (int i = 0; i < rows * columns; i++) {
            gridLayout.getChildAt(i).setOnClickListener(view -> {
            });
            gridLayout.getChildAt(i).setOnLongClickListener(view -> true);
        }
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(gridLayout, "alpha", 1, 0);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(lostTextView, "alpha", 0, 1);
        fadeOut.setDuration(1000);
        fadeOut.start();
        fadeIn.setDuration(500);
        fadeIn.setStartDelay(500);
        fadeIn.start();
        new Handler().postDelayed(this::restartGame, 1600);
    }

    void wonGame() {
        Log.i("won", "Game won");
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(gridLayout, "alpha", 1, 0);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(wonTextView, "alpha", 0, 1);
        fadeOut.setDuration(1000);
        fadeOut.start();
        fadeIn.setDuration(500);
        fadeIn.setStartDelay(500);
        fadeIn.start();
    }

    void openCard(int y, int x, int depthLevel, List<Integer> visited) {
        if (minefield[y][x] == 9) return;
        if (minefield[y][x] < 0) return;
        if (minefield[y][x] == 10 && !isGameLost) {
            isGameLost = true;
            new Handler().postDelayed(this::lostGame, depthLevel * 300L);
        }
        int cardPos = y * columns + x;

        if (visited.contains(cardPos)) return;
        visited.add(cardPos);

        CardView card = (CardView) gridLayout.getChildAt(cardPos);
        card.setOnClickListener(_v -> {
        });
        card.setOnLongClickListener(_v -> true);

        ObjectAnimator rotateY = ObjectAnimator.ofFloat(card, "rotationY", 0f, 180f);
        ObjectAnimator animator = minefield[y][x] == 10
                ? ObjectAnimator.ofArgb(card, "cardBackgroundColor", 0xFFFFFFFF, 0xFFFF0000)
                : ObjectAnimator.ofFloat(card.findViewById(R.id.mine_text), "alpha", 0f, 1f);

        rotateY.setDuration(1000);
        rotateY.setStartDelay(depthLevel * 300L);
        animator.setStartDelay(depthLevel * 300L + 400);
        rotateY.start();
        animator.start();

        minefield[y][x] = -minefield[y][x];

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

        closedCardsCount--;
        if (closedCardsCount == bombCount && !isGameLost) {
            new Handler().postDelayed(this::wonGame, depthLevel * 300L);
        }
    }

    void setFlag(int y, int x) {
        int cardPos = y * columns + x;
        View card = gridLayout.getChildAt(cardPos);
        View flag = card.findViewById(R.id.flagImage);

        if (minefield[y][x] == 9) {
            minefield[y][x] = bombs.contains(new Pair<>(y, x)) ? 10 : calculateAround(y, x);
            flagCount--;
        } else {
            minefield[y][x] = 9;
            flagCount++;
        }
        flagsTextView.setText(String.valueOf(flagCount));
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(flag, "alpha", flag.getAlpha(), Math.abs(flag.getAlpha() - 1));
        fadeIn.setDuration(250);
        fadeIn.start();
    }

    void initGame() {
        initField();

        flagCount = 0;
        gridLayout.removeAllViews();
        gridLayout.setColumnCount(columns);
        gridLayout.setRowCount(rows);
        flagsTextView.setText("0");

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
                card.setOnLongClickListener(view -> {
                    setFlag(y, x);
                    return true;
                });
                gridLayout.addView(card);
            }
        }

    }

    void initField() {
        Random random = new Random();
        bombs = new ArrayList<>();
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
            bombs.add(new Pair<>(y, x));
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
