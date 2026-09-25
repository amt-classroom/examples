package ch.heigvd.amt.proxies;

import java.util.List;

/** Une implémentation ordinaire, sans rien qui la prépare à être proxifiée. */
public class BasicCalculator implements Calculator {

    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public int addAll(List<Integer> values) {
        var sum = 0;
        for (var value : values) {
            sum = add(sum, value); // this.add(sum, value) : un proxy autour de l'instance ne verrait rien
        }
        return sum;
    }
}
