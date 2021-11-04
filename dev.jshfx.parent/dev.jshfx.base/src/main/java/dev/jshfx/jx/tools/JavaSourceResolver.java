package dev.jshfx.jx.tools;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
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
    private Function<String, String> resourceBundle;

    public JavaSourceResolver() {
        compiler = ToolProvider.getSystemJavaCompiler();
        fileManager = compiler.getStandardFileManager(null, null, null);
    }

    public JavaSourceResolver setResourceBundle(Function<String, String> resourceBundle) {
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

    public HtmlDoc getHtmlDoc(Signature signature) {
        StringBuilder htmlBuilder = new StringBuilder("");
        HtmlDoc htmlDoc = null;

        try {
            JavaFileObject jfo = fileManager.getJavaFileForInput(StandardLocation.SOURCE_PATH,
                    signature.getTopTypeFullName(), JavaFileObject.Kind.SOURCE);
            if (jfo != null) {
                JavacTask task = (JavacTask) compiler.getTask(null, null, null, null, null, List.of(jfo));

                DocTrees docTrees = DocTrees.instance(task);

                Iterable<? extends CompilationUnitTree> unitTrees = task.parse();
                SignatureTreePathScanner scanner = new SignatureTreePathScanner();
                TreePath treePath = scanner.scan(unitTrees.iterator().next(), signature);

                DocCommentTree docCommentTree = docTrees.getDocCommentTree(treePath);
                HtmlDocTreePathScanner docScanner = new HtmlDocTreePathScanner();

                htmlBuilder.append("<p><strong>").append(signature.toString()).append("</strong></p>").append("\n");
                docScanner.scan(new DocTreePath(treePath, docCommentTree), htmlBuilder);

                htmlDoc = new HtmlDoc(signature, scanner.getPackageName(), scanner.getImports(),
                        htmlBuilder.toString());
            }

        } catch (IOException e) {

            e.printStackTrace();
        }

        return htmlDoc;
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
        private String packageName;
        private List<String> imports;
        private static final List<String> PRIMITIVES = List.of("byte", "char", "double", "float", "int", "long",
                "short");

        private String getNamespace(Name name) {
            return namespace + "." + name;
        }

        public String getPackageName() {
            return packageName;
        }

        public List<String> getImports() {
            return imports;
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
                        .collect(Collectors.toCollection(() -> new ArrayList<>()));
                names.add(packageName + "." + raw);
                names.add(raw);
            }

            return names;
        }

        @Override
        public TreePath visitCompilationUnit(CompilationUnitTree node, Signature signature) {
            packageName = node.getPackageName().toString();
            namespace = packageName;
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

            lineEnd(htmlBuilder);
            scan(node.getBlockTags(), htmlBuilder);

            lineEnd(htmlBuilder);

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
            var label = processInlineTag(node.getLabel());
            var reference = node.getReference().getSignature();

            if (node.getKind() == DocTree.Kind.LINK) {
                code(getLink(reference, label), htmlBuilder);
            } else {
                htmlBuilder.append(getLink(reference, label));
            }

            return null;
        }

        public Void visitLiteral(LiteralTree node, StringBuilder htmlBuilder) {
            code(node.getBody(), htmlBuilder);

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
            lineEnd(htmlBuilder);

            return null;
        }

        public Void visitSee(SeeTree node, StringBuilder htmlBuilder) {

            appendBlockTagName(node, htmlBuilder);
            var reference = processInlineTag(node.getReference());
            reference = getLink(reference);
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
            var desc = processInlineTag(node.getDescription());
            appendBlockTag(getLink(reference) + " - " + desc, htmlBuilder);

            return null;
        }

        public Void visitUnknownBlockTag(UnknownBlockTagTree node, StringBuilder htmlBuilder) {

            appendBlockTagName(node, htmlBuilder);
            processInlineTag(node.getContent(), htmlBuilder);
            lineEnd(htmlBuilder);

            return null;
        }

        public Void visitUnknownInlineTag(UnknownInlineTagTree node, StringBuilder htmlBuilder) {
            processInlineTag(node.getContent(), htmlBuilder);

            return null;
        }

        private String processInlineTag(List<? extends DocTree> docTrees) {
            var stringBuilder = new StringBuilder();
            processInlineTag(docTrees, stringBuilder);

            return stringBuilder.toString();
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

        private String getLink(Object reference) {

            return getLink(reference, null);
        }

        private String getLink(Object reference, Object label) {
            String labelStr = null;

            if (label == null || label.toString().isEmpty()) {
                labelStr = reference.toString().replace("#", ".");

                if (labelStr.startsWith(".")) {
                    labelStr = labelStr.substring(1);
                }
            } else {
                labelStr = label.toString();
            }

            String link = String.format("<a href=\"%s\">%s</a>", reference, labelStr);

            return link;
        }

        private void appendBlockTagName(BlockTagTree tag, StringBuilder htmlBuilder) {

            if (!blockPassed.contains(tag.getTagName())) {
                blockPassed.add(tag.getTagName());
                htmlBuilder.append("<br>")
                        .append(String.format("<strong>%s:</strong><br>\n", resourceBundle.apply(tag.getTagName())));
            }
        }

        private void code(Object value, StringBuilder htmlBuilder) {
            htmlBuilder.append("<code>").append(value).append("</code>");
        }

        private void appendBlockTagCode(Object value, StringBuilder htmlBuilder) {
            code(value, htmlBuilder);
            lineEnd(htmlBuilder);
        }

        private void appendBlockTag(Object value, StringBuilder htmlBuilder) {
            htmlBuilder.append(value);
            lineEnd(htmlBuilder);
        }

        private void lineEnd(StringBuilder htmlBuilder) {
            htmlBuilder.append("<br>").append("\n");
        }
    }

    public record HtmlDoc(Signature signature, String packageName, List<String> imports, String doc) {
    }
}
