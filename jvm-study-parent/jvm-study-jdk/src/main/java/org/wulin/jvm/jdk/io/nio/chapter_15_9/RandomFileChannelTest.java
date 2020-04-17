package org.wulin.jvm.jdk.io.nio.chapter_15_9;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
/**
 * Description:
 * <br/>网站: <a href="http://www.crazyit.org">疯狂Java联盟</a>
 * <br/>Copyright (C), 2001-2016, Yeeku.H.Lee
 * <br/>This program is protected by copyright laws.
 * <br/>Program Name:
 * <br/>Date:
 * @author Yeeku.H.Lee kongyeeku@163.com
 * @version 1.0
 */
public class RandomFileChannelTest
{
	public static void main(String[] args)
		throws IOException
	{
		File f = new File("wulin_resources/a.txt");
		String property = System.getProperty("file.encoding");
		System.out.println(property);
		try(
			// 创建一个RandomAccessFile对象
			RandomAccessFile raf = new RandomAccessFile(f, "rw");
				
			// 获取RandomAccessFile对应的Channel
			FileChannel randomChannel = raf.getChannel())
		{
			// 将Channel中所有数据映射成ByteBuffer
			ByteBuffer buffer = randomChannel.map(FileChannel
				.MapMode.READ_ONLY, 0 , f.length());
			// 把Channel的记录指针移动到最后
			randomChannel.position(f.length());
			// 将buffer中所有数据输出
			randomChannel.write(buffer);
			
		}
	}
}
