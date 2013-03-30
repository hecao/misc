package net.dongliu.push.utils;

/**
 * 对象池,只记录空闲的对象. 先入后出.
 * 
 * @author dongliu
 * 
 */
public class ObjectPool<T> {

	private Object[] oa;

	private int cursor;
	private ObjectFactory<T> factory;

	/**
	 * 构造函数.
	 * 
	 * @param size
	 * @param factory
	 */
	public ObjectPool(int size, ObjectFactory<T> factory) {
		if (size <= 1) {
			throw new IllegalArgumentException("Pool size should be greater than 1.");
		}
		this.oa = new Object[size];
		this.cursor = 0;
	}

	/**
	 * 从对象池获取一个实例. 如果对象池为空，则创建新实例.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T getObject() {
		synchronized (this) {
			if (cursor > 0) {
				cursor--;
				T t = (T) oa[cursor];
				oa[cursor] = null;
				return t;
			}
		}
		return factory.newObject();
	}

	/**
	 * 向对象池归还一个实例. 如果对象池满，则此实例被丢弃.
	 * 
	 * @param t
	 */
	public void returnObject(T t) {
		if (t != null) {
			factory.resetObject(t);
			synchronized (this) {
				if (cursor < oa.length) {
					oa[cursor] = t;
					cursor++;
				}
			}
		}
	}

	/**
	 * 销毁.
	 */
	public void close() {
		this.oa = null;
		this.factory = null;
	}

	public int size() {
		return oa.length;
	}

	/**
	 * 创建对象工厂接口.
	 * 
	 * @author dongliu
	 * 
	 * @param <T>
	 */
	public static interface ObjectFactory<T> {
		public T newObject();

		public void resetObject(T t);
	}
}
