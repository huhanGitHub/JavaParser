import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author huhan on 11/15/18.
 */


public class CodeParser {

    static String className=null;
    static String inputPath="javasrc/";
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
                String code = n.getBody().get().toString();
                code = code.substring(1, code.length() - 1);

                code = removePunctuation(code);
                code = removeComment(code);
                code = replaceBlank(code).trim();
                code = splitWordsByCapitalLetter(code);
                code = code.toLowerCase();
                String json = code;
                json = json + "\n";
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
        String s = str.replaceAll("\\s{1,}"," ");
        return s;
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

    public static String removePunctuation(String str){
        String s=str.replaceAll("[\\pP\\p{Punct}]"," ");
        return s;
    }

    public static String splitWordsByCapitalLetter(String str){
        if (str.length()<2)
            return str;
        String[] strs=str.split(" ");
        StringBuffer all=new StringBuffer();
        for (int i=0; i < strs.length; i++){
            StringBuffer sb=new StringBuffer(strs[i]);

            Boolean allIsCapital=true;
            for (int j=0; j<sb.length();j++){
                Boolean isCapital = Character.isUpperCase(sb.charAt(j));
                allIsCapital = allIsCapital && isCapital;

                if (isCapital && j != 0) {
                    sb.insert(j, " ");
                    j++;
                }
            }
            if (!allIsCapital)
                all.append(sb+" ");
            else
                all.append(strs[i]+" ");
        }
        return all.toString().substring(0, all.length()-1);
    }

}

