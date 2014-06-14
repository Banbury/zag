package org.p2c2e.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("org.p2c2e.util.GlkMethod")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class GlkMethodProcessor extends AbstractProcessor {
	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		Messager m = processingEnv.getMessager();

		if (annotations.size() > 0) {
			try {
				JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
						"org.p2c2e.zing.Dispatch2");
				jfo.delete();

				BufferedWriter bw = new BufferedWriter(jfo.openWriter());
				writeln(bw, "package org.p2c2e.zing;");
				writeln(bw, "import java.lang.reflect.Method;");
				writeln(bw, "import java.util.HashMap;");

				writeln(bw, "public final class Dispatch2 {");
				writeln(bw,
						"private static HashMap<Integer, Method> METHODS = new HashMap<Integer, Method>(512);");
				writeln(bw,
						"public static Method getMethod(int selector) {	return METHODS.get(selector); }");
				writeln(bw,
						"static { try { createMethodList(); } catch (Exception ex) { ex.printStackTrace(); } }");

				writeln(bw,
						"private static void createMethodList() throws NoSuchMethodException {");

				for (Element e : roundEnv
						.getElementsAnnotatedWith(GlkMethod.class)) {
					ExecutableElement method = (ExecutableElement) e;

					GlkMethod annot = method.getAnnotation(GlkMethod.class);
					int id = annot.value();

					if (id < 1) {
						m.printMessage(Kind.ERROR,
								"Method number must be greater than zero.");
					}

					StringBuilder sb = new StringBuilder();
					for (VariableElement p : method.getParameters()) {
						sb.append(", ");
						sb.append(p.asType().toString());
						sb.append(".class");
					}

					writeln(bw, "METHODS.put(" + id + ", "
							+ method.getEnclosingElement().getSimpleName()
							+ ".class.getMethod(\"" + method.getSimpleName()
							+ "\"" + sb + ")); ");
				}

				writeln(bw, "}");
				writeln(bw, "}");
				bw.flush();
				bw.close();
			} catch (IOException ex) {
				m.printMessage(Kind.ERROR, ex.getMessage());
				return false;
			}

			return true;
		}

		return false;
	}

	private void writeln(BufferedWriter w, String line) throws IOException {
		w.append(line);
		w.newLine();
	}
}
