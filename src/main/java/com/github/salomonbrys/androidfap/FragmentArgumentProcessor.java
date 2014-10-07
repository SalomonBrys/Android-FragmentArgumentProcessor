package com.github.salomonbrys.androidfap;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

/**
 * Annotation processor
 *
 * @author Salomon BRYS (salomon.brys@gmail.com)
 */
@SupportedAnnotationTypes({ "com.github.salomonbrys.androidfap.FragmentArguments", "com.github.salomonbrys.androidfap.FragmentArgument" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class FragmentArgumentProcessor extends AbstractProcessor {

	/**
	 * Use a weird trick to get the TypeMirror from the annotation
	 *
	 * @param annotation The annotation to get its type()
	 * @return The TypeMirror corresponding to the annotation's type() value
	 */
	private static TypeMirror getType(FragmentArgument annotation) {
	    try
	    {
	        annotation.type();
	    }
	    catch (MirroredTypeException mte)
	    {
	        return mte.getTypeMirror();
	    }
	    return null;
	}

	/**
	 * Search in a TypeElement, it's interfaces and parents for a specific type
	 *
	 * @param type The type to inspect
	 * @param search The cannonical name of the class to look for
	 * @return Whether the type is a child of the searched class or not
	 */
	private static boolean typeIsAssignableTo(final TypeElement type, final String search) {
		if (type.getQualifiedName().toString().equals(search))
			return true;

		for (TypeMirror itfType : type.getInterfaces()) {
			if (itfType instanceof DeclaredType) {
				Element itfElement = ((DeclaredType) itfType).asElement();
				if (itfElement instanceof TypeElement) {
					return typeIsAssignableTo((TypeElement) itfElement, search);
				}
			}
		}

		TypeMirror superType = type.getSuperclass();
		if (superType instanceof DeclaredType) {
			Element superElement = ((DeclaredType) superType).asElement();
			if (superElement instanceof TypeElement) {
				return typeIsAssignableTo((TypeElement) superElement, search);
			}
		}

		return false;
	}

	/**
	 * Computes the string to append to 'get' or 'set' to get a valid Bundle method name.
	 * For example, for the type int[], will return 'IntArray', which leads to the methods 'putIntArray' and 'getIntArray'
	 *
	 * @param argument The type to access in the bundle
	 * @return The string to append to 'get' or 'put'
	 */
	private static String getBundleAccessor(FragmentArgument argument) {
		TypeMirror argType = getType(argument);

		if (argType instanceof PrimitiveType) {
			return argType.toString().toUpperCase().charAt(0) + argType.toString().substring(1);
		}
		else if (argType instanceof DeclaredType) {
			Element argElement = ((DeclaredType) argType).asElement();
			if (argElement instanceof TypeElement) {
				TypeElement argTypeElement = (TypeElement) argElement;
				     if (typeIsAssignableTo(argTypeElement, "android.os.Bundle"))        { return "Bundle"; }
				else if (typeIsAssignableTo(argTypeElement, "java.lang.String"))         { return "String"; }
				else if (typeIsAssignableTo(argTypeElement, "java.lang.CharSequence"))   { return "CharSequence"; }
				else if (typeIsAssignableTo(argTypeElement, "android.util.SparseArray")) { return "SparseParcelableArray"; }
				else if (typeIsAssignableTo(argTypeElement, "android.os.Parcelable"))    { return "Parcelable"; }
				else if (typeIsAssignableTo(argTypeElement, "java.io.Serializable"))     { return "Serializable"; }
				else if (typeIsAssignableTo(argTypeElement, "android.os.IBinder"))       { return "Binder"; }
				return null;
			}
		}
		else if (argType instanceof ArrayType) {
			ArrayType argArrayType = (ArrayType) argType;
			TypeMirror compType = argArrayType.getComponentType();
			if (compType instanceof PrimitiveType) {
				return compType.toString().toUpperCase().charAt(0) + compType.toString().substring(1) + "Array";
			}
			else if (compType instanceof DeclaredType) {
				Element compElement = ((DeclaredType) compType).asElement();
				if (compElement instanceof TypeElement) {
					TypeElement compTypeElement = (TypeElement) compElement;
					     if (typeIsAssignableTo(compTypeElement, "java.lang.String"))         { return "StringArray"; }
					else if (typeIsAssignableTo(compTypeElement, "java.lang.CharSequence"))   { return "CharSequenceArray"; }
					else if (typeIsAssignableTo(compTypeElement, "android.os.Parcelable"))    { return "ParcelableArray"; }
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Generate a java source file for the corresponding annotated element
	 *
	 * @param elem The annotated element (should be the fragment type)
	 * @param arguments The arguments of the fragment
	 */
	private void generate(Element elem, FragmentArgument[] arguments) {
		if (!(elem instanceof TypeElement))
			return ;

		TypeElement typeElement = (TypeElement) elem;

		PackageElement pkg = (PackageElement) typeElement.getEnclosingElement();

		try {
			final String fragment = typeElement.getSimpleName().toString();
			final JavaFileObject jfo = this.processingEnv.getFiler().createSourceFile(typeElement.getQualifiedName() + "Arguments");
            final PrintWriter bw = new PrintWriter(jfo.openWriter());
			try {
				bw.println("package " + pkg.getQualifiedName() + ";");
				bw.println();
				bw.println();
				bw.println("@java.lang.SuppressWarnings(\"all\")");
				bw.println("class " + fragment + "Arguments {");
				bw.println();

				bw.println("	public static " + fragment + " new" + fragment + "(");
				String coma = "  ";
				for (FragmentArgument argument : arguments) {
					bw.println("		" + coma + getType(argument).toString() + " " + argument.name());
					coma = ", ";
				}

				bw.println("	) {");
				bw.println("		" + fragment + " fragment = new " + fragment + "();");
				bw.println("		android.os.Bundle __args_bundle = new android.os.Bundle();");

				for (FragmentArgument argument : arguments) {
					String put = getBundleAccessor(argument);
					if (put != null) {
						bw.println("		__args_bundle.put" + put + "(\"" + argument.name() + "\", " + argument.name() + ");");
					}
					else {
						bw.println("		// Skipping unknown " + getType(argument).toString() + " " + argument.name());
					}
				}

				bw.println("		fragment.setArguments(__args_bundle);");
				bw.println("		return fragment;");
				bw.println("	}");
				bw.println();

				bw.println("	private android.os.Bundle _args;");
				bw.println();
				bw.println("	public " + fragment + "Arguments(" + fragment + " fragment) {");
				bw.println("		this._args = fragment.getArguments();");
				bw.println("	}");
				bw.println();

				for (FragmentArgument argument : arguments) {
					String get = getBundleAccessor(argument);
					if (get != null) {
						String type = getType(argument).toString();
						bw.println("	public " + type + " " + argument.name() + "() {");
						bw.println("		return (" + type + ") this._args.get" + get + "(\"" + argument.name() + "\");");
						bw.println("	}");
						bw.println();
					}
					else {
						bw.println("	// Skipping unknown " + getType(argument).toString() + " " + argument.name());
					}
				}

				bw.println("}");
			}
            finally {
                bw.close();
            }
        }
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (Element elem : roundEnv.getElementsAnnotatedWith(FragmentArguments.class)) {
			FragmentArguments arguments = elem.getAnnotation(FragmentArguments.class);
			generate(elem, arguments.value());
		}
		for (Element elem : roundEnv.getElementsAnnotatedWith(FragmentArgument.class)) {
			FragmentArgument argument = elem.getAnnotation(FragmentArgument.class);
			generate(elem, new FragmentArgument[] { argument });
		}
		return true;
	}

}
