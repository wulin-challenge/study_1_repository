package org.wulin.jvm.jdk.unsafe;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import sun.misc.Unsafe;

/**
 * unsafe demo 测试类
 * <p> 参考文章: https://blog.csdn.net/u010398771/article/details/85319991
 * @author wulin
 *
 */
public class UnsafeDemoTest {
	
	/**
	 * 操作数组:
	 * 可以获取数组的在内容中的基本偏移量（arrayBaseOffset），获取数组内元素的间隔（比例），
	 * 根据数组对象和偏移量获取元素值（getObject），设置数组元素值（putObject），示例如下。
	 */
	@Test
	public void unsafeArrayTest() {
		System.out.println();
		String[] strings = new String[]{"1", "2", "3"};
		long i = UnsafeUtil.getUnsafe().arrayBaseOffset(String[].class);
		System.out.println("string[] base offset is :" + i);
		System.out.println();
		
		//every index scale
		long scale = UnsafeUtil.getUnsafe().arrayIndexScale(String[].class);
		System.out.println("string[] index scale is " + scale);
		 
		//print first string in strings[]
		System.out.println("first element is :" + UnsafeUtil.getUnsafe().getObject(strings, i));
		 
		//set 100 to first string
		UnsafeUtil.getUnsafe().putObject(strings, i + scale * 0, "100");
		 
		//print first string in strings[] again
		System.out.println("after set ,first element is :" + UnsafeUtil.getUnsafe().getObject(strings, i + scale * 0));
	}
	
	/**
	 * 对象操作
	 * 实例化Data
	 *
	 * 可以通过类的class对象创建类对象（allocateInstance），获取对象属性的偏移量（objectFieldOffset）
	 * ，通过偏移量设置对象的值（putObject）
	 *
	 * 对象的反序列化
	 * 当使用框架反序列化或者构建对象时，会假设从已存在的对象中重建，你期望使用反射来调用类的设置函数，
	 * 或者更准确一点是能直接设置内部字段甚至是final字段的函数。问题是你想创建一个对象的实例，
	 * 但你实际上又不需要构造函数，因为它可能会使问题更加困难而且会有副作用。
	 *
	 */
	@Test
	public void unsaveObjectTest() {
		System.out.println();
		try {
			Unsafe unsafe = UnsafeUtil.getUnsafe();
			//调用allocateInstance函数避免了在我们不需要构造函数的时候却调用它
			Data data = (Data) unsafe.allocateInstance(Data.class);
			data.setId(1L);
			data.setName("unsafe");
			System.out.println(data);
			 
			//返回成员属性在内存中的地址相对于对象内存地址的偏移量
			Field nameField = Data.class.getDeclaredField("name");
			long fieldOffset = unsafe.objectFieldOffset(nameField);
			//putLong，putInt，putDouble，putChar，putObject等方法，直接修改内存数据（可以越过访问权限）
			unsafe.putObject(data,fieldOffset,"这是新的值");
			System.out.println(data.getName());
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 我们可以在运行时创建一个类，比如从已编译的.class文件中。将类内容读取为字节数组，
	 * 并正确地传递给defineClass方法；当你必须动态创建类，而现有代码中有一些代理， 这是很有用的
	 * <p> TODO 测试没有通过,猜测可能参数等方面传递有问题
	 */
	@Test
	public void unsafeDynamicClassTest() {
		System.out.println();
		try {
			Unsafe unsafe = UnsafeUtil.getUnsafe();
			File file = new File("D:\\software\\helpProgramming\\git_client\\git\\workspace_directory\\self_directory\\study_jvm_workspace\\study_jvm\\jvm-study-parent\\jvm-study-jdk\\target\\classes\\org\\wulin\\jvm\\jdk\\unsafe\\Data.class");
			
			FileInputStream input = new FileInputStream(file);
			byte[] content = new byte[(int)file.length()];
			input.read(content);
			Class c = unsafe.defineClass(null, content, 0, content.length,null,null);
			c.getMethod("getId").invoke(c.newInstance(), null);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 内存操作
	 * 可以在Java内存区域中分配内存（allocateMemory），设置内存（setMemory，用于初始化），
	 * 在指定的内存位置中设置值（putInt\putBoolean\putDouble等基本类型）
	 */
	@Test
	public void unsafeMomeryTest() {
		System.out.println();
		Unsafe unsafe = UnsafeUtil.getUnsafe();
		//分配一个8byte的内存
		long address = unsafe.allocateMemory(8L);
		//初始化内存填充1
		unsafe.setMemory(address, 8L, (byte) 1);
		//测试输出
		System.out.println("add byte to memory:" + unsafe.getInt(address));
		//设置0-3 4个byte为0x7fffffff
		unsafe.putInt(address, 0x7fffffff);
		//设置4-7 4个byte为0x80000000
		unsafe.putInt(address + 4, 0x80000000);
		//int占用4byte
		System.out.println("add byte to memory:" + unsafe.getInt(address));
		System.out.println("add byte to memory:" + unsafe.getInt(address + 4));
	}
	
	/**
	 * CAS操作
	 * Compare And Swap（比较并交换），当需要改变的值为期望的值时，那么就替换它为新的值，是原子
	 * （不可在分割）的操作。很多并发框架底层都用到了CAS操作，CAS操作优势是无锁，可以减少线程切换耗费
	 * 的时间，但CAS经常失败运行容易引起性能问题，也存在ABA问题。在Unsafe中包含compareAndSwapObject、
	 * compareAndSwapInt、compareAndSwapLong三个方法，compareAndSwapInt的简单示例如下。
	 */
	@Test
	public void unsafeCompareAndSwapTest() {
		System.out.println();
		Unsafe unsafe = UnsafeUtil.getUnsafe();
		try {
			Data data = new Data();
			data.setId(1L);
			Field id = data.getClass().getDeclaredField("id");
			long offset = unsafe.objectFieldOffset(id);
//			id.setAccessible(true);
			//比较并交换，比如id的值如果是所期望的值1，那么就替换为2，否则不做处理
			// 一定要是基本类型的long,不能是包装类型的 Long
			boolean compareAndSwapLong = unsafe.compareAndSwapLong(data,offset,1L,2L);
			System.out.println("success : "+compareAndSwapLong+",id: "+data.getId());
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 常量获取
	 *
	 * 可以获取地址大小（addressSize），页大小（pageSize），基本类型数组的偏移量
	 * （Unsafe.ARRAY_INT_BASE_OFFSET\Unsafe.ARRAY_BOOLEAN_BASE_OFFSET等）、
	 * 基本类型数组内元素的间隔（Unsafe.ARRAY_INT_INDEX_SCALE\Unsafe.ARRAY_BOOLEAN_INDEX_SCALE等）
	 */
	@Test
	public void unsafeConstantObtainTest() {
		System.out.println();
		Unsafe unsafe = UnsafeUtil.getUnsafe();
		//get os address size
		System.out.println("address size is :" + unsafe.addressSize());
		//get os page size
		System.out.println("page size is :" + unsafe.pageSize());
		//int array base offset
		System.out.println("unsafe array int base offset:" + Unsafe.ARRAY_INT_BASE_OFFSET);
	}
	
	/**
	 * 线程许可
	 * 许可线程通过（park），或者让线程等待许可(unpark)，
	 */
	@Test
	public void unsafeThreadParkTest() {
		System.out.println();
		Unsafe unsafe = UnsafeUtil.getUnsafe();
		
		try {
			Thread packThread = new Thread(() -> {
			    long startTime = System.currentTimeMillis();
			    //纳秒，相对时间park
			    unsafe.park(false,3000000000L);
			    //毫秒，绝对时间park
			    //unsafe.park(true,System.currentTimeMillis()+3000);
			 
			    System.out.println("main thread end,cost :"+(System.currentTimeMillis()-startTime)+"ms");
			});
			packThread.start();
			TimeUnit.SECONDS.sleep(1);
			//注释掉下一行后，线程3秒数后进行输出,否则在1秒后输出
			unsafe.unpark(packThread);
			
			System.in.read();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
