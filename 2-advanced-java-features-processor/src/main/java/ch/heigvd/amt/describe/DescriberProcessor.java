package ch.heigvd.amt.describe;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

/** Génère, pour chaque classe {@code @Describe}, ce que la reflection lit à l'exécution : un describer. */
@SupportedAnnotationTypes("*") // le module compile aussi les exemples des autres sections : rien d'autre à revendiquer
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class DescriberProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (var type : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(Describe.class))) {
            if (check(type)) {
                write(type);
            }
        }
        return true;
    }

    /** Signale sur sa ligne chaque champ que le describer ne saurait pas lire. */
    private boolean check(TypeElement type) {
        var valid = true;
        for (var field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
            // Le describer généré est du Java ordinaire : pas de setAccessible pour lire un champ privé.
            if (field.getModifiers().contains(Modifier.PRIVATE)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "a @Describe class cannot have private fields: the describer reads them directly", field);
                valid = false;
            }
        }
        return valid;
    }

    /** Écrit {@code <Type>_Describer}, dont {@code describe} lit chaque champ directement. */
    private void write(TypeElement type) {
        var name = type.getSimpleName();
        var pkg = processingEnv.getElementUtils().getPackageOf(type).getQualifiedName();
        try (var out = new PrintWriter(
                processingEnv.getFiler().createSourceFile(type.getQualifiedName() + "_Describer", type).openWriter())) {
            out.println("package " + pkg + ";");
            out.println();
            out.println("// Généré par DescriberProcessor, ne pas modifier.");
            out.println("public final class " + name + "_Describer {");
            out.println("    public static String describe(" + name + " object) {");
            out.print("        return \"" + name + "[");
            var first = true;
            for (var field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
                if (!first) {
                    out.print(", ");
                }
                first = false;
                var fieldName = field.getSimpleName();
                out.print(fieldName + "=");
                if (field.getAnnotation(Hidden.class) != null) {
                    out.print("***");
                } else {
                    out.print("\" + object." + fieldName + " + \"");
                }
            }
            out.println("]\";");
            out.println("    }");
            out.println("}");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
