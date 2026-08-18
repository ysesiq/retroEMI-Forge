package com.rewindmc.retroemi;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import dev.emi.emi.runtime.EmiLog;
import cpw.mods.fml.common.ModContainer;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class EmiModAnnotationScanner {

	private static final Pattern CLASS_FILE = Pattern.compile("[^\\s$]+(\\$[^\\s]+)?\\.class$");

	public static List<Class<?>> scanForAnnotatedClasses(ModContainer container, ClassLoader loader, Class<? extends Annotation> aClass) {
		List<Class<?>> aClasses = Lists.newArrayList();
		String modClassName = container.getMod().getClass().getName();
		String aName = "L" + aClass.getName().replace('.', '/') + ";";
		try {
			URL url = loader.getResource(modClassName.replace('.', '/') + ".class");
			if (url == null) return aClasses;
			if ("jar".equals(url.getProtocol())) {
				try (JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile()) {
					jarFile.stream()
						.map(e -> e.getName())
						.filter(EmiModAnnotationScanner::isClassFile)
						.forEach(name -> scanJarEntry(jarFile, name, aName, loader, aClasses));
				}
			} else if ("file".equals(url.getProtocol())) {
				scanDir(classRoot(modClassName, Paths.get(url.toURI())), aName, loader, aClasses);
			}
		} catch (Exception e) {
			EmiLog.error("Error scanning for annotated classes", e);
		}
		return aClasses;
	}

	private static void scanJarEntry(JarFile jarFile, String name, String aName, ClassLoader loader, List<Class<?>> out) {
		try (InputStream is = jarFile.getInputStream(jarFile.getEntry(name))) {
			processClass(is, aName, loader, out);
		} catch (Exception ignored) {
		}
	}

	private static void scanDir(Path root, String aName, ClassLoader loader, List<Class<?>> out) {
		try (Stream<Path> paths = Files.walk(root)) {
			paths.filter(p -> isClassFile(p.getFileName().toString()))
				.forEach(p -> {
					try (InputStream is = Files.newInputStream(p)) {
						processClass(is, aName, loader, out);
					} catch (Exception ignored) {
					}
				});
		} catch (Exception ignored) {
		}
	}

	private static Path classRoot(String modClassName, Path classFile) {
		Path root = classFile;
		for (int i = 0; i < modClassName.split("\\.").length - 1; i++) {
			root = root.getParent();
		}
		return root;
	}

	private static boolean isClassFile(String name) {
		return CLASS_FILE.matcher(name).matches() && !name.contains("__MACOSX") && !name.contains("module-info");
	}

	private static void processClass(InputStream is, String aName, ClassLoader loader, List<Class<?>> out) {
		try {
			boolean[] found = {false};
			String[] name = {null};
			new ClassReader(is).accept(new ClassVisitor(Opcodes.ASM5) {
				@Override
				public void visit(int version, int access, String n, String signature, String superName, String[] interfaces) {
					name[0] = n.replace('/', '.');
				}

				@Override
				public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
					found[0] |= aName.equals(desc);
					return null;
				}
			}, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
			if (found[0]) out.add(loader.loadClass(name[0]));
		} catch (Exception e) {
			EmiLog.warn("Failed to process class: " + e.getMessage());
		}
	}
}
