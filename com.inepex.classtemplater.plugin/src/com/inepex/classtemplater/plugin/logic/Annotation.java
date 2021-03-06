/*
 * Copyright:
 * 2010 Tibor Somodi, Inepex, Hungary, http://www.inepex.com
 * License:
 * EPL: http://www.eclipse.org/legal/epl-v10.html
 */

package com.inepex.classtemplater.plugin.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IConstantPoolEntry;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.internal.core.util.AnnotationDefaultAttribute;

import com.inepex.classtemplater.plugin.Log;

public class Annotation {

	ICompilationUnit compilationUnit;
	
	String name;
	Map<String, String> params = new HashMap<String, String>();
	Map<String, Object> paramObjects = new HashMap<String, Object>();
	
	public Annotation(IAnnotation jdtAnnotation, ICompilationUnit compilationUnit) throws Exception {
		this.compilationUnit = compilationUnit;
		name = jdtAnnotation.getElementName();
		
		//default values aren't found in JDT so using AST to get them
		String[][] type = compilationUnit.findPrimaryType().resolveType(jdtAnnotation.getElementName());
		if (type != null)
		{
			IType annType = jdtAnnotation.getJavaProject().findType(type[0][0] + "." + type[0][1]);

			//hint to read annotation default value from a classfile
//						
			
			if (annType.getCompilationUnit() != null){
				AnnotationASTVisitor annASTVisitor = new AnnotationASTVisitor();
				ASTParser parser = ASTParser.newParser(AST.JLS3);
				parser.setKind(ASTParser.K_COMPILATION_UNIT);
				parser.setSource(annType.getCompilationUnit());
				parser.setResolveBindings(true);
				CompilationUnit aParser = (CompilationUnit) parser.createAST(null);
				aParser.accept(annASTVisitor);
				Map<String, Object> defaultValues = annASTVisitor.getDefaultValueObjects();
				for (Entry<String, Object> entry : defaultValues.entrySet()){
					paramObjects.put(entry.getKey(), entry.getValue());
					params.put(entry.getKey(), String.valueOf(entry.getValue()));
				}
			} else {
				//read annotation default value from .class file
				IClassFileReader reader = ToolFactory.createDefaultClassFileReader(annType.getClassFile(), IClassFileReader.ALL);
				if (reader != null){
					for (IMethodInfo methodInfo : reader.getMethodInfos()){
						if (methodInfo.getAttributes().length > 0 && methodInfo.getAttributes()[0] instanceof AnnotationDefaultAttribute){
							String name = new String(methodInfo.getName());
							Object value = parseDefaultObjectValueFromAnnotationDefaultAttribute(
									(AnnotationDefaultAttribute)(methodInfo.getAttributes()[0]),
									new String(methodInfo.getDescriptor()));
							if (value != null) {
								paramObjects.put(name, value);
								params.put(name, String.valueOf(value));
							}
						}
//						System.out.println(methodInfo.getName());
						
					}
				}
			}
		}

		for (IMemberValuePair pair : jdtAnnotation.getMemberValuePairs()){
			try {
				params.put(pair.getMemberName(), String.valueOf(pair.getValue()));
				paramObjects.put(pair.getMemberName(), pair.getValue());
			} catch (ClassCastException e) {
				Log.log("Could not cast value of annotation-attribute: " + name + ", " + pair.getMemberName() + ". \n" +
						"Only string values can be used for annotation-attribute");
			}
		}
	}
	
	/**
	 * kind = 1, String
	 * kind = 4, float
	 * kind = 5, long
	 * kind = 6, double
	 * kind = 3, int
	 * kind = 3, boolean
	 */
	private Object parseDefaultObjectValueFromAnnotationDefaultAttribute(AnnotationDefaultAttribute a, String type){
		if (a.getMemberValue().getConstantValue() != null){
			IConstantPoolEntry constant = a.getMemberValue().getConstantValue();
			switch (constant.getKind()){
				case 1: 
					return new String(constant.getUtf8Value());
				case 5:
					return constant.getLongValue();
				case 6:
					return constant.getDoubleValue();
				case 4:
					return constant.getFloatValue();
				case 3:
					if (type.contains("()Z")){
						return constant.getIntegerValue() == 1;
					} else return constant.getIntegerValue();
				default: return null;
			}
		} else if (a.getMemberValue().getEnumConstantName() != null) {
			String[] typeSplitted = new String(a.getMemberValue().getEnumConstantTypeName()).split("/");
			String enumType = typeSplitted[typeSplitted.length-1].substring(0, typeSplitted[typeSplitted.length-1].length()-1);
			return enumType + "." + new String(a.getMemberValue().getEnumConstantName());
		} else return null;
		
	}
	
	public Annotation(String name, Map<String, String> params) {
		super();
		this.name = name;
		this.params = params;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, String> getParams() {
		return params;
	}
	public void setParams(Map<String, String> params) {
		this.params = params;
	}
	
	/**
	 * 
	 * @param name
	 * @return String.valueOf(param)
	 */
	public String getParamValue(String name){
		return params.get(name);
	}
	
	public List<Annotation> getNestedAnnotationList(String name) throws Exception {
		List<Annotation> nestedAnnotations = new ArrayList<Annotation>();
		
		if (paramObjects.containsKey(name)) {
			for (Object annotation : (Object[])paramObjects.get(name)){
				nestedAnnotations.add(new Annotation((IAnnotation)annotation, compilationUnit));
			}
		}
		return nestedAnnotations;
	}
	
	public Object getParamObject(String name){
		return paramObjects.get(name);
	}
	
	public Boolean getParamBoolean(String name){
		return (Boolean)paramObjects.get(name);
	}
	
	public Double getParamDouble(String name){
		return (Double)paramObjects.get(name);
	}
	
	public Integer getParamInteger(String name){
		return (Integer)paramObjects.get(name);
	}
	
	public Long getParamLong(String name){
		return (Long)paramObjects.get(name);
	}
	
	public List<String> getParamStringList(String name){
		List<String> stringList = new ArrayList<String>();
		for (Object o : (Object[])paramObjects.get(name)){
			stringList.add((String)o);
		}
		return stringList;
	}
	
	public static Map<String, Annotation> getAnnotationsOf(IAnnotatable annotable, ICompilationUnit compilationUnit) throws Exception {
		Map<String, Annotation> annotations = new HashMap<String, Annotation>();
		for(IAnnotation annotation : annotable.getAnnotations()){
			Annotation a = new Annotation(annotation, compilationUnit);
			annotations.put(a.getName(), a);
		}		
		return annotations;
	}
	
}
