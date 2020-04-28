/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.locks;

/**
 * A {@code ReadWriteLock} maintains a pair of associated {@link
 * Lock locks}, one for read-only operations and one for writing.
 * The {@link #readLock read lock} may be held simultaneously by
 * multiple reader threads, so long as there are no writers.  The
 * {@link #writeLock write lock} is exclusive.
 * 
 * <p> ReadWriteLock维护一对关联的锁，一个用于只读操作，一个用于写入。只要没有写程序，
 * 读锁就可以同时由多个读程序线程保持。写锁是排他的。
 *
 * <p>All {@code ReadWriteLock} implementations must guarantee that
 * the memory synchronization effects of {@code writeLock} operations
 * (as specified in the {@link Lock} interface) also hold with respect
 * to the associated {@code readLock}. That is, a thread successfully
 * acquiring the read lock will see all updates made upon previous
 * release of the write lock.
 * 
 * <p> 所有ReadWriteLock实现都必须保证writeLock操作（在Lock接口中指定）的内存同步效果对于关联的readLock也保持不变。
 * 也就是说，成功获取读锁的线程将看到在先前释放写锁时所做的所有更新。
 *
 * <p>A read-write lock allows for a greater level of concurrency in
 * accessing shared data than that permitted by a mutual exclusion lock.
 * It exploits the fact that while only a single thread at a time (a
 * <em>writer</em> thread) can modify the shared data, in many cases any
 * number of threads can concurrently read the data (hence <em>reader</em>
 * threads).
 * In theory, the increase in concurrency permitted by the use of a read-write
 * lock will lead to performance improvements over the use of a mutual
 * exclusion lock. In practice this increase in concurrency will only be fully
 * realized on a multi-processor, and then only if the access patterns for
 * the shared data are suitable.
 * 
 * <p> 与互斥锁相比，读写锁在访问共享数据时允许更高级别的并发性。它利用了这样的事实：虽然一次只能有一个线程
 * （写线程）可以修改共享数据，但在许多情况下，任何数量的线程都可以同时读取数据（因此有读取器线程）。从理论上讲，
 * 与使用互斥锁相比，使用读写锁允许的并发性增加将导致性能提高。实际上，并发性的增加只能在多处理器上完全实现，
 * 并且只有在共享数据的访问模式合适时才能实现。
 *
 * <p>Whether or not a read-write lock will improve performance over the use
 * of a mutual exclusion lock depends on the frequency that the data is
 * read compared to being modified, the duration of the read and write
 * operations, and the contention for the data - that is, the number of
 * threads that will try to read or write the data at the same time.
 * For example, a collection that is initially populated with data and
 * thereafter infrequently modified, while being frequently searched
 * (such as a directory of some kind) is an ideal candidate for the use of
 * a read-write lock. However, if updates become frequent then the data
 * spends most of its time being exclusively locked and there is little, if any
 * increase in concurrency. Further, if the read operations are too short
 * the overhead of the read-write lock implementation (which is inherently
 * more complex than a mutual exclusion lock) can dominate the execution
 * cost, particularly as many read-write lock implementations still serialize
 * all threads through a small section of code. Ultimately, only profiling
 * and measurement will establish whether the use of a read-write lock is
 * suitable for your application.
 *
 * <p> 读写锁是否会比使用互斥锁提高性能，取决于与修改相比，读取数据的频率，读取和写入操作的持续时间以及对数据的争用-是，
 * 将尝试同时读取或写入数据的线程数。例如，最初使用数据填充然后不经常修改但经常搜索的集合（例如某种目录）是使用读写锁的理想选择。
 * 但是，如果更新变得频繁，那么数据将大部分时间专门用于锁定，并且并发增加很少，甚至没有增加。此外，如果读操作太短，
 * 则读写锁实现的开销（其本质上比互斥锁更复杂）会支配执行成本，特别是因为许多读写锁实现仍通过线程序列化所有线程。
 * 一小段代码。最终，只有性能分析和测量才能确定使用读写锁是否适合您的应用程序。
 *
 * <p>Although the basic operation of a read-write lock is straight-forward,
 * there are many policy decisions that an implementation must make, which
 * may affect the effectiveness of the read-write lock in a given application.
 * Examples of these policies include:
 * 
 * <p> 尽管读写锁的基本操作很简单，但是实现必须做出许多策略决定，这可能会影响给定应用程序中读写锁的有效性。
 * 这些策略的示例包括：
 * 
 * <ul>
 * <li>Determining whether to grant the read lock or the write lock, when
 * both readers and writers are waiting, at the time that a writer releases
 * the write lock. Writer preference is common, as writes are expected to be
 * short and infrequent. Reader preference is less common as it can lead to
 * lengthy delays for a write if the readers are frequent and long-lived as
 * expected. Fair, or &quot;in-order&quot; implementations are also possible.
 * 
 * <p> 确定读写器释放写锁时，读写器都等待时是授予读锁还是写锁。编写者偏好是常见的，因为期望写的次数短且不频繁。
 * 读者偏爱不太普遍，因为如果读者经常出现且寿命长，则可能导致较长的写延迟。公平或“有序”的实现也是可能的。
 *
 * <li>Determining whether readers that request the read lock while a
 * reader is active and a writer is waiting, are granted the read lock.
 * Preference to the reader can delay the writer indefinitely, while
 * preference to the writer can reduce the potential for concurrency.
 * 
 * <p> 确定是否在读取器处于活动状态且写入器正在等待时请求读取锁定的读取器。对阅读者的偏好可以无限期地延迟作者，
 * 而对作家的偏好可以降低并发的可能性。
 *
 * <li>Determining whether the locks are reentrant: can a thread with the
 * write lock reacquire it? Can it acquire a read lock while holding the
 * write lock? Is the read lock itself reentrant?
 * 
 * <p> 确定锁是否可重入：具有写锁的线程能否重新获取它？持有写锁的同时可以获取读锁吗？读锁本身是否可重入？
 *
 * <li>Can the write lock be downgraded to a read lock without allowing
 * an intervening writer? Can a read lock be upgraded to a write lock,
 * in preference to other waiting readers or writers?
 * 
 * <p> 在不允许插入写程序的情况下，写锁可以降级为读锁吗？
 * 是否可以优先于其他正在等待的读取器或写入器将读取锁升级为写入锁？
 *
 * </ul>
 * You should consider all of these things when evaluating the suitability
 * of a given implementation for your application.
 * 
 * <p> 在评估给定实现对应用程序的适用性时，应考虑所有这些因素。
 *
 * @see ReentrantReadWriteLock
 * @see Lock
 * @see ReentrantLock
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface ReadWriteLock {
    /**
     * Returns the lock used for reading.
     * 
     * <p> 返回用于读取的锁。
     *
     * @return the lock used for reading - 用于读取的锁。
     */
    Lock readLock();

    /**
     * Returns the lock used for writing.
     * 
     * <p> 返回用于写入的锁。
     *
     * @return the lock used for writing - 用于写入的锁。
     */
    Lock writeLock();
}
