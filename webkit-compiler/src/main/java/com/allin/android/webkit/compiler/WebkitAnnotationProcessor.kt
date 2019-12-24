package com.allin.android.webkit.compiler

import androidx.lifecycle.Lifecycle
import com.allin.android.webkit.annotations.JavascriptApi
import com.allin.android.webkit.annotations.JavascriptNamespace
import com.allin.android.webkit.api.*
import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import com.sun.source.tree.ClassTree
import com.sun.source.tree.TreeVisitor
import com.sun.source.util.SimpleTreeVisitor
import com.sun.source.util.Trees
import com.sun.tools.javac.tree.JCTree
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementVisitor
import javax.lang.model.element.Modifier
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Types
import javax.tools.Diagnostic

private val CLASS_NAME_ANDROIDX_NULLABLE = ClassName.get("androidx.annotation", "Nullable")
private val CLASS_NAME_ANDROIDX_NONNULL = ClassName.get("androidx.annotation", "NonNull")
private val CLASS_NAME_ANDROIDX_KEEP = ClassName.get("androidx.annotation", "Keep")

@AutoService(value = [Processor::class])
class WebkitAnnotationProcessor : AbstractProcessor() {

    private var typeUtils: Types? = null
    private var filer: Filer? = null
    private var trees: Trees? = null
    private var messager: Messager? = null

    override fun init(env: ProcessingEnvironment?) {
        super.init(env)
        typeUtils = env?.typeUtils
        filer = env?.filer
        messager = env?.messager

        trees = runCatching {
            Trees.instance(processingEnv)
        }.getOrElse {

            runCatching {
                var trees: Trees? = null
                for (field in processingEnv.javaClass.declaredFields) {
                    if (field.name == "delegate" || field.name == "processingEnv") {
                        field.isAccessible = true
                        val javacEnv = field[processingEnv] as ProcessingEnvironment
                        trees = Trees.instance(javacEnv)
                        break
                    }
                }
                trees
            }.getOrNull()
        }
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return getSupportedAnnotations().map { it.canonicalName }.toMutableSet()
    }

    private fun getSupportedAnnotations(): Set<Class<out Annotation>> {
        return linkedSetOf(
            JavascriptNamespace::class.java
        )
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.RELEASE_8
    }

    override fun process(elements: MutableSet<out TypeElement>?, env: RoundEnvironment?): Boolean {
        if (env == null) {
            return false
        }

        env.getElementsAnnotatedWith(JavascriptNamespace::class.java).forEach { element ->

            val javascriptNamespace = element.getAnnotation(JavascriptNamespace::class.java)
            val className = element.simpleName.toString()
            val packageName =
                (element.enclosingElement as PackageElement).qualifiedName.toString()

            val generatedPackageName = GENERATED_PACKAGE_NAME
            val generatedClassName = "$AWEBKIT\$$className\$${System.nanoTime()}$SUFFIX_JS_COLLECTOR"
            JavaFile.builder(
                generatedPackageName,
                TypeSpec.classBuilder(generatedClassName)
                    .addSuperinterface(JavascriptApiCollector::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(CLASS_NAME_ANDROIDX_KEEP)
                    .addField(FieldSpec.builder(ClassName.get(packageName, className), "_INSTANCE", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new \$T()", ClassName.get(packageName, className))
                        .build())
                    .addMethods(
                        JavascriptApiCollector::class.java.declaredMethods.map { method ->
                            MethodSpec.methodBuilder(method.name)
                                .addAnnotation(CLASS_NAME_ANDROIDX_KEEP)
                                .addAnnotation(Override::class.java)
                                .addModifiers(Modifier.PUBLIC)
                                .addParameters(
                                    method.parameters.map { parameter ->
                                        ParameterSpec
                                            .builder(parameter.parameterizedType, parameter.name)
                                            .addAnnotation(CLASS_NAME_ANDROIDX_NONNULL)
                                            .addModifiers(Modifier.FINAL)
                                            .build()
                                    }
                                )
                                .returns(method.returnType)
                                .addCode(CodeBlock.builder()
                                    .also { builder ->
                                        when (method.name) {
                                            "collectTo" -> {
                                                builder.beginControlFlow(
                                                    "\$T.functionInvokerMappersOf(_INSTANCE, new \$T()",
                                                    ClassName.get(
                                                        "com.allin.android.webkit.internal",
                                                        "MethodChecker"
                                                    ),
                                                    ClassName.get(FunctionInvokerMappersSupplier::class.java)
                                                )
                                                builder.add("@Override\n")
                                                builder.beginControlFlow(
                                                    "public void get(@\$T Map<KFunction<?>, ? extends \$T> mapper)",
                                                    CLASS_NAME_ANDROIDX_NONNULL,
                                                    ClassName.get(NativeApiInvoker::class.java)
                                                )
                                                builder.addStatement("arg0.putAll(mapper)")
                                                builder.endControlFlow()
                                                builder.endControlFlow(")")

                                            }
                                            "namespace" -> {
                                                builder.addStatement("return \"${javascriptNamespace.namespace}\"")
                                            }
                                            else -> throw IllegalStateException("unKnow method name: ${method.name}")
                                        }
                                    }
                                    .build())
                                .build()
                        }
                    )
                    .build()
            ).addFileComment("Generated code from AWebkit. Do not modify!").build()
                .writeTo(filer)


            // lifecycle

            var implementedDefaultLifecycleObserver = false
            val tree = trees?.getTree(element) as? JCTree
            tree?.accept(object : JCTree.Visitor() {
                override fun visitClassDef(cls: JCTree.JCClassDecl?) {
                    implementedDefaultLifecycleObserver =
                        cls?.implementing?.any {
                            it.type.toString() == "androidx.lifecycle.LifecycleObserver"
                        } == true
                }
            })
            if (implementedDefaultLifecycleObserver) {
                val parameterSpec = ParameterSpec.builder(Lifecycle::class.java, "lifecycle")
                    .addAnnotation(CLASS_NAME_ANDROIDX_NONNULL)
                    .build()

                val methodSpec = MethodSpec.methodBuilder("registerWith")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.VOID)
                    .addParameter(parameterSpec)
                    .addCode(CodeBlock.builder()
                        .addStatement("lifecycle.addObserver(_INSTANCE)")
                        .build())
                    .build()

                val lifecycleClassName = "$AWEBKIT\$$className\$${System.nanoTime()}$SUFFIX_LIFECYCLE_REGISTRANT"
                val typeSpec = TypeSpec
                    .classBuilder(lifecycleClassName)
                    .addField(FieldSpec.builder(ClassName.get(packageName, className), "_INSTANCE", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new \$T()", ClassName.get(packageName, className))
                        .build())
                    .addSuperinterface(LifecycleRegistrant::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(methodSpec)
                    .build()

                JavaFile.builder(GENERATED_PACKAGE_NAME, typeSpec)
                    .addFileComment("Generated code from AWebkit. Do not modify!")
                    .build()
                    .writeTo(filer)
            }
        }
        return false
    }

    private fun printErrorMessage(message: String) {
        messager?.printMessage(Diagnostic.Kind.ERROR, message)
    }
}