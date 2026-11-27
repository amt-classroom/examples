package ch.heigvd.amt.conditional;

/** Une scène du festival. Sa version augmente à chaque modification. */
public record Stage(long id, String name, int capacity, int version) {}
