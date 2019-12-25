package com.allin.android.webkit.gradle

import com.android.SdkConstants
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class RegisterTransform extends Transform {

    static File fileContainsInitClass

    ArrayList<String> classList = new ArrayList<>()

    @Override
    String getName() {
        return Constants.TRANSFORM_NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation invocation) throws TransformException, InterruptedException, IOException {
        if (invocation == null) {
            return
        }
        Log.i('Start scan register info in jar file.')

        long startTime = System.currentTimeMillis()

        invocation.outputProvider.deleteAll()

        def inputs = invocation.inputs
        def outputProvider = invocation.outputProvider

        inputs.forEach { input ->
            def jarInputs = input.jarInputs
            def directoryInputs = input.directoryInputs

            jarInputs.forEach { jarInput ->
                handJarInput(this, jarInput, outputProvider)
            }

            directoryInputs.forEach { directoryInput ->
                handDirectoryInput(this, directoryInput, outputProvider)
            }
        }

        Log.i('Scan finish, current cost time ' + (System.currentTimeMillis() - startTime) + "ms")

        Log.i("classList: ${classList.join('\n')}")
        if (fileContainsInitClass) {
            Log.i("fileContainsInitClass: $fileContainsInitClass")

            def jarFile = fileContainsInitClass
            def optJar = new File(jarFile.getParent(), jarFile.name + ".opt")
            if (optJar.exists())
                optJar.delete()
            def file = new JarFile(jarFile)
            Enumeration enumeration = file.entries()
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar))

            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = file.getInputStream(jarEntry)
                jarOutputStream.putNextEntry(zipEntry)
                if (Constants.GENERATE_TO_CLASS_FILE_NAME == entryName) {

                    Log.i('Insert init code to class >> ' + entryName)

                    def classReader = new ClassReader(IOUtils.toByteArray(inputStream))
                    def classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)

                    classReader.accept(new GenClassVisitor(classList, classWriter), ClassReader.EXPAND_FRAMES)

                    def byteArr = classWriter.toByteArray()
                    jarOutputStream.write(byteArr)
                } else {
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                inputStream.close()
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            file.close()

            if (jarFile.exists()) {
                jarFile.delete()
            }
            optJar.renameTo(jarFile)
        }

        Log.i("Generate code finish, current cost time: " + (System.currentTimeMillis() - startTime) + "ms")
    }

    private static void handJarInput(RegisterTransform transform, JarInput jarInput, TransformOutputProvider outputProvider) {
        if (jarInput.file.absolutePath.endsWith(SdkConstants.DOT_JAR)) {
            def jarName = jarInput.name
            def md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
            if (jarName.endsWith(SdkConstants.DOT_JAR)) {
                jarName = jarName.substring(0, jarName.length() - 4)
            }

            def src = jarInput.file
            def dest = outputProvider.getContentLocation(
                    jarName + '_' + md5Name,
                    jarInput.contentTypes,
                    jarInput.scopes,
                    Format.JAR
            )

            // scan jar file to find classes

            if (src) {
                def file = new JarFile(src)
                Enumeration enumeration = file.entries()
                while (enumeration.hasMoreElements()) {
                    JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                    String entryName = jarEntry.getName()
                    Log.i("entryName: $entryName")
                    if (entryName.startsWith(Constants.AWEBKIT_CLASS_PACKAGE_NAME)) {
                        InputStream inputStream = file.getInputStream(jarEntry)
                        scanClass(transform, inputStream)
                        inputStream.close()
                    } else if (Constants.GENERATE_TO_CLASS_FILE_NAME == entryName) {
                        // mark this jar file contains RegisterCenter.class
                        // After the scan is complete, we will generate register code into this file
                        RegisterTransform.fileContainsInitClass = dest
                    }
                }
                file.close()
            }

            FileUtils.copyFile(src, dest)
        }
    }

    private static void handDirectoryInput(RegisterTransform transform, DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        def file = directoryInput.file
        def root = file.absolutePath
        if (!root.endsWith(File.separator)) {
            root += File.separator
        }

        if (file.isDirectory()) {
            file.eachFileRecurse { File classFile ->
                def path = classFile.absolutePath.replace(root, '')
                boolean leftSlash = File.separator == '/'
                if (!leftSlash) {
                    path = path.replaceAll("\\\\", "/")
                }
                def shouldProcessClass = path != null && path.startsWith(Constants.AWEBKIT_CLASS_PACKAGE_NAME)
                if (file.isFile() && shouldProcessClass) {
                    // scan
                    scanClass(transform, new FileInputStream(file))
                }
            }
        }

        def dest = outputProvider.getContentLocation(
                directoryInput.name,
                directoryInput.contentTypes,
                directoryInput.scopes,
                Format.DIRECTORY
        )

        FileUtils.copyDirectory(file, dest)
    }

    private static void scanClass(RegisterTransform transform, InputStream inputStream) {
        ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(cr, 0)
        ScanClassVisitor cv = new ScanClassVisitor(transform, Opcodes.ASM5, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        inputStream.close()
    }

    static class ScanClassVisitor extends ClassVisitor {

        final RegisterTransform transform

        ScanClassVisitor(RegisterTransform transform, int api, ClassVisitor cv) {
            super(api, cv)
            this.transform = transform
        }

        void visit(int version, int access, String name, String signature,
                   String superName, String[] interfaces) {
            Log.i("name: $name; interface: ${interfaces.join(',')}")
            super.visit(version, access, name, signature, superName, interfaces)
            if (interfaces.any { Constants.AWEBKIT_CORE_INTERFACES.contains(it) }) {
                transform.classList.add(name)
            }
        }
    }

    static class GenClassVisitor extends ClassVisitor implements Opcodes {
        private final ArrayList<String> classList

        @SuppressWarnings("UnnecessaryQualifiedReference")
        GenClassVisitor(ArrayList<String> classList, ClassWriter cw) {
            super(Opcodes.ASM6, cw)
            this.classList = classList
            generateField(cw)
        }

        private void generateField(ClassWriter cw) {
            def size = classList.size()

            def mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "getGeneratedClassesByAWebkit", "()[Ljava/lang/String;", null, null)

            AnnotationVisitor av0 = mv.visitAnnotation("Landroidx/annotation/Keep;", false)
            av0.visitEnd()

            mv.visitCode()
            Label l0 = new Label()
            mv.visitLabel(l0)
            mv.visitLineNumber(5, l0)

            // 5: represent Opcodes#ICONST_5
            if (size <= 5) {
                mv.visitInsn(size + 3)
            } else {
                mv.visitIntInsn(BIPUSH, classList.size())
            }

            mv.visitTypeInsn(ANEWARRAY, "java/lang/String")

            for (int i = 0; i < size; i++) {
                def cls = classList.get(i)
                mv.visitInsn(DUP)
                if (i <= 5) {
                    mv.visitInsn(i + 3)
                } else {
                    mv.visitIntInsn(BIPUSH, i)
                }
                mv.visitLdcInsn(cls)
                mv.visitInsn(AASTORE)
            }

            mv.visitInsn(ARETURN)
            mv.visitMaxs(4, 0)
            mv.visitEnd()
        }
    }
}