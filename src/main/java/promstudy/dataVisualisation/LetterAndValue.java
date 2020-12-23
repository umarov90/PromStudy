/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package promstudy.dataVisualisation;

import java.awt.Color;

/**
 *
 * @author Umarov
 */
public class LetterAndValue implements Comparable<LetterAndValue> {

    public char c;
    public int val;
    public Color col;

    public LetterAndValue(char c, int val, Color col) {
        this.c = c;
        this.val = val;
        this.col = col;
    }

    @Override
    public int compareTo(LetterAndValue o) {
        return Integer.compare(val, o.val);
    }
}
