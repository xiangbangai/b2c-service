package com.huateng.toprules.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author cheney
 *
 */
public class RuleUtil {
	
	/**
	 * @param condition
	 * @return
	 */
	public static String[] splitCondition(String condition){
		String regrex = "^\\s*(\\w+)\\s*(==|!=)\\s*(.+)$";		

		Pattern p = Pattern.compile(regrex);
		Matcher m = p.matcher(condition);		
		while (m.find()) {
			int count = m.groupCount();
			String [] groups = new String[m.groupCount()];
			//val = m.group();
			groups[0] = m.group(1);
			groups[1] = m.group(2);
			groups[2] = m.group(3);	
			System.out.println(groups[0]);
//			groups[0] = "arg.get"+groups[0].substring(0,1).toUpperCase()+groups[0].substring(1)+"()";
			return groups;
		}
		return null;
	}
	
	public static String convertExpression(String express){
		if(express==null || express.length()==0){
			return "";
		}
		char cchar[] = express.toCharArray();
		char ccharRet[] = new char[express.length()*4];
		
		for(int i=0;i<ccharRet.length;i++){
			ccharRet[i]=" ".charAt(0);
		}
		
		boolean hasLter = false;
		int size = cchar.length;
		int j = 0;
		for(int i=0;i<size;i++){
			if((cchar[i]>='a' && cchar[i]<='z') || (cchar[i]>='A' && cchar[i]<='Z')){
				if(hasLter==false){/*字母开始*/
					ccharRet[j++] = 'a';
					ccharRet[j++] = 'r';
					ccharRet[j++] = 'g';
					ccharRet[j++] = '.';
					ccharRet[j++] = 'g';
					ccharRet[j++] = 'e';
					ccharRet[j++] = 't';
					ccharRet[j++] = lowerToUpperCaseCase(cchar[i]);
					hasLter = true;
				}else{
					ccharRet[j++] = cchar[i];
				}
			}else{
				if(hasLter){
					ccharRet[j++] = '(';
					ccharRet[j++] = ')';
				}
				hasLter=false;
				ccharRet[j++] = cchar[i];
			}
		}
		return new String(ccharRet);
	}
	
    public static char upperCaseToLowerCase(char ch) {
        if (ch >= 65 && ch <= 90) { //如果是大写字母就转化成小写字母
            ch = (char) (ch + 32);
        }
        return ch;
    }
    
    public static char lowerToUpperCaseCase(char ch) {
        if (ch > 90) { //如果是小写字母就转化成大写字母
            ch = (char) (ch - 32);
        }
        return ch;
    }
    
    public static void copyFile(String src,String desc) throws IOException{
    	FileInputStream input=new FileInputStream(new File(src));
    	FileOutputStream output=new FileOutputStream(desc);
    	byte[] b=new byte[1024*5];
    	int len;
    	while((len=input.read(b))!=-1){
    		output.write(b,0,len);
    	}
    	output.flush();
    	output.close();
    	input.close();
    }

	
}
