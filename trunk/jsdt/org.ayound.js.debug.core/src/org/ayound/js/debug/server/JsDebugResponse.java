/*******************************************************************************
 *
 *==============================================================================
 *
 * Copyright (c) 2008-2011 ayound@gmail.com
 * All rights reserved.
 * 
 * Created on 2008-10-26
 *******************************************************************************/
package org.ayound.js.debug.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import org.ayound.js.debug.core.JsDebugCorePlugin;
import org.ayound.js.debug.model.JsBreakPoint;
import org.ayound.js.debug.resource.JsResourceManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;

public class JsDebugResponse {
	private PrintWriter out;

	private Socket client;

	private OutputStream outPutStream;

	private JsResourceManager jsManager;

	public JsDebugResponse(OutputStream outPutStream, Socket client,
			JsResourceManager manager) {
		this.client = client;
		this.outPutStream = outPutStream;
		this.jsManager = manager;
		try {
			this.out = new PrintWriter(this.client.getOutputStream(), true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeHTMLHeader(String encoding) {
		if (encoding == null) {
			encoding = "UTF-8";
		}
		out.println("HTTP/1.0 200 OK");// ����Ӧ����Ϣ,������Ӧ��
		out.println("Content-Type:text/html;charset=" + encoding);
		out.println();// ���� HTTP Э��, ���н�����ͷ��Ϣ
	}

	public void writeJsHeader(String encoding) {
		if (encoding == null) {
			encoding = "UTF-8";
		}
		out.println("HTTP/1.0 200 OK");// ����Ӧ����Ϣ,������Ӧ��
		out.println("Content-Type:text/javascript;charset=" + encoding);
		out.println();// ���� HTTP Э��, ���н�����ͷ��Ϣ
	}

	public void writeOtherHeader(String fileName) {
		fileName = fileName.toLowerCase();
		out.println("HTTP/1.0 200 OK");// ����Ӧ����Ϣ,������Ӧ��
		if (fileName.endsWith("gif") || fileName.endsWith("jpg")
				|| fileName.equals("bmp") || fileName.endsWith("png")) {
			out.println("image/*");
		} else if (fileName.endsWith("css")) {
			out.println("text/css");
		}
		out.println();// ���� HTTP Э��, ���н�����ͷ��Ϣ
	}

	public void write(String str) {
		if (!this.client.isClosed()) {
			this.out.write(str);
		}
	}

	public void writeln(String str) {
		if (!this.client.isClosed()) {
			this.out.write(str + "\n");
		}
	}

	public void close() {
		this.out.close();
		try {
			this.outPutStream.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			this.client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeResume() {
		StringBuffer buffer = new StringBuffer(
				"{COMMAND:'BREAKPOINT',BREAKPOINTS:{");
		IBreakpointManager manager = DebugPlugin.getDefault()
				.getBreakpointManager();
		for (IBreakpoint point : manager
				.getBreakpoints(JsDebugCorePlugin.MODEL_ID)) {
			if (point instanceof JsBreakPoint) {
				String resource = jsManager.getResourceByFile((IFile) point
						.getMarker().getResource());
				int line = point.getMarker().getAttribute(IMarker.LINE_NUMBER,
						0);
				buffer.append("'").append(resource).append(line).append(
						"':true,");
			}
		}
		buffer.append("'end':false}}");
		this.write(buffer.toString());

	}

	public void writeTerminate() {
		this.write("{COMMAND:'TERMINATE'}");

	}

	public void writeStepOver() {
		this.write("{COMMAND:'STEPOVER'}");
	}

	public void writeStepReturn() {
		this.write("{COMMAND:'STEPRETURN'}");
	}

	public void writeStepInTo() {
		this.write("{COMMAND:'STEPINTO'}");
	}

	public OutputStream getOutPutStream() {
		return outPutStream;
	}

	public JsResourceManager getJsManager() {
		return jsManager;
	}
}
