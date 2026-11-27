package ch.heigvd.amt.tables;

import java.util.List;

/** Un tableau du rapport : son titre, puis ses lignes. La première ligne porte les en-têtes. */
public record Table(String title, List<List<String>> rows) {}
