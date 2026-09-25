package ch.heigvd.amt.classloaders;

/** Le service qu'un plugin déclare ; ServiceLoader en trouve les implémentations dans son JAR. */
public interface Exporter {

    String name();
}
