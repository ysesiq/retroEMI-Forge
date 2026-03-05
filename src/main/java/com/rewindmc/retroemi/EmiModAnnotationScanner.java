package com.rewindmc.retroemi;

import com.google.common.collect.Lists;
import cpw.mods.fml.common.ModContainer;
import dev.emi.emi.runtime.EmiLog;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class EmiModAnnotationScanner {

	public EmiModAnnotationScanner() {
	}

	public static List<Class<?>> scanForAnnotatedClasses(ModContainer modContainer, ClassLoader classLoader, Class<? extends Annotation> annotationClass) {
		List<Class<?>> annotatedClasses = Lists.newArrayList();
		String annotationName = "L" + annotationClass.getName().replace('.', '/') + ";";

		try {
			String modClassName = modContainer.getMod().getClass().getName();
			String modClassPath = modClassName.replace('.', '/') + ".class";
			URL classUrl = classLoader.getResource(modClassPath);

			if (classUrl != null) {
				String protocol = classUrl.getProtocol();

				if ("jar".equals(protocol)) {
					JarURLConnection jarConn = (JarURLConnection) classUrl.openConnection();
					JarFile jarFile = jarConn.getJarFile();

					Enumeration<JarEntry> entries = jarFile.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						if (entry.getName().endsWith(".class") && !entry.isDirectory()) {
							try (InputStream is = jarFile.getInputStream(entry)) {
								byte[] classBytes = readAllBytes(is);
								if (classBytes.length > 0 && hasAnnotation(classBytes, annotationName)) {
									String className = entry.getName().replace('/', '.').replace(".class", "");
									Class<?> clazz = classLoader.loadClass(className);
									annotatedClasses.add(clazz);
								}
							} catch (Exception e) {
								EmiLog.warn("Failed to process class " + entry.getName() + ": " + e.getMessage());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error scanning for annotated classes", e);
		}

		return annotatedClasses;
	}

	private static byte[] readAllBytes(InputStream inputStream) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[1024];
		while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}
		return buffer.toByteArray();
	}

	private static boolean hasAnnotation(byte[] classBytes, final String annotationName) {
		try {
			final boolean[] hasAnnotation = {false};

			ClassReader classReader = new ClassReader(classBytes);
			classReader.accept(new ClassVisitor(Opcodes.ASM5) { // 升级到 ASM5
				@Override
				public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
					if (annotationName.equals(desc)) {
						hasAnnotation[0] = true;
					}
					return super.visitAnnotation(desc, visible);
				}
			}, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

			return hasAnnotation[0];
		} catch (IllegalArgumentException e) {
			EmiLog.warn("Skipping incompatible class file: " + e.getMessage());
			return false;
		} catch (Exception e) {
			EmiLog.warn("Error processing class with ASM: " + e.getMessage());
			return false;
		}
	}
}
