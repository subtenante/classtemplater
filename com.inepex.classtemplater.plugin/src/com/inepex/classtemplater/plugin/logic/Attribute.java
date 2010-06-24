package com.inepex.classtemplater.plugin.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceFieldElementInfo;

public class Attribute {

	String name;
	String type;
	String visibility;
	boolean isPrivate = false;
	boolean isPublic = false;
	boolean isProtected = false;
	boolean isStatic = false;
	boolean isAbstract = false;
	boolean isFinal = false;
	boolean isList = false;
	boolean isGeneric = false;
	boolean isEnum = false;
	String fistGenType;
	Set<Importable> typesInGenerics = new HashSet<Importable>();
	Map<String, Annotation> annotations = new HashMap<String, Annotation>();
	
	
	public Attribute(String name, String type, String visibility,
			boolean isStatic, boolean isAbstract, boolean isFinal, boolean isList, boolean isGeneric
			, boolean isEnum) {
		super();
		this.name = name;
		this.type = type;
		setVisibility(visibility);
		this.isStatic = isStatic;
		this.isAbstract = isAbstract;
		this.isFinal = isFinal;
		this.isList = isList;
		this.isGeneric = isGeneric;
		this.isEnum = isEnum;
	}
	
	public Attribute(IField field) throws Exception {
		String sign = field.getTypeSignature();
		
		if (sign.startsWith("Q")) {
			if (sign.indexOf("<") == -1) sign = sign.substring(1, sign.length()-1);
			else {
				////process generic types 
				String basetype = sign.substring(1, sign.indexOf("<"));
				String gentype = sign.substring(sign.indexOf("<") + 1, sign.indexOf(">"));
				String[] parts = gentype.split(";");
				String partString = "";
				for (String s : parts){
					String type = s.substring(1);
					if (typesInGenerics.size() == 0) fistGenType = type;
					typesInGenerics.add(new Importable(type));
					partString +=  type + ", ";
				}
				if (typesInGenerics.size() > 0) isGeneric = true;
				partString = partString.substring(0, partString.length() - 2);
				sign = basetype + "<" + partString + ">";				
			}
		}
		else if (sign.equals("I")) sign = "int";
		else if (sign.equals("J")) sign = "long";
		else if (sign.equals("Z")) sign = "boolean";
		else if (sign.equals("D")) sign = "double";
		else if (sign.equals("B")) sign = "byte";
		else if (sign.equals("S")) sign = "short";
		else if (sign.equals("F")) sign = "float";
		else if (sign.equals("C")) sign = "char";
		String visibility = "";
		if (Flags.isPublic(field.getFlags())) visibility = "public";
		else if (Flags.isPrivate(field.getFlags())) visibility = "private";
		else if (Flags.isProtected(field.getFlags())) visibility = "protected";
		else visibility = "public";
		
		name = field.getElementName();
		type = sign;
		this.visibility = visibility;
		isStatic = Flags.isStatic(field.getFlags());
		isAbstract = Flags.isAbstract(field.getFlags());
		isFinal = Flags.isFinal(field.getFlags());
		isList = sign.contains("List");
		
		try {
			String[][] type = field.getDeclaringType().resolveType(field.getTypeSignature().substring(1, field.getTypeSignature().length()-1));
			isEnum = field.getJavaProject().findType(type[0][0] + "." + type[0][1]).isEnum();
		} catch (Exception e) {
			System.out.println("Error at enum check" + e.getMessage());
			
		}

		//annotations
		annotations = Annotation.getAnnotationsOf(field);
	}
	
	public String getName() {
		return name;
	}
	
	public String getNameL1(){
		return StringUtil.getL1(name); 
	}
	
	public String getNameU1(){
		return StringUtil.getU1(name);
	}	
	
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getTypeL1(){
		return StringUtil.getL1(type); 
	}
	
	public String getTypeU1(){
		return StringUtil.getU1(type);
	}	
	
	public String getVisibility() {
		return visibility;
	}
	public void setVisibility(String visibility) {
		this.visibility = visibility;
		if (visibility.equals("private")) isPrivate = true;
		else if (visibility.equals("public")) isPublic = true;
		else if (visibility.equals("protected")) isProtected = true;
	}
	public boolean isPrivate() {
		return isPrivate;
	}
	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}
	public boolean isPublic() {
		return isPublic;
	}
	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}
	public boolean isProtected() {
		return isProtected;
	}
	public void setProtected(boolean isProtected) {
		this.isProtected = isProtected;
	}
	public boolean isStatic() {
		return isStatic;
	}
	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}
	public boolean isAbstract() {
		return isAbstract;
	}
	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}
	public boolean isFinal() {
		return isFinal;
	}
	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}
	
	public boolean isList() {
		return isList;
	}
	public void setList(boolean isList) {
		this.isList = isList;
	}
	
	public Set<Importable> getTypesInGenerics() {
		return typesInGenerics;
	}

	public void setTypesInGenerics(Set<Importable> typesInGenerics) {
		this.typesInGenerics = typesInGenerics;
	}

	public Map<String, Annotation> getAnnotations() {
		return annotations;
	}
	public void setAnnotations(Map<String, Annotation> annotations) {
		this.annotations = annotations;
	}
	public boolean hasAnnotation(String name){
		return (annotations.get(name) != null);
	}

	public String getFistGenType() {
		return fistGenType;
	}
	
	public String getFistGenTypeU1() {
		return StringUtil.getU1(fistGenType);
	}
	
	public String getFistGenTypeL1() {
		return StringUtil.getL1(fistGenType);
	}

	public void setFistGenType(String fistGenType) {
		this.fistGenType = fistGenType;
	}

	public boolean isGeneric() {
		return isGeneric;
	}

	public void setGeneric(boolean isGeneric) {
		this.isGeneric = isGeneric;
	}

	public boolean isEnum() {
		return isEnum;
	}

	public void setEnum(boolean isEnum) {
		this.isEnum = isEnum;
	}
	
	
	
}
