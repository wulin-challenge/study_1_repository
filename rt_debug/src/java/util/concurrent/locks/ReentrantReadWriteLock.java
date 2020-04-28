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
import java.util.concurrent.TimeUnit;
import java.util.Collection;

/**
 * An implementation of {@link ReadWriteLock} supporting similar
 * semantics to {@link ReentrantLock}.
 * 
 * <p> ReadWriteLock的实现，支持与ReentrantLock相似的语义。
 * 
 * <p>This class has the following properties:
 * 
 * <p> 此类具有以下属性：
 *
 * <ul>
 * <li><b>Acquisition order</b>
 * 
 * <p> 采购订单
 *
 * <p>This class does not impose a reader or writer preference
 * ordering for lock access.  However, it does support an optional
 * <em>fairness</em> policy.
 * 
 * <p> 此类不对锁定访问强加读取器或写入器首选项顺序。但是，它确实支持可选的公平性政策。
 *
 * <dl>
 * <dt><b><i>Non-fair mode (default)</i></b>
 * 
 * <p> 非公平模式（默认）
 * 
 * <dd>When constructed as non-fair (the default), the order of entry
 * to the read and write lock is unspecified, subject to reentrancy
 * constraints.  A nonfair lock that is continuously contended may
 * indefinitely postpone one or more reader or writer threads, but
 * will normally have higher throughput than a fair lock.
 * 
 * <p> 当构造为非公平（默认）时，不受重入限制，未指定读写锁的输入顺序。连续竞争的非公平锁可能会无
 * 限期地延迟一个或多个读取器或写入器线程，但通常比公平锁具有更高的吞吐量。
 *
 * <dt><b><i>Fair mode</i></b>
 * 
 * <p> 公平模式
 * 
 * <dd>When constructed as fair, threads contend for entry using an
 * approximately arrival-order policy. When the currently held lock
 * is released, either the longest-waiting single writer thread will
 * be assigned the write lock, or if there is a group of reader threads
 * waiting longer than all waiting writer threads, that group will be
 * assigned the read lock.
 * 
 * <p> 公平地构造时，线程使用近似到达顺序策略竞争进入。释放当前持有的锁时，将为等待时间最长
 * 的单个写程序线程分配写锁定，或者如果有一组读取器线程的等待时间长于所有等待的写程序线程，则将为该组分配读锁定。
 *
 * <p>A thread that tries to acquire a fair read lock (non-reentrantly)
 * will block if either the write lock is held, or there is a waiting
 * writer thread. The thread will not acquire the read lock until
 * after the oldest currently waiting writer thread has acquired and
 * released the write lock. Of course, if a waiting writer abandons
 * its wait, leaving one or more reader threads as the longest waiters
 * in the queue with the write lock free, then those readers will be
 * assigned the read lock.
 * 
 * <p> 如果持有写入锁或有一个正在等待的写入器线程，则试图（非可重入）获取公平读取锁定的线程将阻塞。
 * 直到当前等待时间最久的写入器线程获得并释放写入锁后，该线程才会获取读取锁。当然，如果等待中的写作者放
 * 弃了等待，将一个或多个阅读器线程留为队列中最长的等待者，并且没有写锁定，那么将为这些阅读器分配读锁定。
 *
 * <p>A thread that tries to acquire a fair write lock (non-reentrantly)
 * will block unless both the read lock and write lock are free (which
 * implies there are no waiting threads).  (Note that the non-blocking
 * {@link ReadLock#tryLock()} and {@link WriteLock#tryLock()} methods
 * do not honor this fair setting and will immediately acquire the lock
 * if it is possible, regardless of waiting threads.)
 * 
 * <p> 尝试获取公平的写锁（非可重入）的线程将阻塞，除非读锁和写锁均处于空闲状态（这意味着没有等待的线程）。 
 * （请注意，非阻塞的ReadLock.tryLock（）和WriteLock.tryLock（）方法不遵循此公平设置，并且如果可
 * 能的话，将立即获取锁定，而不管等待线程如何。）
 * 
 * <p>
 * </dl>
 *
 * <li><b>Reentrancy</b>
 * <p> 可重入
 *
 * <p>This lock allows both readers and writers to reacquire read or
 * write locks in the style of a {@link ReentrantLock}. Non-reentrant
 * readers are not allowed until all write locks held by the writing
 * thread have been released.
 * 
 * <p> 此锁允许读取者和写入者以ReentrantLock的样式重新获取读取或写入锁定。在释放写线程持有的所有写锁之前，
 * 不允许非可重入读者。
 *
 * <p>Additionally, a writer can acquire the read lock, but not
 * vice-versa.  Among other applications, reentrancy can be useful
 * when write locks are held during calls or callbacks to methods that
 * perform reads under read locks.  If a reader tries to acquire the
 * write lock it will never succeed.
 * 
 * <p> 此外，写者可以获取读锁，反之则不能。在其他应用程序中，当在调用或回调对在读锁下执行读取的方法的过程中保持写锁时，
 * 重新进入很有用。如果读者试图获取写锁，它将永远不会成功。
 *
 * <li><b>Lock downgrading</b>
 * 
 * <p> 锁降级
 * 
 * <p>Reentrancy also allows downgrading from the write lock to a read lock,
 * by acquiring the write lock, then the read lock and then releasing the
 * write lock. However, upgrading from a read lock to the write lock is
 * <b>not</b> possible.
 * 
 * <p> 重入还可以通过获取写锁，然后读锁和释放写锁的方式，从写锁降级为读锁。但是，无法从读取锁升级到写入锁。
 *
 * <li><b>Interruption of lock acquisition</b>
 * 
 * <p> 中断锁获取
 * 
 * <p>The read lock and write lock both support interruption during lock
 * acquisition.
 * 
 * <p> 读锁和写锁都支持在锁获取期间中断。
 *
 * <li><b>{@link Condition} support</b>
 * 
 * <p> 条件支持
 * 
 * <p>The write lock provides a {@link Condition} implementation that
 * behaves in the same way, with respect to the write lock, as the
 * {@link Condition} implementation provided by
 * {@link ReentrantLock#newCondition} does for {@link ReentrantLock}.
 * This {@link Condition} can, of course, only be used with the write lock.
 * 
 * <p> 写锁提供的Condition实现与写锁的行为方式相同，就像ReentrantLock.newCondition为
 * ReentrantLock提供的Condition实现一样。当然，此条件只能与写锁一起使用。
 *
 * <p>The read lock does not support a {@link Condition} and
 * {@code readLock().newCondition()} throws
 * {@code UnsupportedOperationException}.
 * 
 * <p> 读锁不支持Condition，而readLock（）。newCondition（）引发
 * UnsupportedOperationException。
 *
 * <li><b>Instrumentation</b>
 * 
 * <p> 仪器
 * 
 * <p>This class supports methods to determine whether locks
 * are held or contended. These methods are designed for monitoring
 * system state, not for synchronization control.
 * 
 * <p> 此类支持确定是持有锁还是争用锁的方法。这些方法设计用于监视系统状态，而不用于同步控制。
 * 
 * </ul>
 *
 * <p>Serialization of this class behaves in the same way as built-in
 * locks: a deserialized lock is in the unlocked state, regardless of
 * its state when serialized.
 * 
 * <p> 此类的序列化与内置锁的行为相同：反序列化的锁处于解锁状态，而不管序列化时的状态如何。
 *
 * <p><b>Sample usages</b>. Here is a code sketch showing how to perform
 * lock downgrading after updating a cache (exception handling is
 * particularly tricky when handling multiple locks in a non-nested
 * fashion):
 * 
 * <p> 用法示例。这是一个代码草图，显示了在更新缓存后如何执行锁降级（以非嵌套方式处理多个锁时，异常处理特别棘手）：
 *
 * <pre> {@code
 * class CachedData {
 *   Object data;
 *   volatile boolean cacheValid;
 *   final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
 *
 *   void processCachedData() {
 *     rwl.readLock().lock();
 *     if (!cacheValid) {
 *       // Must release read lock before acquiring write lock
 *       rwl.readLock().unlock();
 *       rwl.writeLock().lock();
 *       try {
 *         // Recheck state because another thread might have
 *         // acquired write lock and changed state before we did.
 *         if (!cacheValid) {
 *           data = ...
 *           cacheValid = true;
 *         }
 *         // Downgrade by acquiring read lock before releasing write lock
 *         rwl.readLock().lock();
 *       } finally {
 *         rwl.writeLock().unlock(); // Unlock write, still hold read
 *       }
 *     }
 *
 *     try {
 *       use(data);
 *     } finally {
 *       rwl.readLock().unlock();
 *     }
 *   }
 * }}</pre>
 *
 * ReentrantReadWriteLocks can be used to improve concurrency in some
 * uses of some kinds of Collections. This is typically worthwhile
 * only when the collections are expected to be large, accessed by
 * more reader threads than writer threads, and entail operations with
 * overhead that outweighs synchronization overhead. For example, here
 * is a class using a TreeMap that is expected to be large and
 * concurrently accessed.
 * 
 * <p> ReentrantReadWriteLocks可用于提高某些种类的Collection的并发性。 仅当预期集合很大，
 * 由读取器线程而不是写入器线程访问更多读取器线程，并且需要的操作开销大于同步开销时，这通常才是值得的。 
 * 例如，这是一个使用TreeMap的类，该类应该很大并且可以同时访问。
 * 
 *
 *  <pre> {@code
 * class RWDictionary {
 *   private final Map<String, Data> m = new TreeMap<String, Data>();
 *   private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
 *   private final Lock r = rwl.readLock();
 *   private final Lock w = rwl.writeLock();
 *
 *   public Data get(String key) {
 *     r.lock();
 *     try { return m.get(key); }
 *     finally { r.unlock(); }
 *   }
 *   public String[] allKeys() {
 *     r.lock();
 *     try { return m.keySet().toArray(); }
 *     finally { r.unlock(); }
 *   }
 *   public Data put(String key, Data value) {
 *     w.lock();
 *     try { return m.put(key, value); }
 *     finally { w.unlock(); }
 *   }
 *   public void clear() {
 *     w.lock();
 *     try { m.clear(); }
 *     finally { w.unlock(); }
 *   }
 * }}</pre>
 *
 * <h3>Implementation Notes</h3>
 * 
 * <p> 实施说明
 *
 * <p>This lock supports a maximum of 65535 recursive write locks
 * and 65535 read locks. Attempts to exceed these limits result in
 * {@link Error} throws from locking methods.
 * 
 * <p> 此锁最多支持65535个递归写锁和65535个读锁。 尝试超过这些限制会导致锁定方法引发错误。
 *
 * @since 1.5
 * @author Doug Lea
 */
public class ReentrantReadWriteLock
        implements ReadWriteLock, java.io.Serializable {
    private static final long serialVersionUID = -6992448646407690164L;
    /** 
     * Inner class providing readlock 
     * 
     * <p> 提供读锁的内部类
     */
    private final ReentrantReadWriteLock.ReadLock readerLock;
    /** 
     * Inner class providing writelock 
     * 
     * <p> 内部类提供写锁
     */
    private final ReentrantReadWriteLock.WriteLock writerLock;
    /** 
     * Performs all synchronization mechanics 
     * 
     * <p> 执行所有同步机制
     */
    final Sync sync;

    /**
     * Creates a new {@code ReentrantReadWriteLock} with
     * default (nonfair) ordering properties.
     * 
     * <p> 使用默认（不公平）订购属性创建一个新的ReentrantReadWriteLock
     */
    public ReentrantReadWriteLock() {
        this(false);
    }

    /**
     * Creates a new {@code ReentrantReadWriteLock} with
     * the given fairness policy.
     * 
     * <p> 使用给定的公平性策略创建一个新的ReentrantReadWriteLock。
     *
     * @param fair {@code true} if this lock should use a fair ordering policy
     * 
     * <p> 如果此锁应使用公平的订购策略，则为true
     */
    public ReentrantReadWriteLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
        readerLock = new ReadLock(this);
        writerLock = new WriteLock(this);
    }

    public ReentrantReadWriteLock.WriteLock writeLock() { return writerLock; }
    public ReentrantReadWriteLock.ReadLock  readLock()  { return readerLock; }

    /**
     * Synchronization implementation for ReentrantReadWriteLock.
     * Subclassed into fair and nonfair versions.
     * 
     * <p> ReentrantReadWriteLock的同步实现。 分为公平和非公平版本。
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 6317671515068378041L;

        /*
         * Read vs write count extraction constants and functions.
         * Lock state is logically divided into two unsigned shorts:
         * The lower one representing the exclusive (writer) lock hold count,
         * and the upper the shared (reader) hold count.
         * 
         * <p> 读取与写入计数提取常量和函数。 锁状态在逻辑上分为两个无符号的短裤：较低的一个表示排他（写）锁保持计数，
         * 较高的一个表示共享（读）锁保持计数。
         */

        static final int SHARED_SHIFT   = 16;
        static final int SHARED_UNIT    = (1 << SHARED_SHIFT);
        static final int MAX_COUNT      = (1 << SHARED_SHIFT) - 1;
        static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;

        /** 
         * Returns the number of shared holds represented in count  
         * 
         * <p> 返回以count表示的共享保留数
         */
        static int sharedCount(int c)    { return c >>> SHARED_SHIFT; }
        /** 
         * Returns the number of exclusive holds represented in count  
         * 
         * <p> 返回以count表示的独占保留数
         */
        static int exclusiveCount(int c) { return c & EXCLUSIVE_MASK; }

        /**
         * A counter for per-thread read hold counts.
         * Maintained as a ThreadLocal; cached in cachedHoldCounter
         * 
         * <p> 每线程读取保持计数的计数器。 维护为ThreadLocal; 缓存在cachedHoldCounter中
         */
        static final class HoldCounter {
            int count = 0;
            // Use id, not reference, to avoid garbage retention
            // 使用id而不是引用以避免垃圾保留
            final long tid = getThreadId(Thread.currentThread());
        }

        /**
         * ThreadLocal subclass. Easiest to explicitly define for sake
         * of deserialization mechanics.
         * 
         * <p> ThreadLocal子类。 为了进行反序列化，最容易明确定义。
         */
        static final class ThreadLocalHoldCounter
            extends ThreadLocal<HoldCounter> {
            public HoldCounter initialValue() {
                return new HoldCounter();
            }
        }

        /**
         * The number of reentrant read locks held by current thread.
         * Initialized only in constructor and readObject.
         * Removed whenever a thread's read hold count drops to 0.
         * 
         * <p> 当前线程持有的可重入读锁的数量。 仅在构造函数和readObject中初始化。 
         * 每当线程的读取保持计数降至0时将其删除。
         */
        private transient ThreadLocalHoldCounter readHolds;

        /**
         * The hold count of the last thread to successfully acquire
         * readLock. This saves ThreadLocal lookup in the common case
         * where the next thread to release is the last one to
         * acquire. This is non-volatile since it is just used
         * as a heuristic, and would be great for threads to cache.
         * 
         * <p> 成功获取readLock的最后一个线程的保留计数。 在下一个要释放的线程是最后一个要获取的线程的常见情况下，
         * 这可以节省ThreadLocal查找。 这是非易失性的，因为它仅用作启发式方法，对于线程进行缓存非常有用。
         *
         * <p>Can outlive the Thread for which it is caching the read
         * hold count, but avoids garbage retention by not retaining a
         * reference to the Thread.
         * 
         * <p> 可以使正在为其缓存读取保留计数的线程超时，但是可以通过不保留对线程的引用来避免垃圾保留。
         *
         * <p>Accessed via a benign data race; relies on the memory
         * model's final field and out-of-thin-air guarantees.
         * 
         * <p> 通过良性数据竞赛访问； 依赖于内存模型的最终字段和空中保证。
         */
        private transient HoldCounter cachedHoldCounter;

        /**
         * firstReader is the first thread to have acquired the read lock.
         * firstReaderHoldCount is firstReader's hold count.
         * 
         * <p> firstReader是第一个获得读取锁定的线程。 firstReaderHoldCount是
         * firstReader的保留计数。
         *
         * <p>More precisely, firstReader is the unique thread that last
         * changed the shared count from 0 to 1, and has not released the
         * read lock since then; null if there is no such thread.
         * 
         * <p> 更精确地说，firstReader是唯一一个线程，它最后一次将共享计数从0更改为1，
         * 并且此后没有释放读取锁； 如果没有这样的线程，则返回null。
         *
         * <p>Cannot cause garbage retention unless the thread terminated
         * without relinquishing its read locks, since tryReleaseShared
         * sets it to null.
         * 
         * <p> 除非线程在不放弃读锁的情况下终止，否则不会导致垃圾回收，因为
         * tryReleaseShared将其设置为null。
         *
         * <p>Accessed via a benign data race; relies on the memory
         * model's out-of-thin-air guarantees for references.
         * 
         * <p> 通过良性数据竞赛访问； 依赖于内存模型的超薄保证作为参考。
         *
         * <p>This allows tracking of read holds for uncontended read
         * locks to be very cheap.
         * 
         * <p> 这允许跟踪无竞争读取锁的读取保持非常便宜。
         */
        private transient Thread firstReader = null;
        private transient int firstReaderHoldCount;

        Sync() {
            readHolds = new ThreadLocalHoldCounter();
            setState(getState()); // ensures visibility of readHolds - 确保readHolds的可见性
        }

        /*
         * Acquires and releases use the same code for fair and
         * nonfair locks, but differ in whether/how they allow barging
         * when queues are non-empty.
         * 
         * <p> 获取和释放对公平锁和非公平锁使用相同的代码，但是在队列为非空时它们是否/如何允许插入的方式不同。
         */

        /**
         * Returns true if the current thread, when trying to acquire
         * the read lock, and otherwise eligible to do so, should block
         * because of policy for overtaking other waiting threads.
         * 
         * <p> 如果当前线程在尝试获取读锁时（否则有资格这样做）由于超越其他等待线程的策略而阻塞，则返回true。
         */
        abstract boolean readerShouldBlock();

        /**
         * Returns true if the current thread, when trying to acquire
         * the write lock, and otherwise eligible to do so, should block
         * because of policy for overtaking other waiting threads.
         * 
         * <p> 如果当前线程在尝试获取写锁时（否则有资格这样做）由于策略超过其他等待线程而应阻塞，则返回true。
         */
        abstract boolean writerShouldBlock();

        /*
         * Note that tryRelease and tryAcquire can be called by
         * Conditions. So it is possible that their arguments contain
         * both read and write holds that are all released during a
         * condition wait and re-established in tryAcquire.
         * 
         * <p> 请注意，条件可以调用tryRelease和tryAcquire。 因此，它们的参数可能包含读和写保留，
         * 它们在条件等待期间全部释放，并在tryAcquire中重新建立。
         */

        protected final boolean tryRelease(int releases) {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int nextc = getState() - releases;
            boolean free = exclusiveCount(nextc) == 0;
            if (free)
                setExclusiveOwnerThread(null);
            setState(nextc);
            return free;
        }

        protected final boolean tryAcquire(int acquires) {
            /*
             * Walkthrough:
             * 
             * <p> 演练
             * 
             * <p> 1. If read count nonzero or write count nonzero
             *    and owner is a different thread, fail.
             *    
             * <p> 1. 如果读取计数非零或写入计数非零且所有者是另一个线程，则失败。
             * 
             * <p> 2. If count would saturate, fail. (This can only
             *    happen if count is already nonzero.)
             *    
             * <p> 2.如果计数将饱和，则失败。 （只有在count已经不为零时，才可能发生这种情况。）
             * 
             * <p> 3. Otherwise, this thread is eligible for lock if
             *    it is either a reentrant acquire or
             *    queue policy allows it. If so, update state
             *    and set owner.
             *    
             * <p> 3.否则，如果该线程是可重入获取或队列策略允许的话，则有资格进行锁定。 
             * 如果是这样，请更新状态并设置所有者。
             */
            Thread current = Thread.currentThread();
            int c = getState();
            int w = exclusiveCount(c);
            if (c != 0) {
                // (Note: if c != 0 and w == 0 then shared count != 0)
                if (w == 0 || current != getExclusiveOwnerThread())
                    return false;
                if (w + exclusiveCount(acquires) > MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                // Reentrant acquire
                setState(c + acquires);
                return true;
            }
            if (writerShouldBlock() ||
                !compareAndSetState(c, c + acquires))
                return false;
            setExclusiveOwnerThread(current);
            return true;
        }

        protected final boolean tryReleaseShared(int unused) {
            Thread current = Thread.currentThread();
            if (firstReader == current) {
                // assert firstReaderHoldCount > 0;
                if (firstReaderHoldCount == 1)
                    firstReader = null;
                else
                    firstReaderHoldCount--;
            } else {
                HoldCounter rh = cachedHoldCounter;
                if (rh == null || rh.tid != getThreadId(current))
                    rh = readHolds.get();
                int count = rh.count;
                if (count <= 1) {
                    readHolds.remove();
                    if (count <= 0)
                        throw unmatchedUnlockException();
                }
                --rh.count;
            }
            for (;;) {
                int c = getState();
                int nextc = c - SHARED_UNIT;
                if (compareAndSetState(c, nextc))
                    // Releasing the read lock has no effect on readers,
                    // but it may allow waiting writers to proceed if
                    // both read and write locks are now free.
                	
                	// 释放读取锁定对读取器没有影响，但是如果现在读取和写入锁定均已释放，则可能允许等待的写入器继续进行。
                    return nextc == 0;
            }
        }

        private IllegalMonitorStateException unmatchedUnlockException() {
            return new IllegalMonitorStateException(
                "attempt to unlock read lock, not locked by current thread");
        }

        protected final int tryAcquireShared(int unused) {
            /*
             * Walkthrough:
             * 
             * <p> 演练
             * 
             * <p> 1. If write lock held by another thread, fail.
             * 
             * <p> 1.如果另一个线程持有写锁定，则失败。
             * 
             * <p> 2. Otherwise, this thread is eligible for
             *    lock wrt state, so ask if it should block
             *    because of queue policy. If not, try
             *    to grant by CASing state and updating count.
             *    Note that step does not check for reentrant
             *    acquires, which is postponed to full version
             *    to avoid having to check hold count in
             *    the more typical non-reentrant case.
             *    
             * <p> 2.否则，此线程符合锁定wrt状态的条件，因此请问是否由于队列策略而应阻塞。 如果不是，
             * 请尝试按CASing状态授予许可并更新计数。 请注意，该步骤不检查重入获取，这将推迟到完整版本，
             * 以避免在更典型的非重入情况下必须检查保留计数。
             * 
             * <p> 3. If step 2 fails either because thread
             *    apparently not eligible or CAS fails or count
             *    saturated, chain to version with full retry loop.
             *    
             * <p> 3.如果第2步失败，或者由于线程显然不合格或者CAS失败或计数饱和，请使用完全重试循环链接到版本。
             */
            Thread current = Thread.currentThread();
            int c = getState();
            if (exclusiveCount(c) != 0 &&
                getExclusiveOwnerThread() != current)
                return -1;
            int r = sharedCount(c);
            if (!readerShouldBlock() &&
                r < MAX_COUNT &&
                compareAndSetState(c, c + SHARED_UNIT)) {
                if (r == 0) {
                    firstReader = current;
                    firstReaderHoldCount = 1;
                } else if (firstReader == current) {
                    firstReaderHoldCount++;
                } else {
                    HoldCounter rh = cachedHoldCounter;
                    if (rh == null || rh.tid != getThreadId(current))
                        cachedHoldCounter = rh = readHolds.get();
                    else if (rh.count == 0)
                        readHolds.set(rh);
                    rh.count++;
                }
                return 1;
            }
            return fullTryAcquireShared(current);
        }

        /**
         * Full version of acquire for reads, that handles CAS misses
         * and reentrant reads not dealt with in tryAcquireShared.
         * 
         * <p> 读取的完整版本，可处理tryAcquireShared中未处理的CAS丢失和可重入的读取。
         */
        final int fullTryAcquireShared(Thread current) {
            /*
             * This code is in part redundant with that in
             * tryAcquireShared but is simpler overall by not
             * complicating tryAcquireShared with interactions between
             * retries and lazily reading hold counts.
             * 
             * <p> 该代码与tryAcquireShared中的代码部分冗余，但总体上更简单，因为不使
             * tryAcquireShared与重试和延迟读取保持计数之间的交互复杂化。
             */
            HoldCounter rh = null;
            for (;;) {
                int c = getState();
                if (exclusiveCount(c) != 0) {
                    if (getExclusiveOwnerThread() != current)
                        return -1;
                    // else we hold the exclusive lock; blocking here
                    // would cause deadlock.
                    
                    // 否则我们将持有排他锁； 在这里阻塞将导致死锁。
                } else if (readerShouldBlock()) {
                    // Make sure we're not acquiring read lock reentrantly
                	// 确保我们不会再获取读锁
                    if (firstReader == current) {
                        // assert firstReaderHoldCount > 0;
                    } else {
                        if (rh == null) {
                            rh = cachedHoldCounter;
                            if (rh == null || rh.tid != getThreadId(current)) {
                                rh = readHolds.get();
                                if (rh.count == 0)
                                    readHolds.remove();
                            }
                        }
                        if (rh.count == 0)
                            return -1;
                    }
                }
                if (sharedCount(c) == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                if (compareAndSetState(c, c + SHARED_UNIT)) {
                    if (sharedCount(c) == 0) {
                        firstReader = current;
                        firstReaderHoldCount = 1;
                    } else if (firstReader == current) {
                        firstReaderHoldCount++;
                    } else {
                        if (rh == null)
                            rh = cachedHoldCounter;
                        if (rh == null || rh.tid != getThreadId(current))
                            rh = readHolds.get();
                        else if (rh.count == 0)
                            readHolds.set(rh);
                        rh.count++;
                        cachedHoldCounter = rh; // cache for release
                    }
                    return 1;
                }
            }
        }

        /**
         * Performs tryLock for write, enabling barging in both modes.
         * This is identical in effect to tryAcquire except for lack
         * of calls to writerShouldBlock.
         * 
         * <p> 执行tryLock进行写入，从而在两种模式下都启用插入。 除了缺少对writerShouldBlock的调用之外，
         * 这与tryAcquire的作用相同。
         */
        final boolean tryWriteLock() {
            Thread current = Thread.currentThread();
            int c = getState();
            if (c != 0) {
                int w = exclusiveCount(c);
                if (w == 0 || current != getExclusiveOwnerThread())
                    return false;
                if (w == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
            }
            if (!compareAndSetState(c, c + 1))
                return false;
            setExclusiveOwnerThread(current);
            return true;
        }

        /**
         * Performs tryLock for read, enabling barging in both modes.
         * This is identical in effect to tryAcquireShared except for
         * lack of calls to readerShouldBlock.
         * 
         * <p> 执行tryLock以进行读取，从而在两种模式下都可以进行插入。 除了缺少对readerShouldBlock的调用外，
         * 这与tryAcquireShared的作用相同。
         */
        final boolean tryReadLock() {
            Thread current = Thread.currentThread();
            for (;;) {
                int c = getState();
                if (exclusiveCount(c) != 0 &&
                    getExclusiveOwnerThread() != current)
                    return false;
                int r = sharedCount(c);
                if (r == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                if (compareAndSetState(c, c + SHARED_UNIT)) {
                    if (r == 0) {
                        firstReader = current;
                        firstReaderHoldCount = 1;
                    } else if (firstReader == current) {
                        firstReaderHoldCount++;
                    } else {
                        HoldCounter rh = cachedHoldCounter;
                        if (rh == null || rh.tid != getThreadId(current))
                            cachedHoldCounter = rh = readHolds.get();
                        else if (rh.count == 0)
                            readHolds.set(rh);
                        rh.count++;
                    }
                    return true;
                }
            }
        }

        protected final boolean isHeldExclusively() {
            // While we must in general read state before owner,
            // we don't need to do so to check if current thread is owner
        	
        	// 虽然我们必须在拥有者之前先以一般状态读取状态，但我们不需要这样做就可以检查当前线程是否为拥有者
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        // Methods relayed to outer class
        
        // 与外部类有关的方法

        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        final Thread getOwner() {
            // Must read state before owner to ensure memory consistency
        	// 必须在拥有者之前读取状态，以确保内存一致性
            return ((exclusiveCount(getState()) == 0) ?
                    null :
                    getExclusiveOwnerThread());
        }

        final int getReadLockCount() {
            return sharedCount(getState());
        }

        final boolean isWriteLocked() {
            return exclusiveCount(getState()) != 0;
        }

        final int getWriteHoldCount() {
            return isHeldExclusively() ? exclusiveCount(getState()) : 0;
        }

        final int getReadHoldCount() {
            if (getReadLockCount() == 0)
                return 0;

            Thread current = Thread.currentThread();
            if (firstReader == current)
                return firstReaderHoldCount;

            HoldCounter rh = cachedHoldCounter;
            if (rh != null && rh.tid == getThreadId(current))
                return rh.count;

            int count = readHolds.get().count;
            if (count == 0) readHolds.remove();
            return count;
        }

        /**
         * Reconstitutes the instance from a stream (that is, deserializes it).
         * 
         * <p> 从流中重构实例（即反序列化它）。
         */
        private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            readHolds = new ThreadLocalHoldCounter();
            setState(0); // reset to unlocked state - 重置为解锁状态
        }

        final int getCount() { return getState(); }
    }

    /**
     * Nonfair version of Sync
     * 
     * <p> 非同步版本
     */
    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = -8159625535654395037L;
        final boolean writerShouldBlock() {
            return false; // writers can always barge
        }
        final boolean readerShouldBlock() {
            /* As a heuristic to avoid indefinite writer starvation,
             * block if the thread that momentarily appears to be head
             * of queue, if one exists, is a waiting writer.  This is
             * only a probabilistic effect since a new reader will not
             * block if there is a waiting writer behind other enabled
             * readers that have not yet drained from the queue.
             * 
             * <p> 为了避免无限期地饿死作家，请试一试，如果暂时看起来是队列头的线程（如果存在）则阻塞，
             * 等待作家。 这只是一种概率效应，因为如果在其他启用的读取器后面还没有等待中的写入器还没有从队列中耗尽，
             * 那么新的读取器将不会阻塞。
             */
            return apparentlyFirstQueuedIsExclusive();
        }
    }

    /**
     * Fair version of Sync
     * 
     * <p> 同步的公平版本
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = -2274990926593161451L;
        final boolean writerShouldBlock() {
            return hasQueuedPredecessors();
        }
        final boolean readerShouldBlock() {
            return hasQueuedPredecessors();
        }
    }

    /**
     * The lock returned by method {@link ReentrantReadWriteLock#readLock}.
     * 
     * <p> 方法ReentrantReadWriteLock.readLock返回的锁。
     */
    public static class ReadLock implements Lock, java.io.Serializable {
        private static final long serialVersionUID = -5992448646407690164L;
        private final Sync sync;

        /**
         * Constructor for use by subclasses
         * 
         * <p> 子类使用的构造方法
         *
         * @param lock the outer lock object
         * 
         * <p> 外锁对象
         * 
         * @throws NullPointerException if the lock is null
         */
        protected ReadLock(ReentrantReadWriteLock lock) {
            sync = lock.sync;
        }

        /**
         * Acquires the read lock.
         * 
         * <p> 获取读取锁。
         *
         * <p>Acquires the read lock if the write lock is not held by
         * another thread and returns immediately.
         * 
         * <p> 如果写锁没有被另一个线程持有，则获取读锁并立即返回。
         *
         * <p>If the write lock is held by another thread then
         * the current thread becomes disabled for thread scheduling
         * purposes and lies dormant until the read lock has been acquired.
         * 
         * <p> 如果写锁由另一个线程持有，则出于线程调度目的，当前线程将被禁用，并处于休眠状态，
         * 直到获取读锁为止。
         */
        public void lock() {
            sync.acquireShared(1);
        }

        /**
         * Acquires the read lock unless the current thread is
         * {@linkplain Thread#interrupt interrupted}.
         * 
         * <p> 除非当前线程被中断，否则获取读锁定。
         *
         * <p>Acquires the read lock if the write lock is not held
         * by another thread and returns immediately.
         * 
         * <p> 如果写锁没有被另一个线程持有，则获取读锁并立即返回。
         *
         * <p>If the write lock is held by another thread then the
         * current thread becomes disabled for thread scheduling
         * purposes and lies dormant until one of two things happens:
         * 
         * <p> 如果写锁定由另一个线程持有，则出于线程调度目的，当前线程将被禁用，并且在发生以下两种情况之一之前，
         * 它处于休眠状态：
         *
         * <ul>
         *
         * <li>The read lock is acquired by the current thread; or
         * 
         * <p> 读取锁由当前线程获取； 要么
         *
         * <li>Some other thread {@linkplain Thread#interrupt interrupts}
         * the current thread.
         * 
         * <p> 其他一些线程中断当前线程。
         *
         * </ul>
         *
         * <p>If the current thread:
         * 
         * <p> 如果当前线程：
         *
         * <ul>
         *
         * <li>has its interrupted status set on entry to this method; or
         * 
         * <p> 在进入此方法时已设置其中断状态； 要么
         *
         * <li>is {@linkplain Thread#interrupt interrupted} while
         * acquiring the read lock,
         * 
         * <p> 在获取读锁时被中断，
         *
         * </ul>
         *
         * then {@link InterruptedException} is thrown and the current
         * thread's interrupted status is cleared.
         * 
         * <p> 然后抛出InterruptedException并清除当前线程的中断状态。
         *
         * <p>In this implementation, as this method is an explicit
         * interruption point, preference is given to responding to
         * the interrupt over normal or reentrant acquisition of the
         * lock.
         * 
         * <p> 在此实现中，由于此方法是显式的中断点，因此优先于对中断的响应而不是正常或可重入的锁获取。
         *
         * @throws InterruptedException if the current thread is interrupted
         * 
         * <p> 如果当前线程被中断
         */
        public void lockInterruptibly() throws InterruptedException {
            sync.acquireSharedInterruptibly(1);
        }

        /**
         * Acquires the read lock only if the write lock is not held by
         * another thread at the time of invocation.
         * 
         * <p> 仅当调用时另一个线程未持有写锁时才获取读锁。
         *
         * <p>Acquires the read lock if the write lock is not held by
         * another thread and returns immediately with the value
         * {@code true}. Even when this lock has been set to use a
         * fair ordering policy, a call to {@code tryLock()}
         * <em>will</em> immediately acquire the read lock if it is
         * available, whether or not other threads are currently
         * waiting for the read lock.  This &quot;barging&quot; behavior
         * can be useful in certain circumstances, even though it
         * breaks fairness. If you want to honor the fairness setting
         * for this lock, then use {@link #tryLock(long, TimeUnit)
         * tryLock(0, TimeUnit.SECONDS) } which is almost equivalent
         * (it also detects interruption).
         * 
         * <p> 如果写锁未被另一个线程持有，则获取读锁，并立即返回true值。 即使已将此锁设置为使用公平的排序策略，
         * 对tryLock（）的调用也会立即获取读取锁（如果可用），无论其他线程当前是否在等待读取锁。 即使破坏公平性，
         * 这种“讨价还价”的行为在某些情况下还是有用的。 如果要遵守此锁的公平性设置，请使用几乎等效的
         * tryLock（0，TimeUnit.SECONDS）（它还会检测到中断）。
         *
         * <p>If the write lock is held by another thread then
         * this method will return immediately with the value
         * {@code false}.
         * 
         * <p> 如果写锁由另一个线程持有，则此方法将立即返回false值。
         *
         * @return {@code true} if the read lock was acquired
         * 
         * <p> 如果已获得读取锁，则为true
         */
        public boolean tryLock() {
            return sync.tryReadLock();
        }

        /**
         * Acquires the read lock if the write lock is not held by
         * another thread within the given waiting time and the
         * current thread has not been {@linkplain Thread#interrupt
         * interrupted}.
         * 
         * <p> 如果在给定的等待时间内另一个线程未持有写锁定，并且当前线程尚未中断，则获取读锁定。
         *
         * <p>Acquires the read lock if the write lock is not held by
         * another thread and returns immediately with the value
         * {@code true}. If this lock has been set to use a fair
         * ordering policy then an available lock <em>will not</em> be
         * acquired if any other threads are waiting for the
         * lock. This is in contrast to the {@link #tryLock()}
         * method. If you want a timed {@code tryLock} that does
         * permit barging on a fair lock then combine the timed and
         * un-timed forms together:
         * 
         * <p> 如果写锁未被另一个线程持有，则获取读锁，并立即返回true值。 如果将此锁设置为使用公平的排序策略，
         * 那么如果有任何其他线程在等待该锁，则不会获取可用锁。 这与tryLock（）方法相反。 如果您想要一个定时
         * tryLock确实允许在公平锁上插入，则将定时和非定时形式组合在一起：
         *
         *  <pre> {@code
         * if (lock.tryLock() ||
         *     lock.tryLock(timeout, unit)) {
         *   ...
         * }}</pre>
         *
         * <p>If the write lock is held by another thread then the
         * current thread becomes disabled for thread scheduling
         * purposes and lies dormant until one of three things happens:
         * 
         * <p> 如果写锁定由另一个线程持有，则出于线程调度目的，当前线程将被禁用，并且在发生以下三种情况之一之前，它处于休眠状态：
         *
         * <ul>
         *
         * <li>The read lock is acquired by the current thread; or
         * 
         * <p> 读取锁由当前线程获取； 要么
         *
         * <li>Some other thread {@linkplain Thread#interrupt interrupts}
         * the current thread; or
         * 
         * <p> 其他一些线程中断当前线程； 要么
         *
         * <li>The specified waiting time elapses.
         *
         * <p> 经过了指定的等待时间。
         * </ul>
         *
         * <p>If the read lock is acquired then the value {@code true} is
         * returned.
         * 
         * <p> 如果获取了读锁，则返回值true。
         *
         * <p>If the current thread:
         * 
         * <p> 如果当前线程：
         *
         * <ul>
         *
         * <li>has its interrupted status set on entry to this method; or
         * 
         * <p> 在进入此方法时已设置其中断状态； 要么
         *
         * <li>is {@linkplain Thread#interrupt interrupted} while
         * acquiring the read lock,
         * 
         * <p> 在获取读锁时被中断，
         *
         * </ul> 
         * 
         * <p> then {@link InterruptedException} is thrown and the
         * current thread's interrupted status is cleared.
         * 
         * <p> 然后抛出InterruptedException并清除当前线程的中断状态。
         *
         * <p>If the specified waiting time elapses then the value
         * {@code false} is returned.  If the time is less than or
         * equal to zero, the method will not wait at all.
         * 
         * <p> 如果经过了指定的等待时间，则返回值false。 如果时间小于或等于零，则该方法将根本不等待。
         *
         * <p>In this implementation, as this method is an explicit
         * interruption point, preference is given to responding to
         * the interrupt over normal or reentrant acquisition of the
         * lock, and over reporting the elapse of the waiting time.
         * 
         * <p> 在此实现中，由于此方法是显式的中断点，因此优先于对中断的响应而不是正常或可重入的锁定获取，
         * 而是优先报告等待时间的流逝。
         *
         * @param timeout the time to wait for the read lock
         * 
         * <p> 等待读锁的时间
         * 
         * @param unit the time unit of the timeout argument
         * 
         * <p> 超时参数的时间单位
         * 
         * @return {@code true} if the read lock was acquired
         * 
         * <p> 如果已获得读取锁，则为true
         * 
         * @throws InterruptedException if the current thread is interrupted
         * 
         * <p> 如果当前线程被中断
         * 
         * @throws NullPointerException if the time unit is null
         */
        public boolean tryLock(long timeout, TimeUnit unit)
                throws InterruptedException {
            return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
        }

        /**
         * Attempts to release this lock.
         * 
         * <p> 尝试释放此锁。
         *
         * <p>If the number of readers is now zero then the lock
         * is made available for write lock attempts.
         * 
         * <p> 如果现在读取器的数量为零，则该锁定可用于写锁定尝试。
         */
        public void unlock() {
            sync.releaseShared(1);
        }

        /**
         * Throws {@code UnsupportedOperationException} because
         * {@code ReadLocks} do not support conditions.
         * 
         * <p> 抛出UnsupportedOperationException，因为ReadLocks不支持条件。
         *
         * @throws UnsupportedOperationException always
         */
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns a string identifying this lock, as well as its lock state.
         * The state, in brackets, includes the String {@code "Read locks ="}
         * followed by the number of held read locks.
         * 
         * <p> 返回标识此锁定及其锁定状态的字符串。 括号中的状态包括字符串“ Read locks =”，后跟持有的读锁的数量。
         *
         * @return a string identifying this lock, as well as its lock state
         * 
         * <p> 标识此锁及其锁状态的字符串
         */
        public String toString() {
            int r = sync.getReadLockCount();
            return super.toString() +
                "[Read locks = " + r + "]";
        }
    }

    /**
     * The lock returned by method {@link ReentrantReadWriteLock#writeLock}.
     * 
     * <p> 方法ReentrantReadWriteLock.writeLock返回的锁。
     */
    public static class WriteLock implements Lock, java.io.Serializable {
        private static final long serialVersionUID = -4992448646407690164L;
        private final Sync sync;

        /**
         * Constructor for use by subclasses
         * 
         * <p> 子类使用的构造方法
         *
         * @param lock the outer lock object
         * 
         * <p> 外锁对象
         * 
         * @throws NullPointerException if the lock is null
         */
        protected WriteLock(ReentrantReadWriteLock lock) {
            sync = lock.sync;
        }

        /**
         * Acquires the write lock.
         * 
         * <p> 获取写锁。
         *
         * <p>Acquires the write lock if neither the read nor write lock
         * are held by another thread
         * and returns immediately, setting the write lock hold count to
         * one.
         * 
         * <p> 如果读取和写入锁定均未由另一个线程持有，则获取写入锁定并立即返回，将写入锁定保持计数设置为1。
         *
         * <p>If the current thread already holds the write lock then the
         * hold count is incremented by one and the method returns
         * immediately.
         * 
         * <p> 如果当前线程已经持有写锁，则持有计数将增加一，该方法将立即返回。
         *
         * <p>If the lock is held by another thread then the current
         * thread becomes disabled for thread scheduling purposes and
         * lies dormant until the write lock has been acquired, at which
         * time the write lock hold count is set to one.
         * 
         * <p> 如果锁是由另一个线程持有的，则当前线程将出于线程调度目的而被禁用，并处于休眠状态，
         * 直到获取了写入锁为止，此时写入锁的保持计数被设置为1。
         */
        public void lock() {
            sync.acquire(1);
        }

        /**
         * Acquires the write lock unless the current thread is
         * {@linkplain Thread#interrupt interrupted}.
         * 
         * <p> 除非当前线程被中断，否则获取写锁定。
         *
         * <p>Acquires the write lock if neither the read nor write lock
         * are held by another thread
         * and returns immediately, setting the write lock hold count to
         * one.
         * 
         * <p> 如果读取和写入锁定均未由另一个线程持有，则获取写入锁定并立即返回，将写入锁定保持计数设置为1。
         *
         * <p>If the current thread already holds this lock then the
         * hold count is incremented by one and the method returns
         * immediately.
         * 
         * <p> 如果当前线程已经持有此锁，则持有计数将增加一，并且该方法将立即返回。
         *
         * <p>If the lock is held by another thread then the current
         * thread becomes disabled for thread scheduling purposes and
         * lies dormant until one of two things happens:
         * 
         * <p> 如果该锁由另一个线程持有，则出于线程调度目的，当前线程将被禁用，并处于休眠状态，直到发生以下两种情况之一：
         *
         * <ul>
         *
         * <li>The write lock is acquired by the current thread; or
         * 
         * <p> 写锁被当前线程获取；要么
         *
         * <li>Some other thread {@linkplain Thread#interrupt interrupts}
         * the current thread.
         * 
         * <p> 其他一些线程中断当前线程。
         *
         * </ul>
         *
         * <p>If the write lock is acquired by the current thread then the
         * lock hold count is set to one.
         * 
         * <p> 如果当前线程获取了写锁定，则将锁定保持计数设置为1。
         *
         * <p>If the current thread:
         * 
         * <p> 如果当前线程：
         *
         * <ul>
         *
         * <li>has its interrupted status set on entry to this method;
         * or
         * 
         * <p> 在进入此方法时已设置其中断状态；要么
         *
         * <li>is {@linkplain Thread#interrupt interrupted} while
         * acquiring the write lock,
         * 
         * <p> 在获取写锁定时被中断，
         *
         * </ul>
         *
         * then {@link InterruptedException} is thrown and the current
         * thread's interrupted status is cleared.
         * 
         * <p> 然后抛出InterruptedException并清除当前线程的中断状态。
         *
         * <p>In this implementation, as this method is an explicit
         * interruption point, preference is given to responding to
         * the interrupt over normal or reentrant acquisition of the
         * lock.
         * 
         * <p> 在此实现中，由于此方法是显式的中断点，因此优先于对中断的响应而不是正常或可重入的锁获取。
         *
         * @throws InterruptedException if the current thread is interrupted
         */
        public void lockInterruptibly() throws InterruptedException {
            sync.acquireInterruptibly(1);
        }

        /**
         * Acquires the write lock only if it is not held by another thread
         * at the time of invocation.
         * 
         * <p> 仅当调用时另一个线程未持有该写锁时，才获取该写锁。
         *
         * <p>Acquires the write lock if neither the read nor write lock
         * are held by another thread
         * and returns immediately with the value {@code true},
         * setting the write lock hold count to one. Even when this lock has
         * been set to use a fair ordering policy, a call to
         * {@code tryLock()} <em>will</em> immediately acquire the
         * lock if it is available, whether or not other threads are
         * currently waiting for the write lock.  This &quot;barging&quot;
         * behavior can be useful in certain circumstances, even
         * though it breaks fairness. If you want to honor the
         * fairness setting for this lock, then use {@link
         * #tryLock(long, TimeUnit) tryLock(0, TimeUnit.SECONDS) }
         * which is almost equivalent (it also detects interruption).
         * 
         * <p> 如果读取和写入锁均未由另一个线程持有，则获取写入锁，并立即返回true值，将写入锁的保持计数设置为1。
         *  即使将此锁设置为使用公平的排序策略，对tryLock（）的调用也会立即获取该锁（如果有），无论当前是否有其他线程在等待写锁。 
         *  即使破坏公平性，这种“讨价还价”的行为在某些情况下还是有用的。 如果要遵守此锁的公平性设置，
         *  请使用几乎等效的tryLock（0，TimeUnit.SECONDS）（它还会检测到中断）。
         *
         * <p>If the current thread already holds this lock then the
         * hold count is incremented by one and the method returns
         * {@code true}.
         * 
         * <p> 如果当前线程已经持有此锁，则持有计数将增加一，并且该方法返回true。
         *
         * <p>If the lock is held by another thread then this method
         * will return immediately with the value {@code false}.
         * 
         * <p> 如果锁由另一个线程持有，则此方法将立即返回false值。
         *
         * @return {@code true} if the lock was free and was acquired
         * by the current thread, or the write lock was already held
         * by the current thread; and {@code false} otherwise.
         * 
         * <p> 如果锁是空闲的并由当前线程获取，或者写锁已由当前线程持有，则为true；否则为true。 否则为假。
         */
        public boolean tryLock( ) {
            return sync.tryWriteLock();
        }

        /**
         * Acquires the write lock if it is not held by another thread
         * within the given waiting time and the current thread has
         * not been {@linkplain Thread#interrupt interrupted}.
         * 
         * <p> 如果在给定的等待时间内另一个线程未持有该写锁，并且当前线程尚未中断，则获取该写锁。
         *
         * <p>Acquires the write lock if neither the read nor write lock
         * are held by another thread
         * and returns immediately with the value {@code true},
         * setting the write lock hold count to one. If this lock has been
         * set to use a fair ordering policy then an available lock
         * <em>will not</em> be acquired if any other threads are
         * waiting for the write lock. This is in contrast to the {@link
         * #tryLock()} method. If you want a timed {@code tryLock}
         * that does permit barging on a fair lock then combine the
         * timed and un-timed forms together:
         * 
         * <p> 如果读取和写入锁均未由另一个线程持有，则获取写入锁，并立即返回true值，将写入锁的保持计数设置为1。 
         * 如果将此锁设置为使用公平的排序策略，则如果任何其他线程正在等待写锁，则不会获取可用锁。 这与tryLock（）方法相反。 
         * 如果您想要一个定时tryLock确实允许在公平锁上插入，则将定时和非定时形式组合在一起：
         *
         *  <pre> {@code
         * if (lock.tryLock() ||
         *     lock.tryLock(timeout, unit)) {
         *   ...
         * }}</pre>
         *
         * <p>If the current thread already holds this lock then the
         * hold count is incremented by one and the method returns
         * {@code true}.
         * 
         * <p> 如果当前线程已经持有此锁，则持有计数将增加一，并且该方法返回true。
         *
         * <p>If the lock is held by another thread then the current
         * thread becomes disabled for thread scheduling purposes and
         * lies dormant until one of three things happens:
         * 
         * <p> 如果该锁由另一个线程持有，则出于线程调度目的，当前线程将被禁用，并处于休眠状态，直到发生以下三种情况之一：
         *
         * <ul>
         *
         * <li>The write lock is acquired by the current thread; or
         * 
         * <p> 写锁被当前线程获取；要么
         *
         * <li>Some other thread {@linkplain Thread#interrupt interrupts}
         * the current thread; or
         * 
         * <p> 其他一些线程中断当前线程；要么
         *
         * <li>The specified waiting time elapses
         * 
         * <p> 经过指定的等待时间
         *
         * </ul>
         *
         * <p>If the write lock is acquired then the value {@code true} is
         * returned and the write lock hold count is set to one.
         * 
         * <p> 如果获取了写锁，则返回值true，并且将写锁保持计数设置为1。
         *
         * <p>If the current thread:
         * 
         * <p> 如果当前线程：
         *
         * <ul>
         *
         * <li>has its interrupted status set on entry to this method;
         * or
         * 
         * <p> 在进入此方法时已设置其中断状态；要么
         *
         * <li>is {@linkplain Thread#interrupt interrupted} while
         * acquiring the write lock,
         * 
         * <p> 在获取写锁定时被中断，
         *
         * </ul>
         *
         * then {@link InterruptedException} is thrown and the current
         * thread's interrupted status is cleared.
         * 
         * <p> 然后抛出InterruptedException并清除当前线程的中断状态。
         *
         * <p>If the specified waiting time elapses then the value
         * {@code false} is returned.  If the time is less than or
         * equal to zero, the method will not wait at all.
         * 
         * <p> 如果经过了指定的等待时间，则返回值false。如果时间小于或等于零，则该方法将根本不等待。
         *
         * <p>In this implementation, as this method is an explicit
         * interruption point, preference is given to responding to
         * the interrupt over normal or reentrant acquisition of the
         * lock, and over reporting the elapse of the waiting time.
         * 
         * <p> 在此实现中，由于此方法是显式的中断点，因此优先于对中断的响应而不是正常或可重入的锁定获取，
         * 而是优先报告等待时间的流逝。
         *
         * @param timeout the time to wait for the write lock
         * 
         * <p> 等待写锁的时间
         * 
         * @param unit the time unit of the timeout argument
         * 
         * <p> 超时参数的时间单位
         *
         * @return {@code true} if the lock was free and was acquired
         * by the current thread, or the write lock was already held by the
         * current thread; and {@code false} if the waiting time
         * elapsed before the lock could be acquired.
         * 
         * <p> 如果锁是空闲的并由当前线程获取，或者写锁已由当前线程持有，则为true；否则为true。 
         * 如果可以获取锁之前的等待时间已过，则返回false。
         *
         * @throws InterruptedException if the current thread is interrupted
         * 
         * <p> 如果当前线程被中断
         * 
         * @throws NullPointerException if the time unit is null
         */
        public boolean tryLock(long timeout, TimeUnit unit)
                throws InterruptedException {
            return sync.tryAcquireNanos(1, unit.toNanos(timeout));
        }

        /**
         * Attempts to release this lock.
         *
         * <p> 尝试释放此锁。
         * 
         * <p>If the current thread is the holder of this lock then
         * the hold count is decremented. If the hold count is now
         * zero then the lock is released.  If the current thread is
         * not the holder of this lock then {@link
         * IllegalMonitorStateException} is thrown.
         * 
         * <p> 如果当前线程是此锁的持有者，则保留计数将减少。 如果保持计数现在为零，则释放锁定。
如果当前线程不是此锁的持有者，则抛出IllegalMonitorStateException。
         *
         * @throws IllegalMonitorStateException if the current thread does not
         * hold this lock
         * 
         * <p> 如果当前线程不持有此锁
         * 
         */
        public void unlock() {
            sync.release(1);
        }

        /**
         * Returns a {@link Condition} instance for use with this
         * {@link Lock} instance.
         * 
         * <p> 返回用于此Lock实例的Condition实例。
         * 
         * <p>The returned {@link Condition} instance supports the same
         * usages as do the {@link Object} monitor methods ({@link
         * Object#wait() wait}, {@link Object#notify notify}, and {@link
         * Object#notifyAll notifyAll}) when used with the built-in
         * monitor lock.
         * 
         * <p> 当与内置监视器锁定一起使用时，返回的Condition实例支持与Object监视器方
         * 法（wait，notify和notifyAll）相同的用法。
         *
         * <ul>
         *
         * <li>If this write lock is not held when any {@link
         * Condition} method is called then an {@link
         * IllegalMonitorStateException} is thrown.  (Read locks are
         * held independently of write locks, so are not checked or
         * affected. However it is essentially always an error to
         * invoke a condition waiting method when the current thread
         * has also acquired read locks, since other threads that
         * could unblock it will not be able to acquire the write
         * lock.)
         * 
         * <p> 如果在调用任何Condition方法时未保留此写锁定，则将引发IllegalMonitorStateException。 
         * （读锁独立于写锁而持有，因此不会被检查或受影响。但是，当当前线程也已获取读锁时，调用条件等待方法本质上总是错误，
         * 因为其他可能解除阻塞它的线程不会被调用。能够获取写锁。）
         *
         * <li>When the condition {@linkplain Condition#await() waiting}
         * methods are called the write lock is released and, before
         * they return, the write lock is reacquired and the lock hold
         * count restored to what it was when the method was called.
         * 
         * <p> 调用条件等待方法时，释放写锁，然后在返回之前，重新获取写锁，并将锁保持计数恢复为调用该方法时的值。
         *
         * <li>If a thread is {@linkplain Thread#interrupt interrupted} while
         * waiting then the wait will terminate, an {@link
         * InterruptedException} will be thrown, and the thread's
         * interrupted status will be cleared.
         * 
         * <p> 如果线程在等待时被中断，则等待将终止，将抛出InterruptedException，并清除线程的中断状态。
         *
         * <li> Waiting threads are signalled in FIFO order.
         * 
         * <p> 等待的线程以FIFO顺序发出信号。
         *
         * <li>The ordering of lock reacquisition for threads returning
         * from waiting methods is the same as for threads initially
         * acquiring the lock, which is in the default case not specified,
         * but for <em>fair</em> locks favors those threads that have been
         * waiting the longest.
         * 
         * <p> 从等待方法返回的线程的锁重新获取顺序与最初获取锁的线程相同（默认情况下未指定），
         * 但对于公平锁，优先使用等待时间最长的线程。
         *
         * </ul>
         *
         * @return the Condition object
         */
        public Condition newCondition() {
            return sync.newCondition();
        }

        /**
         * Returns a string identifying this lock, as well as its lock
         * state.  The state, in brackets includes either the String
         * {@code "Unlocked"} or the String {@code "Locked by"}
         * followed by the {@linkplain Thread#getName name} of the owning thread.
         * 
         * <p> 返回标识此锁定及其锁定状态的字符串。 括号中的状态包括字符串“ Unlocked”或字符串“ Locked by”，后跟拥有线程的名称。
         *
         * @return a string identifying this lock, as well as its lock state
         * 
         * <p> 标识此锁及其锁状态的字符串
         */
        public String toString() {
            Thread o = sync.getOwner();
            return super.toString() + ((o == null) ?
                                       "[Unlocked]" :
                                       "[Locked by thread " + o.getName() + "]");
        }

        /**
         * Queries if this write lock is held by the current thread.
         * Identical in effect to {@link
         * ReentrantReadWriteLock#isWriteLockedByCurrentThread}.
         * 
         * <p> 查询此写锁是否由当前线程持有。 与ReentrantReadWriteLock.isWriteLockedByCurrentThread相同。
         *
         * @return {@code true} if the current thread holds this lock and
         *         {@code false} otherwise
         *         
         * <p> 如果当前线程持有此锁，则返回true，否则返回false
         * 
         * @since 1.6
         */
        public boolean isHeldByCurrentThread() {
            return sync.isHeldExclusively();
        }

        /**
         * Queries the number of holds on this write lock by the current
         * thread.  A thread has a hold on a lock for each lock action
         * that is not matched by an unlock action.  Identical in effect
         * to {@link ReentrantReadWriteLock#getWriteHoldCount}.
         * 
         * <p> 查询当前线程对该写锁的保留数。 对于每个未与解锁动作匹配的锁定动作，线程都会拥有一个锁。 
         * 与ReentrantReadWriteLock.getWriteHoldCount相同。
         *
         * @return the number of holds on this lock by the current thread,
         *         or zero if this lock is not held by the current thread
         *         
         * <p> 当前线程对该锁的保留次数；如果当前线程不保留此锁，则为零。
         * 
         * @since 1.6
         */
        public int getHoldCount() {
            return sync.getWriteHoldCount();
        }
    }

    // Instrumentation and status - 仪器和状态

    /**
     * Returns {@code true} if this lock has fairness set true.
     * 
     * <p> 如果此锁的公平性设置为true，则返回true。
     *
     * @return {@code true} if this lock has fairness set true
     * 
     * <p> 如果此锁的公平性设置为true，则为true
     */
    public final boolean isFair() {
        return sync instanceof FairSync;
    }

    /**
     * Returns the thread that currently owns the write lock, or
     * {@code null} if not owned. When this method is called by a
     * thread that is not the owner, the return value reflects a
     * best-effort approximation of current lock status. For example,
     * the owner may be momentarily {@code null} even if there are
     * threads trying to acquire the lock but have not yet done so.
     * This method is designed to facilitate construction of
     * subclasses that provide more extensive lock monitoring
     * facilities.
     * 
     * <p> 返回当前拥有写锁的线程；如果不拥有，则返回null。 当非所有者的线程调用此方法时，
     * 返回值反映当前锁定状态的尽力而为近似。 例如，即使有线程尝试获取锁，但所有者尚未拥有，
     * 所有者可能暂时为null。 设计此方法是为了便于构造提供更广泛的锁监视功能的子类。
     *
     * @return the owner, or {@code null} if not owned
     * 
     * <p> 所有者；如果不拥有，则为null
     */
    protected Thread getOwner() {
        return sync.getOwner();
    }

    /**
     * Queries the number of read locks held for this lock. This
     * method is designed for use in monitoring system state, not for
     * synchronization control.
     * 
     * <p> 查询为此锁持有的读取锁的数量。 此方法设计用于监视系统状态，而不用于同步控制。
     * 
     * @return the number of read locks held
     * 
     * <p> 持有的读取锁的数量
     */
    public int getReadLockCount() {
        return sync.getReadLockCount();
    }

    /**
     * Queries if the write lock is held by any thread. This method is
     * designed for use in monitoring system state, not for
     * synchronization control.
     * 
     * <p> 查询写锁是否由任何线程持有。 此方法设计用于监视系统状态，而不用于同步控制。
     *
     * @return {@code true} if any thread holds the write lock and
     *         {@code false} otherwise
     *         
     * <p> 如果任何线程持有写锁，则为true；否则为false
     */
    public boolean isWriteLocked() {
        return sync.isWriteLocked();
    }

    /**
     * Queries if the write lock is held by the current thread.
     * 
     * <p> 查询写锁是否被当前线程持有。
     *
     * @return {@code true} if the current thread holds the write lock and
     *         {@code false} otherwise
     *         
     * <p> 如果当前线程持有写锁，则为true；否则为false
     */
    public boolean isWriteLockedByCurrentThread() {
        return sync.isHeldExclusively();
    }

    /**
     * Queries the number of reentrant write holds on this lock by the
     * current thread.  A writer thread has a hold on a lock for
     * each lock action that is not matched by an unlock action.
     * 
     * <p> 查询当前线程对该锁持有的重入写入次数。 对于未与解锁动作匹配的每个锁定动作，
     * 编写器线程均拥有一个锁保持状态。
     *
     * @return the number of holds on the write lock by the current thread,
     *         or zero if the write lock is not held by the current thread
     *         
     * <p> 当前线程对写锁的保留数；如果当前线程不保留写锁，则为零
     */
    public int getWriteHoldCount() {
        return sync.getWriteHoldCount();
    }

    /**
     * Queries the number of reentrant read holds on this lock by the
     * current thread.  A reader thread has a hold on a lock for
     * each lock action that is not matched by an unlock action.
     * 
     * <p> 查询当前线程对该锁持有的可重入读取次数。 对于与解锁操作不匹配的每个锁定操作，
     * 读取器线程均具有锁定的保持状态。
     *
     * @return the number of holds on the read lock by the current thread,
     *         or zero if the read lock is not held by the current thread
     *         
     * <p> 当前线程对读锁的保留数；如果当前线程不保留读锁，则为零
     * 
     * @since 1.6
     */
    public int getReadHoldCount() {
        return sync.getReadHoldCount();
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire the write lock.  Because the actual set of threads may
     * change dynamically while constructing this result, the returned
     * collection is only a best-effort estimate.  The elements of the
     * returned collection are in no particular order.  This method is
     * designed to facilitate construction of subclasses that provide
     * more extensive lock monitoring facilities.
     * 
     * <p> 返回一个包含可能正在等待获取写锁的线程的集合。 因为实际的线程集在构造此结果时可能会动态变化，
     * 所以返回的集合只是尽力而为的估计。 返回的集合的元素没有特定的顺序。 
     * 设计此方法是为了便于构造提供更广泛的锁监视功能的子类。
     *
     * @return the collection of threads - 线程集合
     */
    protected Collection<Thread> getQueuedWriterThreads() {
        return sync.getExclusiveQueuedThreads();
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire the read lock.  Because the actual set of threads may
     * change dynamically while constructing this result, the returned
     * collection is only a best-effort estimate.  The elements of the
     * returned collection are in no particular order.  This method is
     * designed to facilitate construction of subclasses that provide
     * more extensive lock monitoring facilities.
     * 
     * <p> 返回一个包含可能正在等待获取读锁的线程的集合。 因为实际的线程集在构造此结果时可能会动态变化，
     * 所以返回的集合只是尽力而为的估计。 返回的集合的元素没有特定的顺序。 
     * 设计此方法是为了便于构造提供更广泛的锁监视功能的子类。
     *
     * @return the collection of threads - 线程集合
     */
    protected Collection<Thread> getQueuedReaderThreads() {
        return sync.getSharedQueuedThreads();
    }

    /**
     * Queries whether any threads are waiting to acquire the read or
     * write lock. Note that because cancellations may occur at any
     * time, a {@code true} return does not guarantee that any other
     * thread will ever acquire a lock.  This method is designed
     * primarily for use in monitoring of the system state.
     * 
     * <p> 查询是否有任何线程正在等待获取读或写锁。 请注意，由于取消可能随时发生，因此返回true不能保证任何其他线程都将获得锁。
     *  此方法主要设计用于监视系统状态。
     *
     * @return {@code true} if there may be other threads waiting to
     *         acquire the lock
     *         
     * <p> 如果可能还有其他线程在等待获取锁，则返回true
     */
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    /**
     * Queries whether the given thread is waiting to acquire either
     * the read or write lock. Note that because cancellations may
     * occur at any time, a {@code true} return does not guarantee
     * that this thread will ever acquire a lock.  This method is
     * designed primarily for use in monitoring of the system state.
     * 
     * <p> 查询给定线程是否正在等待获取读取或写入锁定。 请注意，由于取消可能随时发生，因此返回true不能保证此线程将获得锁。 
     * 此方法主要设计用于监视系统状态。
     *
     * @param thread the thread
     * @return {@code true} if the given thread is queued waiting for this lock
     * 
     * <p> 如果给定线程排队等待此锁，则返回true
     * 
     * @throws NullPointerException if the thread is null
     */
    public final boolean hasQueuedThread(Thread thread) {
        return sync.isQueued(thread);
    }

    /**
     * Returns an estimate of the number of threads waiting to acquire
     * either the read or write lock.  The value is only an estimate
     * because the number of threads may change dynamically while this
     * method traverses internal data structures.  This method is
     * designed for use in monitoring of the system state, not for
     * synchronization control.
     * 
     * <p> 返回等待获取读或写锁的线程数的估计值。 该值只是一个估计值，因为在此方法遍历内部数据结构时，
     * 线程数可能会动态变化。 此方法设计用于监视系统状态，而不用于同步控制。
     *
     * @return the estimated number of threads waiting for this lock
     * 
     * <p> 等待此锁的估计线程数
     */
    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire either the read or write lock.  Because the actual set
     * of threads may change dynamically while constructing this
     * result, the returned collection is only a best-effort estimate.
     * The elements of the returned collection are in no particular
     * order.  This method is designed to facilitate construction of
     * subclasses that provide more extensive monitoring facilities.
     * 
     * <p> 返回一个包含可能正在等待获取读或写锁的线程的集合。 因为实际的线程集在构造此结果时可能会动态变化，
     * 所以返回的集合只是尽力而为的估计。 返回的集合的元素没有特定的顺序。 设计此方法是为了便于构造子类，
     * 以提供更广泛的监视功能。
     *
     * @return the collection of threads
     */
    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    /**
     * Queries whether any threads are waiting on the given condition
     * associated with the write lock. Note that because timeouts and
     * interrupts may occur at any time, a {@code true} return does
     * not guarantee that a future {@code signal} will awaken any
     * threads.  This method is designed primarily for use in
     * monitoring of the system state.
     * 
     * <p> 查询是否有任何线程正在等待与写锁关联的给定条件。 请注意，因为超时和中断可能随时发生，
     * 所以真正的返回并不保证将来的信号会唤醒任何线程。 此方法主要设计用于监视系统状态。
     *
     * @param condition the condition
     * @return {@code true} if there are any waiting threads
     * 
     * <p> 如果有任何等待线程，则返回true
     * 
     * @throws IllegalMonitorStateException if this lock is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this lock
     *         
     * <p> 如果给定条件与此锁没有关联
     * 
     * @throws NullPointerException if the condition is null
     */
    public boolean hasWaiters(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.hasWaiters((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /**
     * Returns an estimate of the number of threads waiting on the
     * given condition associated with the write lock. Note that because
     * timeouts and interrupts may occur at any time, the estimate
     * serves only as an upper bound on the actual number of waiters.
     * This method is designed for use in monitoring of the system
     * state, not for synchronization control.
     * 
     * <p> 返回在与写锁关联的给定条件下等待的线程数的估计值。 请注意，由于超时和中断可能随时发生，
     * 因此估算值仅用作实际侍者数的上限。 此方法设计用于监视系统状态，而不用于同步控制。
     * 
     *
     * @param condition the condition
     * @return the estimated number of waiting threads
     * 
     * <p> 估计的等待线程数
     * 
     * @throws IllegalMonitorStateException if this lock is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this lock
     *         
     * <p> 如果给定条件与此锁没有关联
     * 
     * @throws NullPointerException if the condition is null
     */
    public int getWaitQueueLength(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /**
     * Returns a collection containing those threads that may be
     * waiting on the given condition associated with the write lock.
     * Because the actual set of threads may change dynamically while
     * constructing this result, the returned collection is only a
     * best-effort estimate. The elements of the returned collection
     * are in no particular order.  This method is designed to
     * facilitate construction of subclasses that provide more
     * extensive condition monitoring facilities.
     * 
     * <p> 返回一个包含那些可能正在等待与写锁关联的给定条件的线程的集合。 因为实际的线程集在构造此结果时可能会动态变化，
     * 所以返回的集合只是尽力而为的估计。 返回的集合的元素没有特定的顺序。 设计此方法是为了便于构造提供更广泛的状态监视工具的子类。
     *
     * @param condition the condition
     * @return the collection of threads
     * @throws IllegalMonitorStateException if this lock is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this lock
     *         
     * <p> 如果给定条件与此锁没有关联
     * 
     * @throws NullPointerException if the condition is null
     */
    protected Collection<Thread> getWaitingThreads(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitingThreads((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /**
     * Returns a string identifying this lock, as well as its lock state.
     * The state, in brackets, includes the String {@code "Write locks ="}
     * followed by the number of reentrantly held write locks, and the
     * String {@code "Read locks ="} followed by the number of held
     * read locks.
     * 
     * <p> 返回标识此锁定及其锁定状态的字符串。 括号中的状态包括字符串“ Write locks =”，后跟可重入的写入锁的数量，
     * 以及字符串“ Read locks =”，后跟已持有的读取锁的数量。
     *
     * @return a string identifying this lock, as well as its lock state
     * 
     * <p> 标识此锁及其锁状态的字符串
     */
    public String toString() {
        int c = sync.getCount();
        int w = Sync.exclusiveCount(c);
        int r = Sync.sharedCount(c);

        return super.toString() +
            "[Write locks = " + w + ", Read locks = " + r + "]";
    }

    /**
     * Returns the thread id for the given thread.  We must access
     * this directly rather than via method Thread.getId() because
     * getId() is not final, and has been known to be overridden in
     * ways that do not preserve unique mappings.
     * 
     * <p> 返回给定线程的线程ID。 我们必须直接访问它，而不是通过方法Thread.getId（）来访问它，
     * 因为getId（）不是最终的，并且已知它会以不保留唯一映射的方式被覆盖。
     */
    static final long getThreadId(Thread thread) {
        return UNSAFE.getLongVolatile(thread, TID_OFFSET);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long TID_OFFSET;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> tk = Thread.class;
            TID_OFFSET = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("tid"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
