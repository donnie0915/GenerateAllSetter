package com.okhjp.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

import java.util.Hashtable;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: donnie
 * email:hjp22222@163.com
 * blog: www.okhjp.com
 * Date: 2013-6-1 Time: 上午11:21
 *
 * desc ：
 */

public class GenerateAllSetterAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(DataKeys.EDITOR.getData(e.getDataContext()) != null);
    }

    public void actionPerformed(AnActionEvent e) {

        final Editor editor = DataKeys.EDITOR.getData(e.getDataContext());
        PsiElement psiElement = DataKeys.PSI_ELEMENT.getData(e.getDataContext());
        final int offset = editor.getCaretModel().getOffset();
        final Document document = editor.getDocument();
        int lineNum = document.getLineNumber(offset) ;
        int startOffset = document.getLineStartOffset(lineNum);

        CharSequence editorText = document.getCharsSequence();
        int wordStartOffset = getWordStartOffset(editorText, offset);
        final int distance = wordStartOffset - startOffset;

        PsiClass psiClass = (PsiClass) psiElement;

        String name = psiClass.getName();
        String firstChar = psiClass.getName().substring(0,1).toLowerCase();
        final String  className = firstChar + name.substring(1, name.length());

        // 定义正则表达式，从方法中过滤出 setter 函数.
        String ss = "set(\\w+)";
        Pattern setM = Pattern.compile(ss);
        // 把方法中的"set" 或者 "get" 去掉
        String rapl = "$1";
        String param;

        // 存放set方法
        final Hashtable<String, String> setMethods = new Hashtable<String, String>();
        PsiMethod[] methods = psiClass.getMethods();
        for (PsiMethod method : methods) {
            String methodName = method.getName();
            if (Pattern.matches(ss, methodName)) {
                param = setM.matcher(methodName).replaceAll(rapl).toLowerCase();
                setMethods.put(param, methodName);
            } else {
                // System.out.println(methodName + " 不是setter方法！");
            }
        }


        Application application = ApplicationManager.getApplication();
        application.runWriteAction(new Runnable() {
            public void run() {
                String blankSpace = "";
                for (int i = 0; i < distance; i++) {
                    blankSpace = blankSpace + " ";
                }

                int lineNumber = document.getLineNumber(offset) + 1;
                for (String arg : setMethods.keySet()) {
                    int lineStartOffset = document.getLineStartOffset(lineNumber++);
                    document.insertString(lineStartOffset ,blankSpace+ className + "." + setMethods.get(arg) + "();\n");
                    editor.getCaretModel().moveToOffset(lineStartOffset + 2);
                    editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                }
            }
        });
    }


    private int getWordStartOffset(CharSequence editorText, int cursorOffset) {
        if (editorText.length() == 0) return 0;
        if (cursorOffset > 0 && !Character.isJavaIdentifierPart(editorText.charAt(cursorOffset))
                && Character.isJavaIdentifierPart(editorText.charAt(cursorOffset - 1))) {
            cursorOffset--;
        }

        if (Character.isJavaIdentifierPart(editorText.charAt(cursorOffset))) {
            int start = cursorOffset;
            int end = cursorOffset;

            //定位开始位置
            while (start > 0 && Character.isJavaIdentifierPart(editorText.charAt(start - 1))) {
                start--;
            }
            return start;

        }

        return 0;

    }


}
