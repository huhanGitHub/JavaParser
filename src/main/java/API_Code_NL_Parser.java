import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.sun.deploy.net.proxy.RemoveCommentReader;

import java.io.*;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author huhan on 11/15/18.
 */


public class API_Code_NL_Parser {

    static String className=null;
    static String inputPath="javasrc/";
    //static String outputPath="output/trainPairs.txt";
    static String outputPath="output/code.txt";
    public static void main(String [] args) {
        if (args.length == 2) {
            inputPath = args[0];
            outputPath = args[1];
        }
        ArrayList<File> files=new ArrayList<>();
        try {
            files=getFiles(inputPath);
            for (File file:files){
                System.out.println(file.getName());
                className=file.getName();
                CompilationUnit cu= JavaParser.parse(file);
                cu.accept(new MethodVisitor(),null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

        //FileInputStream in =new FileInputStream("javaFiles/Service.java");
    }

    private static class MethodVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            super.visit(n, arg);
            //System.out.println(n.getDeclarationAsString(false,false,false));

            try {
            String nl = n.getComment().get().toString();
            String name = n.getNameAsString();
            String api=className.substring(0, className.lastIndexOf('.'))+" "+name;
            String code = n.getBody().get().toString();
            code=removeComment(code);


            nl = nl.
                    replace("*", "").
                    replace("/", "").
                    replace("\\", "");
            String[] comments = nl.split("\\.");
            nl = comments[0];

            nl = replaceBlank(nl.trim()).replace("  ", " ");
            name = name.trim().replace("_"," ");
            code = code.substring(1, code.length() - 1);
            code = replaceBlank(code.trim()).replace("  ", " ");

            //System.out.println(nl);
            //System.out.println(name);
            //System.out.println(code);

            String json = "[" + "\'" + api + "\',"+"\'" + nl + "\'," + "\'" + name + "\'," + "\'" + code + "\']";
            //String json = "[" +"\'" + nl + "\'," + "\'" + code + "\']";
            //String json = code;
            json = json + "\n";
            //System.out.println(json);

            try {
                FileWriter fw = new FileWriter(outputPath, true);
                json=json.replaceAll(" +"," ");
                //System.out.println(json);
                fw.write(json);
                fw.flush();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch (NoSuchElementException e){
                e.printStackTrace();
                return;
            }
        }

    }


    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    public static ArrayList<File> getFiles(String path) throws Exception{
        ArrayList<File> fileList=new ArrayList<File>();
        File file=new File(path);
        if (file.isDirectory()){
            File []files=file.listFiles();
            for (File fileIndex:files){
                if (fileIndex.isDirectory()){
                    fileList.addAll(getFiles(fileIndex.getPath()));
                }else {
                    String name=fileIndex.getName();
                    //System.out.println(name);
                    if (name.substring(name.lastIndexOf(".")+1).equals("java")){
                        //System.out.println("choice:___________________"+name);
                        fileList.add(fileIndex);
                    }
                }
            }
        }
        return fileList;
    }

    public static String removeComment(String code){
        String []codeLines=code.split("\n");
        StringBuffer sb=new StringBuffer("");
        for (int i=0;i<codeLines.length;i++){
            if(codeLines[i].indexOf("//")>=0){
                System.out.println(codeLines[i]);
            }else {
                sb.append(codeLines[i]);
            }
        }
        return sb.toString();
    }

    }

