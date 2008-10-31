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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import org.eclipse.debug.core.model.IThread;

public class JsConnectionThread extends Thread {
	private Socket connection;

	private IThread thread;

	private IDebugServer server;

	public JsConnectionThread(Socket conn, IThread thread, IDebugServer server) {
		this.connection = conn;
		this.thread = thread;
		this.server = server;
		this.start();
	}

	@Override
	public void run() {
		try {
			int contentLength = 0;// �ͻ��˷��͵� HTTP ���������ĳ���
			if (this.connection != null) {
				try {
					// ��һ�׶�: ��������
					BufferedReader in = new BufferedReader(
							new InputStreamReader(this.connection
									.getInputStream()));

					// ��ȡ��һ��, �����ַ
					String line = in.readLine();
					String resource = line.substring(line.indexOf('/'), line
							.lastIndexOf('/') - 5);
					// ����������Դ�ĵ�ַ
					resource = URLDecoder.decode(resource, "UTF-8");// ������
					// URL
					// ��ַ
					String method = new StringTokenizer(line).nextElement()
							.toString();// ��ȡ���󷽷�, GET ���� POST

					// ��ȡ������������͹������������ͷ����Ϣ
					while ((line = in.readLine()) != null) {

						// ��ȡ POST �����ݵ����ݳ���
						if (line.startsWith("Content-Length")) {
							try {
								contentLength = Integer.parseInt(line
										.substring(line.indexOf(':') + 1)
										.trim());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						if (line.equals("")) {
							break;
						}
					}
					StringBuffer buffer = new StringBuffer();
					// ��ʾ POST ���ύ������, �������λ����������岿��
					if ("POST".equalsIgnoreCase(method) && (contentLength > 0)) {
						for (int i = 0; i < contentLength; i++) {
							buffer.append((char) in.read());
						}
					}

					JsDebugResponse response = new JsDebugResponse(
							this.connection.getOutputStream(), this.connection,this.server.getJsResourceManager());
					IServerProcessor processor = ProcessorFactory
							.createProcessor(resource, method, buffer
									.toString(), response, this.thread,
									this.server);
					processor.process();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// System.out.println(connection+"���ӵ�HTTP������");//���������һ��,��������Ӧ�ٶȻ����
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public IDebugServer getServer() {
		return this.server;
	}
}
