package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pentaho.di.core.CheckResultInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransformationInspection extends LocalInspectionTool {

    @Override
    public ProblemDescriptor @Nullable [] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        System.out.println(getClass().getSimpleName()+".checkFile");
        PdiFacet pdiFacet = PdiFacet.getInstance(file).orElse(null);
        if (pdiFacet == null) return null;
        XmlFile xmlFile = (XmlFile) file;
        try {
            pdiFacet.initializeKettleEnvironment();

            Class<?> transMetaClass = pdiFacet.loadClass("org.pentaho.di.trans.TransMeta");
            Constructor<?> nodeConstructor = Arrays.stream(transMetaClass.getDeclaredConstructors())
                    .filter(constructor -> constructor.getParameterTypes().length == 2)
                    .filter(constructor -> Node.class.equals(constructor.getParameterTypes()[0]))
                    .findFirst().orElseThrow();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlFile.getVirtualFile().getInputStream());
            Object transMeta = nodeConstructor.newInstance(document.getDocumentElement(), null);
            Method checkStepsMethod = Arrays.stream(transMetaClass.getDeclaredMethods())
                    .filter(method -> "checkSteps".equals(method.getName()))
                    .findFirst().orElseThrow();
            List<CheckResultInterface> remarks = new ArrayList<>();
            checkStepsMethod.invoke(transMeta, remarks, false, null, null, null, null);
            System.out.println(remarks);
        } catch (ClassNotFoundException | ParserConfigurationException | IOException | SAXException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

}
