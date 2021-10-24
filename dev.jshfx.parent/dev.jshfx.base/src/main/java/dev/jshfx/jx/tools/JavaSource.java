package dev.jshfx.jx.tools;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

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
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTreePathScanner;
import com.sun.source.util.DocTrees;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

public class JavaSource {

    private JavaCompiler compiler;
    private StandardJavaFileManager fileManager;
    private ResourceBundle resourceBundle;

    public JavaSource() {
        compiler = ToolProvider.getSystemJavaCompiler();
        fileManager = compiler.getStandardFileManager(null, null, null);
    }

    public JavaSource setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        return this;
    }

    public JavaSource setSourcePaths(Collection<Path> sourcePaths) {
        try {
            fileManager.setLocationFromPaths(StandardLocation.SOURCE_PATH, sourcePaths);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    public String getHtmlDoc(Signature signature) {
        StringBuilder htmlBuilder = new StringBuilder();

        try {
            JavaFileObject jfo = fileManager.getJavaFileForInput(StandardLocation.SOURCE_PATH, signature.getFullName(),
                    JavaFileObject.Kind.SOURCE);
            JavacTask task = (JavacTask) compiler.getTask(null, null, null, null, null, List.of(jfo));

            Trees trees = Trees.instance(task);
            DocTrees docTrees = DocTrees.instance(task);

            Iterable<? extends CompilationUnitTree> unitTrees = task.parse();
            SignatureTreePathScanner scanner = new SignatureTreePathScanner(trees);
            TreePath treePath = scanner.scan(unitTrees.iterator().next(), signature);

            DocCommentTree docCommentTree = docTrees.getDocCommentTree(treePath);
            HtmlDocTreePathScanner docScanner = new HtmlDocTreePathScanner();

            docScanner.scan(new DocTreePath(treePath, docCommentTree), htmlBuilder);

        } catch (IOException e) {

            e.printStackTrace();
        }

        return htmlBuilder.toString();
    }

    private static class SignatureTreePathScanner extends TreePathScanner<TreePath, Signature> {

        private Trees trees;

        public SignatureTreePathScanner(Trees trees) {
            this.trees = trees;
        }

        @Override
        public TreePath visitCompilationUnit(CompilationUnitTree node, Signature signature) {
            return scan(node.getTypeDecls(), signature);
        }

        public TreePath visitClass(ClassTree node, Signature signature) {

            if (node.getSimpleName().toString().equals(signature.getSimpleName())) {

                return getCurrentPath();
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
