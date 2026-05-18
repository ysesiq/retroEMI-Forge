package com.rewindmc.retroemi;

import com.google.common.collect.Lists;
import dev.emi.emi.runtime.EmiLog;
import net.minecraftforge.fml.common.ModContainer;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class EmiModAnnotationScanner {

	public static List<Class<?>> scanForAnnotatedClasses(ModContainer container, ClassLoader loader, Class<? extends Annotation> aClass) {
		List<Class<?>> aClasses = Lists.newArrayList();
		String aName = "L" + aClass.getName().replace('.', '/') + ";";
		try {
			URL url = loader.getResource(container.getMod().getClass().getName().replace('.', '/') + ".class");
			if (url == null) return aClasses;
			if ("jar".equals(url.getProtocol())) {
				try (JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile()) {
					Enumeration<JarEntry> entries = jarFile.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						if (entry.getName().endsWith(".class")) {
							processClassBytes(jarFile.getInputStream(entry).readAllBytes(), aName, loader, aClasses);
						}
					}
				}
			} else if ("file".equals(url.getProtocol())) {
				File rootDir = new File(url.getPath().replace(container.getMod().getClass().getName().replace('.', '/') + ".class", ""));
				scanDir(rootDir, rootDir, loader, aName, aClasses);
			}
		} catch (Exception e) {
			EmiLog.error("Error scanning for annotated classes", e);
		}
		return aClasses;
	}

	private static void scanDir(File root, File dir, ClassLoader loader, String aName, List<Class<?>> out) {
		File[] files = dir.listFiles();
		if (files == null) return;
		for (File f : files) {
			if (f.isDirectory()) {
				scanDir(root, f, loader, aName, out);
			} else if (f.getName().endsWith(".class")) {
				try (InputStream is = new FileInputStream(f)) {
					processClassBytes(is.readAllBytes(), aName, loader, out);
				} catch (Exception ignored) {}
			}
		}
	}

	private static void processClassBytes(byte[] bytes, String aName, ClassLoader loader, List<Class<?>> out) {
		try {
			if (hasAnnotation(bytes, aName)) {
				String className = new ClassReader(bytes).getClassName().replace('/', '.');
				out.add(loader.loadClass(className));
			}
		} catch (Exception e) {
			EmiLog.warn("Failed to process class: " + e.getMessage());
		}
	}

	private static boolean hasAnnotation(byte[] bytes, String aName) {
		try {
			boolean[] found = {false};
			new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM5) {
				@Override
				public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
					if (aName.equals(desc)) {
						found[0] = true;
					}
					return null;
				}
			}, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
			return found[0];
		} catch (Exception e) {
			return false;
		}
	}
}
