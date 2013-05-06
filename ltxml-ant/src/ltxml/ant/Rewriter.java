package ltxml.ant;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Rewriter {

    public static void main(String[] args) throws Exception {

        File dest = new File(args[0]);
        File original = new File(args[0]);
        File tempFile = File.createTempFile(original.getName(), ".tmp", original.getParentFile());
        original.renameTo(tempFile);

        ZipFile zipFile = new ZipFile(tempFile);
        ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(dest)));

        final String[] ns = new String[2];

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();

            System.out.println("Processing " + entry.getName() + " ... ");

            if (entry.isDirectory()) {
                ZipEntry copyEntry = new ZipEntry(entry.getName());
                copyEntry.setTime(entry.getTime());
                zipOut.putNextEntry(copyEntry);
            } else if (entry.getName().endsWith(".class")) {

                InputStream inputStream = zipFile.getInputStream(entry);
                int size = (int) entry.getSize();
                byte[] bytes = new byte[size];
                int count = 0;
                int totalRead = 0;
                while((count = inputStream.read(bytes, totalRead, bytes.length - totalRead)) > 0) {
                    totalRead += count;
                }
                inputStream.close();

                if (totalRead != size)
                    throw new IOException("Invalid read on " + entry.getName() + ", expected " + size + " bytes, got " + totalRead);

                ClassReader reader = new ClassReader(bytes);
                ClassWriter writer = new ClassWriter(reader, 0);
                ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4, writer) {

                    @Override
                    public AnnotationVisitor visitAnnotation(String s, boolean b) {
                        System.out.println("  Class Annotation: " + s + " " + b);
                        if (s.startsWith("Lltxml"))
                            return super.visitAnnotation(s, b);
                        else if (s.equals("Ljavax/xml/bind/annotation/XmlSchema;")) {
                            // todo: magic
                            return new AnnotationVisitor(Opcodes.ASM4) {
                                @Override
                                public void visit(String name, Object value) {
                                    if (name.equals("namespace")) {
                                        ns[0] = (String) value;
                                        ns[1] = entry.getName();
                                    }
                                }
                            };
                        }
                        return null;
                    }

                    @Override
                    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                        FieldVisitor rv = super.visitField(access, name, desc, signature, value);
                        FieldVisitor myv = new FieldVisitor(Opcodes.ASM4, rv) {
                            @Override
                            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                                System.out.println("  Field annotation: " + desc);
                                if (desc.startsWith("Lltxml"))
                                    return super.visitAnnotation(desc, visible);
                                else if (desc.equals("Ljavax/xml/bind/annotation/XmlElement;")) {
                                    return super.visitAnnotation("Lltxml/LtXmlElement;", visible);
                                }
                                return null;
                            }
                        };
                        return myv;
                    }
                };
                reader.accept(visitor, 0);
                byte[] bytes_out = writer.toByteArray();

                ZipEntry copyEntry = new ZipEntry(entry.getName());
                copyEntry.setTime(entry.getTime());
                copyEntry.setComment(entry.getComment());
                zipOut.putNextEntry(copyEntry);
                zipOut.write(bytes_out);
                zipOut.flush();
            } else {
                ZipEntry copyEntry = new ZipEntry(entry.getName());
                copyEntry.setTime(entry.getTime());
                copyEntry.setComment(entry.getComment());
                zipOut.putNextEntry(copyEntry);

                InputStream inputStream = zipFile.getInputStream(entry);
                int size = (int) entry.getSize();
                byte[] bytes = new byte[size];
                if (inputStream.read(bytes) != size) {
                    throw new IOException("Invalid read on " + entry.getName());
                }
                inputStream.close();
                zipOut.write(bytes);
                zipOut.flush();
            }

        }

        if (ns[0] != null) {
            String pkg = new File(ns[1]).getParentFile().getPath();
            System.out.println("Namespace: " + ns[0] + ", package: " + pkg);
            ClassWriter cw = new ClassWriter(0);
            cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, pkg + "/LtXmlNamespace", null, "java/lang/Object", new String[]{});
            AnnotationVisitor av = cw.visitAnnotation("Lltxml/LtXmlSchema;", true);
            av.visit("namespace", ns[0]);
            av.visitEnd();
            cw.visitEnd();
            ZipEntry nsEntry = new ZipEntry(pkg + "/LtXmlNamespace.class");
            zipOut.putNextEntry(nsEntry);
            zipOut.write(cw.toByteArray());
            zipOut.flush();
        }

        zipOut.close();
        tempFile.delete();
    }

}
