import java.lang.reflect.GenericSignatureFormatError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;

public class Relation<T> {

	private final Collection<T> elements;

	@SafeVarargs
	public Relation(T... elements) {
		this.elements = Arrays.<T>asList(elements);
	}

	public Relation(Collection<T> elements) {
		this.elements = elements;
	}

	public static class Tuple<T> {
		public final T key;
		public final T val;

		public Tuple(T key, T val) {
			if (!(key instanceof Number) && !(key instanceof String) && !(key instanceof Character))
				throw new GenericSignatureFormatError("Only string and number are accepted");

			this.key = key;
			this.val = val;
		}

		@Override
		public String toString() {
			return "(" + key + "," + val + ")";
		}
	}

	private ArrayList<Tuple<T>> tuples = new ArrayList<Tuple<T>>();
	private Hashtable<T, Integer> keys = new Hashtable<T, Integer>();
	private Hashtable<T, Integer> vals = new Hashtable<T, Integer>();

	public Relation<T> add(T key, T val) {
		add0(key, val);
		return this;
	}

	private boolean add0(T key, T val) {
		if (!elements.contains(key) || !elements.contains(val))
			throw new IllegalArgumentException("Element not in collection");

		for (Tuple<T> t : tuples)
			if (t.key.equals(key) && t.val.equals(val))
				return false;

		tuples.add(new Tuple<T>(key, val));
		keys.put(key, toInt(keys.get(key)) + 1);
		vals.put(val, toInt(vals.get(val)) + 1);

		return true;
	}

	private int toInt(Integer i) {
		if (i == null)
			return 0;
		else
			return i;
	}

	public Relation<T> kleene() {
		transitiveClosure();
		reflexiveClosure();
		return this;
	}

	public Relation<T> composition(int times) {
		Relation<T> r = new Relation<T>(elements);
		if (times == 0) {
			for (T e : elements)
				r.add(e, e);
		}

		if (times == 1)
			return this;

		r = this;

		for (int i = 0; i < times - 1; i++)
			r = composition(r);

		return r;
	}

	public Relation<T> symmetricalClosure() {
		ArrayList<Tuple<T>> tmp = new ArrayList<Tuple<T>>();
		for (Tuple<T> t : tuples)
			tmp.add(t);

		for (Tuple<T> t : tmp)
			add(t.val, t.key);

		return this;
	}

	public Relation<T> composition(Relation<T> rel) {
		Relation<T> r = new Relation<T>(elements);
		for (Tuple<T> t1 : tuples)
			for (Tuple<T> t2 : rel.tuples)
				if (t1.val.equals(t2.key))
					r.add(t1.key, t2.val);
		return r;
	}

	public boolean contains(T key, T val) {
		for (Tuple<T> t : tuples)
			if (t.key.equals(key) && t.val.equals(val))
				return true;
		return false;
	}

	public Relation<T> remove(T key, T val) {
		tuples.removeIf(e -> e.key.equals(key) && e.val.equals(val));
		return this;
	}

	public Relation<T> transitiveClosure() {
		Relation<T> res = composition(this);
		for (Tuple<T> t : res.tuples)
			add(t.key, t.val);

		int app = -1;

		while ((res = composition(res)).tuples.size() > 0 && app != 0) {
			app = 0;
			for (Tuple<T> t : res.tuples) {
				if (add0(t.key, t.val))
					app++;
			}
		}

		return this;
	}

	public void print() {
		for (Tuple<T> t : tuples)
			System.out.print(t.toString() + ",");
	}

	public void println() {
		for (Tuple<T> t : tuples)
			System.out.println(t.toString() + ",");
	}

	public Relation<T> reflexiveClosure() {
		for (T e : elements)
			add(e, e);

		return this;
	}

	public boolean isTotal() {
		for (T e : elements)
			if (!keys.contains(e))
				return false;
		return true;
	}

	public boolean isUnivalent() {
		for (T e : elements)
			if (toInt(keys.get(e)) > 1)
				return false;
		return true;
	}

	public Relation<T> sort() {
		tuples.sort(new Comparator<Tuple<T>>() {

			@Override
			public int compare(Tuple<T> o1, Tuple<T> o2) {
				int res = o1.key.toString().compareTo(o2.key.toString());
				if (res == 0)
					return o1.val.toString().compareTo(o2.val.toString());
				return res;
			}
		});

		return this;
	}

	public boolean isSurgettive() {
		for (T e : elements)
			if (vals.contains(e))
				return false;
		return true;
	}

	public boolean isIniettive() {
		for (T e : elements)
			if (toInt(vals.get(e)) > 1)
				return false;
		return true;
	}

	public boolean isBijection() {
		return isTotal() && isUnivalent() && isSurgettive() && isIniettive();
	}

	public boolean isFunction() {
		for (T e : elements)
			if (toInt(keys.get(e)) != 1)
				return false;
		return true;

	}

	public Relation<T> op() {
		Relation<T> r = new Relation<T>(elements);

		for (Tuple<T> t : tuples)
			r.add(t.val, t.key);

		return r;
	}

	public static void main(String... args) {
		Relation<Number> r = new Relation<Number>(1, 2, 3, 4, 5);
		r.add(2, 2).add(1, 1).add(2, 3).add(3, 4).add(4, 5);
		r.kleene().sort().print();
	}

}
