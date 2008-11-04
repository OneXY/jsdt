/*******************************************************************************
 *
 *==============================================================================
 *
 * Copyright (c) 2008-2011 ayound@gmail.com 
 * This program and the accompanying materials
 * are made available under the terms of the Apache License 2.0 
 * which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * All rights reserved.
 * 
 * Created on 2008-11-4
 *******************************************************************************/
package org.ayound.js.debug.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.mozilla.intl.chardet.HtmlCharsetDetector;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

import java.io.InputStream;

public class CharsetDetector {

	private String charset;

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
	/**
	 * detect charset from inputStream
	 * only support chinese charset
	 * @param inputStream
	 */
	public void detect(InputStream inputStream) {
		nsICharsetDetectionObserver charsetDetectObserver = new nsICharsetDetectionObserver() {
			public void Notify(String charset) {
				HtmlCharsetDetector.found = true;
				setCharset(charset);
			}
		};
		nsDetector detector = new nsDetector(nsDetector.CHINESE);
		// ����һ��Oberver
		detector.Init(charsetDetectObserver);
		BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
		try {
			byte[] buffer = new byte[1024];
			boolean done = false; // �Ƿ��Ѿ�ȷ��ĳ���ַ���
			boolean isAscii = true;// �ٶ���ǰ�Ĵ���ASCII����
			boolean found = false;
			int len = 0;
			while ((len = bufferedInputStream.read(buffer, 0, buffer.length)) != -1) {
				// ����ǲ���ȫ��ascii�ַ�������һ���ַ�����ASC����ʱ�������е����ݼ�����ASCII�����ˡ�
				if (isAscii)
					isAscii = detector.isAscii(buffer, len);
				// �������ascii�ַ��������DoIt����.
				if (!isAscii && !done)
					done = detector.DoIt(buffer, len, false);// �������ASCII���ֻ�ûȷ�����뼯���������⡣
			}
			detector.DataEnd();// ���Ҫ���ô˷�������ʱ��Notify�����á�
			if (isAscii) {
				setCharset("ascii");
				found = true;
			}
			if (!found) {// ���û�ҵ������ҵ�����ܵ���Щ�ַ���
				String charsets[] = detector.getProbableCharsets();
				if (charsets.length > 0) {
					setCharset(charsets[1]);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				bufferedInputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	/**
	 * detect charset from inputStream
	 * only support chinese charset
	 * @param fileName
	 */
	public void detect(String fileName){
		try {
			detect(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * detect charset from inputStream
	 * only support chinese charset
	 * @param file
	 */
	public void detect(File file){
		try {
			detect(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * detect charset from inputStream
	 * only support chinese charset
	 * @param file
	 */
	public void detect(IFile file){
		try {
			detect(file.getContents());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
