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

import com.sun.source.doctree.AuthorTree;
import com.sun.source.doctree.BlockTagTree;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.doctree.UnknownInlineTagTree;
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

            htmlBuilder.append("<p><strong>").append(signature.toString()).append("</strong></p>").append("\n");
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
                names = imports.stream().filter(i -> i.endsWith("*")).map(i -> i.substring(0, i.lastIndexOf("*")) + raw)
                        .collect(Collectors.toList());
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

            TreePath path = null;

            try {
                path = scan(node.getTypeDecls(), signature);
            } catch (Result result) {
                path = result.getTreePath();
            }

            return path;
        }

        @Override
        public TreePath visitClass(ClassTree node, Signature signature) {

            TreePath path = null;

            if (signature.getKind() == Signature.Kind.TYPE
                    && signature.getFullName().equals(getNamespace(node.getSimpleName()))) {

                throw new Result(getCurrentPath());
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

        private static final List<DocTree.Kind> INLINE_TAGS = List.of(DocTree.Kind.CODE, DocTree.Kind.LINK,
                DocTree.Kind.LINK_PLAIN, DocTree.Kind.UNKNOWN_INLINE_TAG);
        private List<String> blockPassed = new ArrayList<>();

        public Void visitDocComment(DocCommentTree node, StringBuilder htmlBuilder) {

            processInlineTag(node.getFullBody(), htmlBuilder);

            htmlBuilder.append("<br>\n");
            scan(node.getBlockTags(), htmlBuilder);

            htmlBuilder.append("<br>");

            return null;
        }

        public Void visitAuthor(AuthorTree node, StringBuilder htmlBuilder) {

            appendBlockTagName(node, htmlBuilder);
            appendBlockTag(node.getName(), htmlBuilder);

            return null;
        }

        public Void visitDeprecated(DeprecatedTree node, StringBuilder htmlBuilder) {

            appendBlockTagName(node, htmlBuilder);
            appendBlockTag(node.getBody(), htmlBuilder);

            return null;
        }

        public Void visitLink(LinkTree node, StringBuilder htmlBuilder) {
            var label = node.getLabel();
            var reference = node.getReference().getSignature();

            htmlBuilder.append("<code>");

            if (label.isEmpty()) {
                htmlBuilder.append(String.format(" <a href=\"%s\">%s</a> ", reference, reference.replace("#", ".")));
            } else {
                htmlBuilder.append(String.format(" <a href=\"%s\">%s</a> ", reference, label));
            }

            htmlBuilder.append("</code>");

            return null;
        }

        public Void visitLiteral(LiteralTree node, StringBuilder htmlBuilder) {
            htmlBuilder.append(String.format(" <code>%s</code> ", node.getBody()));

            return null;
        }

        public Void visitParam(ParamTree node, StringBuilder htmlBuilder) {

            appendBlockTagName(node, htmlBuilder);
            var name = node.getName().getName();
            var descBuilder = new StringBuilder();
            processInlineTag(node.getDescription(), descBuilder);
            appendBlockTag(name + " - " + descBuilder, htmlBuilder);

            return null;
        }

        public Void visitReturn(ReturnTree node, StringBuilder htmlBuilder) {
            appendBlockTagName(node, htmlBuilder);
            processInlineTag(node.getDescription(), htmlBuilder);
            htmlBuilder.append("<br>").append("\n");

            return null;
        }

        public Void visitSee(SeeTree node, StringBuilder htmlBuilder) {

            appendBlockTagName(node, htmlBuilder);
            var reference = getReference(
                    node.getReference().stream().map(Object::toString).collect(Collectors.joining()).strip());
            reference = String.format("<a href=\"%s\">%s</a>", reference, reference);
            appendBlockTagCode(reference, htmlBuilder);

            return null;
        }

        public Void visitSince(SinceTree node, StringBuilder htmlBuilder) {

            appendBlockTagName(node, htmlBuilder);
            appendBlockTagCode(node.getBody(), htmlBuilder);

            return null;
        }

        public Void visitThrows(ThrowsTree node, StringBuilder htmlBuilder) {

            appendBlockTagName(node, htmlBuilder);
            var reference = node.getExceptionName().getSignature();
            var descBuilder = new StringBuilder();
            processInlineTag(node.getDescription(), descBuilder);
            htmlBuilder.append(String.format("<code><a href=\"%s\">%s</a></code> - %s", reference,
                    reference.replace("#", "."), descBuilder)).append("<br>");

            return null;
        }

        public Void visitUnknownBlockTag(UnknownBlockTagTree node, StringBuilder htmlBuilder) {

            appendBlockTagName(node, htmlBuilder);
            processInlineTag(node.getContent(), htmlBuilder);
            htmlBuilder.append("<br>").append("\n");

            return null;
        }

        public Void visitUnknownInlineTag(UnknownInlineTagTree node, StringBuilder htmlBuilder) {
            processInlineTag(node.getContent(), htmlBuilder);
            
            return null;
        }

        private String getReference(Object obj) {
            return obj.toString().replace("#", ".");
        }

        private void processInlineTag(List<? extends DocTree> docTrees, StringBuilder htmlBuilder) {

            docTrees.forEach(t -> {

                if (INLINE_TAGS.contains(t.getKind())) {
                    scan(t, htmlBuilder);
                } else {
                    htmlBuilder.append(t);
                }
            });
        }

        private void appendBlockTagName(BlockTagTree tag, StringBuilder htmlBuilder) {

            if (!blockPassed.contains(tag.getTagName())) {
                blockPassed.add(tag.getTagName());
                htmlBuilder.append("<br>").append(
                        String.format("<strong>%s:</strong><br>\n", resourceBundle.getString(tag.getTagName())));
            }
        }

        private void appendBlockTag(Object value, StringBuilder htmlBuilder) {
            htmlBuilder.append(value).append("<br>").append("\n");
        }

        private void appendBlockTagCode(Object value, StringBuilder htmlBuilder) {
            htmlBuilder.append("<code>").append(value).append("</code>").append("<br>").append("\n");
        }
    }
}
