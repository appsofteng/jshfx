package dev.jshfx.jx.tools;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.lang.model.element.Name;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import com.sun.source.doctree.BlockTagTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTreePathScanner;
import com.sun.source.util.DocTrees;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;

public class JavaSourceResolver {

    private JavaCompiler compiler;
    private StandardJavaFileManager fileManager;
    private ResourceBundle resourceBundle;

    public JavaSourceResolver() {
        compiler = ToolProvider.getSystemJavaCompiler();
        fileManager = compiler.getStandardFileManager(null, null, null);
    }

    public JavaSourceResolver setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        return this;
    }

    public JavaSourceResolver setSourcePaths(Collection<Path> sourcePaths) {
        try {
            fileManager.setLocationFromPaths(StandardLocation.SOURCE_PATH, sourcePaths);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    public void close() {
        try {
            fileManager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHtmlDoc(Signature signature) {
        StringBuilder htmlBuilder = new StringBuilder();

        try {
            JavaFileObject jfo = fileManager.getJavaFileForInput(StandardLocation.SOURCE_PATH,
                    signature.getTopTypeFullName(), JavaFileObject.Kind.SOURCE);
            JavacTask task = (JavacTask) compiler.getTask(null, null, null, null, null, List.of(jfo));

            DocTrees docTrees = DocTrees.instance(task);

            Iterable<? extends CompilationUnitTree> unitTrees = task.parse();
            SignatureTreePathScanner scanner = new SignatureTreePathScanner();
            TreePath treePath = scanner.scan(unitTrees.iterator().next(), signature);

            DocCommentTree docCommentTree = docTrees.getDocCommentTree(treePath);
            HtmlDocTreePathScanner docScanner = new HtmlDocTreePathScanner();

            docScanner.scan(new DocTreePath(treePath, docCommentTree), htmlBuilder);

        } catch (IOException e) {

            e.printStackTrace();
        }

        return htmlBuilder.toString();
    }

    private static class Result extends Error {

        private static final long serialVersionUID = 1L;

        private final TreePath treePath;

        public Result(TreePath treePath) {
            this.treePath = treePath;
        }

        public TreePath getTreePath() {
            return treePath;
        }
    }

    private static class SignatureTreePathScanner extends TreePathScanner<TreePath, Signature> {

        private String namespace;
        private List<String> imports;
        private static final List<String> PRIMITIVES = List.of("byte", "char", "double", "float", "int", "long",
                "short");

        private String getNamespace(Name name) {
            return namespace + "." + name;
        }

        private List<String> getFullNames(String name) {

            List<String> names = List.of(name);

            if (PRIMITIVES.stream().anyMatch(p -> name.matches(p + "(\\[\\]|\\.\\.\\.)?"))) {
                return names;
            }
            
            var raw = name.replaceAll("<.*>", "");

            names = imports.stream().filter(i -> i.endsWith("." + raw)).collect(Collectors.toList());

            if (names.isEmpty()) {
                names = imports.stream().filter(i -> i.endsWith("*"))
                        .map(i -> i.substring(0, i.lastIndexOf("*")) + raw).collect(Collectors.toList());
            }
            
            if (names.isEmpty()) {
                names = List.of(name);
            }
            

            return names;
        }

        @Override
        public TreePath visitCompilationUnit(CompilationUnitTree node, Signature signature) {
            namespace = node.getPackageName().toString();
            imports = node.getImports().stream().map(i -> i.getQualifiedIdentifier().toString())
                    .collect(Collectors.toCollection(() -> new ArrayList<>()));
            imports.add("java.lang.*");

            return scan(node.getTypeDecls(), signature);
        }

        @Override
        public TreePath visitClass(ClassTree node, Signature signature) {

            TreePath path = null;

            if (signature.getKind() == Signature.Kind.TYPE
                    && signature.getFullName().equals(getNamespace(node.getSimpleName()))) {

                path = getCurrentPath();
            } else {
                try {
                    namespace = getNamespace(node.getSimpleName());
                    path = scan(node.getMembers(), signature);
                } catch (Result result) {
                    path = result.getTreePath();
                } finally {
                    int i = namespace.lastIndexOf(".");

                    namespace = namespace.substring(0, i);
                }
            }

            return path;
        }

        @Override
        public TreePath visitVariable(VariableTree node, Signature signature) {

            if ((signature.getKind() == Signature.Kind.FIELD || signature.getKind() == Signature.Kind.ENUM_CONSTANT)
                    && signature.getFullName().equals(getNamespace(node.getName()))) {

                throw new Result(getCurrentPath());
            } else {
                return null;
            }
        }

        public TreePath visitMethod(MethodTree node, Signature signature) {
            if (signature.getKind() == Signature.Kind.METHOD
                    && signature.getFullName().equals(getNamespace(node.getName()))
                    && signature.getMethodParameterTypes().size() == node.getParameters().size()) {

                boolean parameterTypeMatch = true;
                for (int i = 0; i < node.getParameters().size(); i++) {
                    List<String> names = getFullNames(node.getParameters().get(i).getType().toString());
                    if (!names.contains(signature.getMethodParameterTypes().get(i))) {
                        parameterTypeMatch = false;
                        break;
                    }
                }
                if (parameterTypeMatch) {
                    throw new Result(getCurrentPath());
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    private class HtmlDocTreePathScanner extends DocTreePathScanner<Void, StringBuilder> {

        private List<String> blockPassed = new ArrayList<>();

        public Void visitText(TextTree node, StringBuilder htmlBuilder) {
            htmlBuilder.append(node.getBody()).append("\n");
            return null;
        }

        public Void visitSee(SeeTree node, StringBuilder htmlBuilder) {

            appendBlockTagName(node, htmlBuilder);
            appendBlockTag(htmlBuilder, node.getReference());

            return null;
        }

        public Void visitSince(SinceTree node, StringBuilder htmlBuilder) {

            appendBlockTagName(node, htmlBuilder);
            appendBlockTag(htmlBuilder, node.getBody());

            return null;
        }

        private void appendBlockTagName(BlockTagTree tag, StringBuilder htmlBuilder) {

            if (blockPassed.isEmpty()) {
                htmlBuilder.append("<br><br>\n\n");
            }

            if (!blockPassed.contains(tag.getTagName())) {
                blockPassed.add(tag.getTagName());
                htmlBuilder.append(
                        String.format("<strong>%s:</strong><br>\n", resourceBundle.getString(tag.getTagName())));
            }
        }

        private void appendBlockTag(StringBuilder htmlBuilder, Object value) {
            htmlBuilder.append("<p>").append("\n");
            htmlBuilder.append("<code>").append(value).append("</code>").append("\n");
            htmlBuilder.append("</p>").append("\n");
        }
    }
}
