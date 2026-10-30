package ch.heigvd.amt.proxies;

import java.util.List;

public interface Calculator {

    int add(int a, int b);

    int addAll(List<Integer> values);
}
